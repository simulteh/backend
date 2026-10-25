#!/usr/bin/env python3
"""Exercise deployment decisions with real Git and isolated Docker/SSH mocks.

Run: python3 -m unittest discover -s deploy/tests -p 'test_*.py' -v
No Docker daemon, network access, SSH credentials or third-party modules needed.
"""

import json
import os
from pathlib import Path
import subprocess
import tempfile
import textwrap
import unittest


SCRIPT = Path(__file__).resolve().parents[1] / "deploy-vm.sh"

MOCK = """#!/usr/bin/env python3
import json
import os
from pathlib import Path
import sys

name = Path(sys.argv[0]).name
args = sys.argv[1:]
with open(os.environ['MOCK_LOG'], 'a') as output:
    output.write(json.dumps({'name': name, 'args': args, 'cwd': os.getcwd()}) + '\\n')
if name == 'docker':
    project = os.environ['SIMULTEH_PROJECT_DIR']
    expected = ['compose', '--project-name', 'net-genius', '--project-directory',
                project, '--file', project + '/docker-compose.yml']
    if args[:len(expected)] != expected or os.getcwd() != project:
        sys.exit(90)
    command = args[len(expected):]
    if command == ['config', '--quiet']:
        sys.exit(int(os.environ.get('CONFIG_EXIT', '0')))
    if command == ['build', 'backend']:
        sys.exit(int(os.environ.get('BUILD_EXIT', '0')))
    if command == ['up', '--detach', '--no-deps', '--no-build', 'backend']:
        sys.exit(int(os.environ.get('UP_EXIT', '0')))
    if command == ['exec', '-T', 'backend', 'curl', '--fail', '--silent',
                   '--max-time', '2', 'http://127.0.0.1:8080/auth/ping']:
        print(os.environ.get('READY_BODY', 'pong'))
        sys.exit(int(os.environ.get('READY_EXIT', '0')))
    if command == ['exec', '-T', 'nginx', 'nginx', '-t']:
        sys.exit(int(os.environ.get('NGINX_TEST_EXIT', '0')))
    if command == ['exec', '-T', 'nginx', 'nginx', '-s', 'reload']:
        sys.exit(int(os.environ.get('NGINX_RELOAD_EXIT', '0')))
    sys.exit(91)
if name == 'curl':
    if args != ['--fail', '--silent', '--max-time', '5', '--',
                'https://www.simultech.ru/api/auth/ping']:
        sys.exit(92)
    print(os.environ.get('PUBLIC_HEALTH_BODY', 'pong'))
    sys.exit(int(os.environ.get('PUBLIC_HEALTH_EXIT', '0')))
if name == 'flock':
    if args != ['--exclusive', '--wait', '900', '9']:
        sys.exit(93)
    sys.exit(int(os.environ.get('FLOCK_EXIT', '0')))
if name == 'sleep':
    sys.exit(0)
sys.exit(94)
"""


class VmDeploymentTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory(prefix="simulteh-deploy-test-")
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name).resolve()
        self.origin = self.root / "origin.git"
        self.seed = self.root / "seed"
        self.project = self.root / "net-genius"
        self.repo = self.project / "backend"
        self.project.mkdir()
        (self.project / "docker-compose.yml").write_text("services: {}\n")
        self.git("init", "--bare", "-b", "main", str(self.origin))
        self.git("clone", str(self.origin), str(self.seed))
        (self.seed / ".gitignore").write_text(".env\n")
        (self.seed / "app.txt").write_text("old version\n")
        self.commit(self.seed, "initial")
        self.git("-C", str(self.seed), "push", "origin", "main")
        self.old_sha = self.git("-C", str(self.seed), "rev-parse", "HEAD").stdout.strip()
        self.git("clone", str(self.origin), str(self.repo))
        (self.repo / ".env").write_text("JWT_SECRET=must-not-be-printed\n")
        (self.seed / "app.txt").write_text("new version\n")
        self.commit(self.seed, "next version")
        self.git("-C", str(self.seed), "push", "origin", "main")
        self.new_sha = self.git("-C", str(self.seed), "rev-parse", "HEAD").stdout.strip()
        self.mock_bin = self.root / "bin"
        self.mock_bin.mkdir()
        for command in ("docker", "curl", "flock", "sleep"):
            path = self.mock_bin / command
            path.write_text(textwrap.dedent(MOCK))
            path.chmod(0o755)
        self.log = self.root / "commands.jsonl"
        self.env = os.environ.copy()
        self.env.pop("SSH_ORIGINAL_COMMAND", None)
        self.env.update({
            "PATH": str(self.mock_bin) + os.pathsep + self.env["PATH"],
            "SIMULTEH_PROJECT_DIR": str(self.project),
            "SIMULTEH_LOCK_FILE": str(self.root / "deploy.lock"),
            "SIMULTEH_READY_ATTEMPTS": "3",
            "SIMULTEH_READY_DELAY_SECONDS": "0",
            "SIMULTEH_PUBLIC_HEALTH_ATTEMPTS": "2",
            "MOCK_LOG": str(self.log),
        })

    @staticmethod
    def git(*args):
        return subprocess.run(["git", *args], check=True, text=True,
                              stdout=subprocess.PIPE, stderr=subprocess.PIPE)

    def commit(self, repo, message):
        self.git("-C", str(repo), "add", ".")
        self.git("-C", str(repo), "-c", "user.name=Deployment Test",
                 "-c", "user.email=deploy@example.invalid", "commit", "-qm", message)

    def run_deploy(self, sha=None, *, original=None, extra_env=None):
        env = self.env.copy()
        if original is not None:
            env["SSH_ORIGINAL_COMMAND"] = original
            args = []
        else:
            args = [sha or self.new_sha]
        env.update(extra_env or {})
        result = subprocess.run(["bash", str(SCRIPT), *args], env=env,
                                cwd=self.root, text=True, stdout=subprocess.PIPE,
                                stderr=subprocess.PIPE, timeout=20)
        self.assertNotIn("must-not-be-printed", result.stdout + result.stderr)
        return result

    def commands(self):
        if not self.log.exists():
            return []
        return [json.loads(line) for line in self.log.read_text().splitlines()]

    def docker_commands(self):
        return [entry["args"][7:] for entry in self.commands() if entry["name"] == "docker"]

    def head(self):
        return self.git("-C", str(self.repo), "rev-parse", "HEAD").stdout.strip()

    def test_success_updates_exact_commit_and_only_backend_then_reloads_nginx(self):
        result = self.run_deploy(original="deploy " + self.new_sha)
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        self.assertEqual(self.head(), self.new_sha)
        self.assertEqual(self.docker_commands(), [
            ["config", "--quiet"],
            ["build", "backend"],
            ["up", "--detach", "--no-deps", "--no-build", "backend"],
            ["exec", "-T", "backend", "curl", "--fail", "--silent", "--max-time", "2",
             "http://127.0.0.1:8080/auth/ping"],
            ["exec", "-T", "nginx", "nginx", "-t"],
            ["exec", "-T", "nginx", "nginx", "-s", "reload"],
        ])
        self.assertEqual(self.commands()[0]["name"], "flock")
        self.assertEqual(self.commands()[-1]["name"], "curl")
        self.assertEqual((self.repo / ".env").read_text(), "JWT_SECRET=must-not-be-printed\n")

    def test_manual_full_sha_is_supported(self):
        result = self.run_deploy(self.new_sha.upper())
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(self.head(), self.new_sha)

    def test_dirty_tracked_files_are_rejected_without_build_or_checkout(self):
        (self.repo / "app.txt").write_text("important local edit\n")
        result = self.run_deploy()
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("local changes", result.stderr)
        self.assertEqual(self.head(), self.old_sha)
        self.assertEqual(self.docker_commands(), [])
        self.assertEqual((self.repo / "app.txt").read_text(), "important local edit\n")

    def test_stale_workflow_sha_skips_without_build_or_checkout(self):
        result = self.run_deploy(self.old_sha)
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn("Skipping", result.stdout)
        self.assertEqual(self.head(), self.old_sha)
        self.assertEqual(self.docker_commands(), [])

    def test_invalid_ssh_commands_cannot_execute_or_change_files(self):
        marker = self.root / "must-not-exist"
        for command in ("", "bash", self.new_sha, "deploy main", "deploy " + self.new_sha[:8],
                        "deploy " + self.new_sha + " extra", "deploy " + self.new_sha + "\n",
                        "deploy " + self.new_sha + "; touch " + str(marker),
                        "deploy $(touch " + str(marker) + ")"):
            with self.subTest(command=command):
                result = self.run_deploy(original=command)
                self.assertNotEqual(result.returncode, 0)
                self.assertFalse(marker.exists())
                self.assertFalse((self.root / "deploy.lock").exists())
                self.assertEqual(self.commands(), [])
                self.assertEqual(self.head(), self.old_sha)

    def test_non_main_checkout_is_rejected(self):
        self.git("-C", str(self.repo), "switch", "-c", "local-work")
        result = self.run_deploy()
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("must be on main", result.stderr)
        self.assertEqual(self.docker_commands(), [])

    def test_local_ahead_branch_is_not_silently_built_or_reset(self):
        (self.repo / "app.txt").write_text("local commit\n")
        self.commit(self.repo, "local work")
        local_sha = self.head()
        result = self.run_deploy()
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("diverged", result.stderr)
        self.assertEqual(self.head(), local_sha)
        self.assertEqual(self.docker_commands(), [])

    def test_lock_failure_prevents_git_update_and_build(self):
        result = self.run_deploy(extra_env={"FLOCK_EXIT": "1"})
        self.assertNotEqual(result.returncode, 0)
        self.assertEqual(self.head(), self.old_sha)
        self.assertEqual(self.docker_commands(), [])

    def test_compose_validation_failure_prevents_build_and_replacement(self):
        result = self.run_deploy(extra_env={"CONFIG_EXIT": "12"})
        self.assertEqual(result.returncode, 12, result.stdout + result.stderr)
        self.assertEqual(self.docker_commands(), [["config", "--quiet"]])

    def test_failed_build_never_replaces_the_running_backend(self):
        result = self.run_deploy(extra_env={"BUILD_EXIT": "13"})
        self.assertEqual(result.returncode, 13, result.stdout + result.stderr)
        self.assertEqual(self.docker_commands(), [["config", "--quiet"], ["build", "backend"]])

    def test_failed_backend_start_stops_further_actions(self):
        result = self.run_deploy(extra_env={"UP_EXIT": "14"})
        self.assertEqual(result.returncode, 14, result.stdout + result.stderr)
        self.assertEqual(len(self.docker_commands()), 3)

    def test_readiness_requires_pong_and_is_bounded_without_nginx_reload(self):
        for failure in ({"READY_BODY": "not pong"}, {"READY_EXIT": "7"}):
            with self.subTest(failure=failure):
                self.log.unlink(missing_ok=True)
                result = self.run_deploy(extra_env=failure)
                self.assertNotEqual(result.returncode, 0)
                commands = self.docker_commands()
                self.assertEqual(len([cmd for cmd in commands if cmd[:3] == ["exec", "-T", "backend"]]), 3)
                self.assertFalse(any(cmd[:3] == ["exec", "-T", "nginx"] for cmd in commands))
                self.assertFalse(any(entry["name"] == "curl" for entry in self.commands()))

    def test_invalid_nginx_config_is_not_reloaded(self):
        result = self.run_deploy(extra_env={"NGINX_TEST_EXIT": "15"})
        self.assertEqual(result.returncode, 15, result.stdout + result.stderr)
        self.assertEqual(self.docker_commands()[-1], ["exec", "-T", "nginx", "nginx", "-t"])
        self.assertFalse(any(entry["name"] == "curl" for entry in self.commands()))

    def test_nginx_reload_error_is_not_reported_as_success(self):
        result = self.run_deploy(extra_env={"NGINX_RELOAD_EXIT": "16"})
        self.assertEqual(result.returncode, 16, result.stdout + result.stderr)
        self.assertNotIn("Deployment succeeded", result.stdout)
        self.assertFalse(any(entry["name"] == "curl" for entry in self.commands()))

    def test_public_health_failure_is_bounded_and_reported(self):
        result = self.run_deploy(extra_env={"PUBLIC_HEALTH_BODY": "wrong upstream"})
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("public HTTPS", result.stderr)
        self.assertEqual(len([entry for entry in self.commands() if entry["name"] == "curl"]), 2)
        self.assertNotIn("Deployment succeeded", result.stdout)


if __name__ == "__main__":
    unittest.main()

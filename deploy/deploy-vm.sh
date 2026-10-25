#!/usr/bin/env bash
# Install this reviewed file as /usr/local/sbin/simulteh-deploy on the VM.
# The restricted deployment key invokes it without arguments; its only accepted
# SSH command is: deploy <full commit SHA>. Local administrators may pass one SHA.
set -euo pipefail
umask 077

fail() {
    printf 'Deployment failed: %s\n' "$1" >&2
    exit 1
}

command_pattern='^deploy ([0-9a-fA-F]{40})$'
if [[ ${SSH_ORIGINAL_COMMAND+x} ]]; then
    [[ $# -eq 0 && $SSH_ORIGINAL_COMMAND =~ $command_pattern ]] \
        || fail 'the SSH command must be deploy followed by one full commit SHA'
    requested_sha=${BASH_REMATCH[1]}
else
    [[ $# -eq 1 && $1 =~ ^[0-9a-fA-F]{40}$ ]] \
        || fail 'supply one full commit SHA'
    requested_sha=$1
fi
requested_sha=$(printf '%s' "$requested_sha" | tr '[:upper:]' '[:lower:]')

# These settings belong to the trusted server environment, never SSH arguments.
project_dir=${SIMULTEH_PROJECT_DIR:-/root/net-genius}
lock_file=${SIMULTEH_LOCK_FILE:-/run/lock/simulteh-deploy.lock}
ready_attempts=${SIMULTEH_READY_ATTEMPTS:-60}
ready_delay=${SIMULTEH_READY_DELAY_SECONDS:-2}
public_attempts=${SIMULTEH_PUBLIC_HEALTH_ATTEMPTS:-15}
public_health_url=${SIMULTEH_PUBLIC_HEALTH_URL:-https://www.simultech.ru/api/auth/ping}

[[ $project_dir == /* && -d $project_dir/backend/.git \
    && -f $project_dir/docker-compose.yml ]] \
    || fail 'the root stack and backend checkout must already exist'
[[ $lock_file == /* ]] || fail 'the lock file path must be absolute'
[[ $ready_attempts =~ ^[1-9][0-9]?$ ]] || fail 'invalid readiness attempt limit'
[[ $public_attempts =~ ^[1-9][0-9]?$ ]] || fail 'invalid public health attempt limit'
[[ $ready_delay =~ ^([0-9]|[12][0-9]|30)$ ]] || fail 'invalid readiness delay'
[[ $public_health_url == https://* ]] || fail 'public health URL must use HTTPS'

for executable in git docker flock curl sleep; do
    command -v "$executable" >/dev/null 2>&1 || fail "required executable missing: $executable"
done

# Keep the lock for this entire shell, including build, restart and health checks.
# Waiting runs re-check origin/main under the lock, so an older queued run skips.
exec 9>"$lock_file"
flock --exclusive --wait 900 9 || fail 'another deployment still holds the lock'

cd -- "$project_dir"
repo=$project_dir/backend
compose() {
    docker compose --project-name net-genius --project-directory "$project_dir" \
        --file "$project_dir/docker-compose.yml" "$@"
}

[[ $(git -C "$repo" symbolic-ref --quiet --short HEAD) == main ]] \
    || fail 'the backend checkout must be on main'
[[ -z $(git -C "$repo" status --porcelain --untracked-files=no) ]] \
    || fail 'tracked backend files have local changes; resolve them manually'

printf 'Fetching main for deployment.\n'
git -C "$repo" fetch --no-tags origin refs/heads/main:refs/remotes/origin/main
remote_sha=$(git -C "$repo" rev-parse --verify refs/remotes/origin/main)
if [[ $requested_sha != "$remote_sha" ]]; then
    printf 'Skipping this run: origin/main has a different commit.\n'
    exit 0
fi

# A locally diverged/ahead main is an error, never a reason for a hard reset.
git -C "$repo" merge-base --is-ancestor HEAD "$requested_sha" \
    || fail 'local main has diverged from origin/main; resolve it manually'
git -C "$repo" merge --ff-only --no-edit "$requested_sha"
[[ $(git -C "$repo" rev-parse HEAD) == "$requested_sha" ]] \
    || fail 'the checked-out commit differs from the requested deployment'

# Quiet config validation does not print resolved .env or database credentials.
compose config --quiet
printf 'Building backend at %s.\n' "$requested_sha"
compose build backend
# Only replace the backend once its build has succeeded. Other services and the
# existing net-genius_postgres_data volume are not recreated or modified here.
compose up --detach --no-deps --no-build backend

backend_ready() {
    local attempt response
    for ((attempt = 1; attempt <= ready_attempts; attempt++)); do
        if response=$(compose exec -T backend curl --fail --silent --max-time 2 \
            http://127.0.0.1:8080/auth/ping 2>/dev/null); then
            [[ $response != pong ]] || return 0
        fi
        if ((attempt < ready_attempts)); then
            sleep "$ready_delay"
        fi
    done
    return 1
}

printf 'Waiting for backend readiness.\n'
backend_ready || fail 'backend did not become ready within the configured limit'

# Nginx caches the upstream container IP. Reload only after both backend
# readiness and nginx configuration validation, to resolve its new address.
compose exec -T nginx nginx -t
compose exec -T nginx nginx -s reload

public_ready() {
    local attempt response
    for ((attempt = 1; attempt <= public_attempts; attempt++)); do
        if response=$(curl --fail --silent --max-time 5 -- "$public_health_url" 2>/dev/null); then
            [[ $response != pong ]] || return 0
        fi
        if ((attempt < public_attempts)); then
            sleep "$ready_delay"
        fi
    done
    return 1
}

public_ready || fail 'the public HTTPS API health check failed'
printf 'Deployment succeeded: %s\n' "$requested_sha"

# Запуск на ВМ

Бэкенд и PostgreSQL запускаются в Docker на одной ВМ. На ВМ нужны Git,
Docker Engine и Docker Compose v2 (`docker compose version`). Java и Maven
на саму ВМ устанавливать не нужно: сборка выполняется в Dockerfile.

## 1. Получить код

В существующем клоне репозитория переключитесь на `main`.
При первом обновлении на эту версию старый `.env` перестаёт храниться в Git.
Если он есть, сначала сохраните его настройки вне репозитория:

```bash
set -e
if [ -f .env ] && git ls-files --error-unmatch .env >/dev/null 2>&1; then
  cp -p .env ../backend.env.backup
  chmod 600 ../backend.env.backup
  git restore -- .env
fi
git switch main
git pull --ff-only origin main
```

## 2. Заполнить настройки

Если `.env` ещё нет, создайте его из шаблона:

```bash
test -f .env || cp .env.example .env
chmod 600 .env
nano .env
```

Все секреты указываются здесь, в `.env` рядом с `docker-compose.yml`.
Файл не попадает в Git и образ приложения. Для паролей используйте одинарные
кавычки: `SMTP_PASSWORD='значение'`. Это сохраняет символы `$` и `#`
([правила Docker Compose](https://docs.docker.com/compose/how-tos/environment-variables/variable-interpolation/#env-file-syntax)).

Обязательно заполните:

| Переменная | Что указать |
|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | Имя БД, пользователь и пароль. Если база уже существует, сохраните её действующие значения из старых настроек |
| `JWT_SECRET` | Один раз сгенерированный ключ: `openssl rand -base64 64 \| tr -d '\n'` |
| `SMTP_USERNAME` | Логин почтового ящика, обычно полный email |
| `SMTP_PASSWORD` | Сгенерированный SMTP-пароль / пароль приложения |
| `FRONTEND_URL` | Адрес фронтенда, например `https://simulteh.com`. По нему формируются ссылки подтверждения и сброса пароля |
| `TEACHER_REQUEST_EMAIL` | Почта, куда отправлять заявки преподавателей с документами |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Учётные данные первого администратора |

`SMTP_HOST` и `SMTP_PORT` должны соответствовать выданным настройкам почты.
В шаблоне указан Яндекс с портом 587:

```dotenv
SMTP_HOST=smtp.yandex.ru
SMTP_PORT=587
SMTP_STARTTLS=true
SMTP_SSL=false
```

Если SMTP-сервер использует порт 465 с SSL:

```dotenv
SMTP_PORT=465
SMTP_STARTTLS=false
SMTP_SSL=true
```

`SMTP_FROM` можно оставить пустым: отправителем будет `SMTP_USERNAME`.
Другой адрес должен быть разрешён почтовым провайдером для этого ящика.

JWT-ключ сохраняется между перезапусками. Его замена делает ранее выданные
access-токены недействительными. `ADMIN_PASSWORD` используется только при
создании нового администратора; пароль существующего аккаунта меняется через API.
`SEED_LABS=false` оставляет демонстрационные лабораторные выключенными.

Изменение `POSTGRES_*` в `.env` не меняет пароль или имя уже созданной базы.
Для существующей БД используйте прежние значения и прежний Docker volume.
Запускайте Compose из того же каталога и с тем же именем проекта, что и раньше:
так сохранится подключение к существующему `postgres_data`.

## 3. Запустить

```bash
docker compose config --quiet
docker compose up -d --build --wait --wait-timeout 180
docker compose ps
curl --fail http://127.0.0.1:8080/auth/ping
```

Последняя команда должна вернуть `pong`. Если меняли `APP_PORT`, используйте
его вместо 8080. `--wait` ожидает прохождения проверок PostgreSQL и приложения.
Команда `config --quiet` проверяет конфигурацию, не печатая секреты.

PostgreSQL доступен приложению по `postgres:5432` внутри Docker-сети.
Порт базы не публикуется на ВМ. API публикуется на `APP_PORT` (по умолчанию 8080).
Если nginx установлен на этой же ВМ, задайте `APP_BIND_ADDRESS=127.0.0.1`
и проксируйте запросы на `http://127.0.0.1:8080`. Для прямого доступа к API
используется `APP_BIND_ADDRESS=0.0.0.0`. HTTPS настраивается на reverse proxy.

Swagger: `http://АДРЕС_ВМ:8080/swagger-ui.html` при прямом доступе либо
`/swagger-ui.html` на домене API за nginx. На фронтенде должны существовать
страницы `/confirm-email?token=...` и `/reset-password?token=...`.

## 4. Проверить почту и вход

1. Зарегистрировать тестового пользователя через фронтенд или Swagger.
2. Убедиться, что письмо пришло и ссылка ведёт на `FRONTEND_URL`.
3. Подтвердить email и войти; проверить `GET /profile/me` с access-токеном.
4. Отправить заявку преподавателя с PDF/JPG/PNG/DOC и проверить письмо
   с вложением на `TEACHER_REQUEST_EMAIL`.
5. Войти администратором, открыть `GET /admin/verifications` и одобрить заявку
   через `POST /admin/verifications/{id}/approve`.

Доступность `/auth/ping` проверяет работу API, но не доставку писем.
Ошибки запуска и SMTP смотрите в логах:

```bash
docker compose logs --tail=100 app
docker compose logs --tail=100 postgres
```

## Последующие обновления

```bash
git pull --ff-only origin main
docker compose config --quiet
docker compose up -d --build --wait --wait-timeout 180
```

`.env` заполняется один раз и сохраняется при `git pull`. После изменения
секретов снова выполните `docker compose up -d`: простой `restart` не обновляет
окружение существующего контейнера. Данные PostgreSQL сохраняются в volume.
`docker compose down -v` удаляет этот volume вместе с данными базы.

Перед обновлением существующей базы сделайте резервную копию. Например:

```bash
mkdir -p backups
docker compose exec -T postgres sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc' > backups/before-update.dump
```

Каталог `backups/` исключён из Git и Docker build context. Сохраняйте копии
под отдельными именами и переносите нужные резервные копии за пределы ВМ.

Сейчас схема обновляется Hibernate и `db/profile-schema.sql`; полноценные
версионированные миграции остаются отдельной задачей.

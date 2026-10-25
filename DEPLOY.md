# Обновление существующей ВМ

Эта инструкция относится к существующему проекту `/root/net-genius`:

```text
/root/net-genius/
├── docker-compose.yml     ← запускает все четыре сервиса
├── backend/              ← Git-репозиторий бэкенда
│   ├── Dockerfile
│   └── .env              ← новые настройки почты, JWT и администратора
├── frontend/
└── nginx/nginx.conf
```

Сервисы уже называются `nginx`, `frontend`, `backend`, `database`.
Nginx работает **в контейнере** и обращается к `backend:8080`.
Бэкенд обращается к `database:5432`. Снаружи открыты только порты nginx 80/443.
Существующая PostgreSQL 15 хранит данные в volume `postgres_data`.

Команды ниже выполняются **из `/root/net-genius`**, даже если меняются файлы
бэкенда. Все команды с `docker compose` используют корневой Compose.
`backend/docker-compose.yml` — отдельный вариант запуска бэкенда с новой БД;
на этой ВМ его не используем. Отсутствие этого файла в старом клоне было
локальным удалением (`D` в `git status`). После `git restore` он может вернуться,
но запускать его здесь не требуется.

## 1. Сохранить текущую версию и базу

Сначала создайте приватную копию. В архив попадут старые настройки и сертификаты,
поэтому не отправляйте его в чат или Git. Эти команды не останавливают контейнеры.

```bash
(
set -eu
cd /root/net-genius
umask 077
VM_BACKUP_DIR="/root/net-genius-backups/$(date +%Y%m%d-%H%M%S)"
mkdir -p "$VM_BACKUP_DIR"
tar -czf "$VM_BACKUP_DIR/code-config.tar.gz" backend docker-compose.yml nginx
docker compose exec -T database sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc' > "$VM_BACKUP_DIR/database.dump"
test -s "$VM_BACKUP_DIR/database.dump"
docker compose exec -T database pg_restore --list < "$VM_BACKUP_DIR/database.dump" > "$VM_BACKUP_DIR/database-contents.txt"
tar -tzf "$VM_BACKUP_DIR/code-config.tar.gz" > /dev/null
printf 'Копия сохранена: %s\n' "$VM_BACKUP_DIR"
)
```

Блок останавливается при первой ошибке. В таком случае сначала исправьте её.
Переходите дальше только после успешного сохранения архива и проверки дампа.
`pg_dump` читает работающую БД; ничего в ней не меняет.

## 2. Отбросить старые изменения кода и получить новую версию

Пользователь разрешил отбросить незакоммиченные изменения **в клоне бэкенда
на ВМ**. Архив из шага 1 позволяет их восстановить. Команда `restore`
возвращает отслеживаемые Git-файлы к последнему коммиту; неотслеживаемые файлы
не удаляет. Корневой Compose, nginx и PostgreSQL эта команда не меняет.

Сначала слейте подготовленную ветку `codex/vm-deploy` в `main` на GitHub.
Затем на ВМ:

```bash
cd /root/net-genius
git -C backend restore --source=HEAD --staged --worktree -- .
git -C backend status --short --branch
git -C backend pull --ff-only origin main
git -C backend log -1 --oneline
```

При ошибке `pull` остановитесь и разберите причину. Не используйте
`git clean` или `git reset --hard`. Сам `git pull` только обновляет файлы;
старый бэкенд продолжает работать в прежнем контейнере.

## 3. Заполнить настройки бэкенда

Создайте файл из шаблона, если его ещё нет:

```bash
cd /root/net-genius
test -f backend/.env || cp backend/deploy/vm.env.example backend/.env
chmod 600 backend/.env
nano backend/.env
```

`backend/.env` — настройки приложения. Он исключён из Git и Docker build context.
Пароли записывайте в одинарных кавычках, особенно если в них есть `$` или `#`.
Настоящий файл с паролями никому присылать не нужно.

| Настройка | Что указать |
|---|---|
| `JWT_SECRET` | Один раз сгенерировать `openssl rand -base64 64` и вставить одной строкой |
| `SMTP_USERNAME` | Почтовый ящик отправителя |
| `SMTP_PASSWORD` | Сгенерированный пароль приложения для почты |
| `SMTP_HOST`, `SMTP_PORT` | В шаблоне Яндекс, порт 465 с SSL; сверить с вашим провайдером |
| `SMTP_FROM` | Можно оставить пустым: используется `SMTP_USERNAME` |
| `FRONTEND_URL` | `https://www.simultech.ru`; сюда ведут ссылки из писем |
| `TEACHER_REQUEST_EMAIL` | Ящик получателя заявок преподавателей |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Ваши email и пароль первого администратора приложения |
| `PUBLIC_API_URL` | `/api`, адрес запросов Swagger на этом сайте |
| `FORWARD_HEADERS_STRATEGY` | `framework`, с nginx из шага 5 |
| `SEED_LABS` | `false`, демонстрационные лабораторные не добавляются |

JWT-ключ сохраняйте при следующих обновлениях. Его смена завершает действие
старых access-токенов. Новый слой авторизации также потребует повторного входа
для токенов, выданных старой версией без `uid`.

`ADMIN_EMAIL` и `ADMIN_PASSWORD` используются при создании администратора.
Существующий аккаунт не получает новый пароль или роль из `.env`. Для первой
настройки выберите email, ещё не занятый обычным пользователем, либо используйте
данные существующего администратора.

### Подключить файл к существующему Compose

В `/root/net-genius/docker-compose.yml` добавьте **только `env_file`** в сервис
`backend` рядом с `build` и `environment`:

```yaml
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    env_file:
      - ./backend/.env
    # Существующие environment, depends_on и restart оставьте на месте.
```

Существующие `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` и
`SPRING_DATASOURCE_PASSWORD` в `backend.environment` сохраняются. Они имеют
приоритет над настройками файла. Адрес остаётся
`jdbc:postgresql://database:5432/netgenius`.

В сервисе `database` сохраните прежние `POSTGRES_*` и подключение
`postgres_data:/var/lib/postgresql/data`. Значения пароля БД здесь и в бэкенде
уже согласованы; для первого обновления их не меняем. Смена переменной
`POSTGRES_PASSWORD` сама по себе не меняет пароль существующей БД.

`APP_BIND_ADDRESS`, `APP_PORT` и `POSTGRES_*` из standalone-шаблона на этой ВМ
добавлять не нужно. Новые сети, контейнер PostgreSQL и публикация порта 8080
тоже не нужны.

```bash
docker compose config --quiet
```

Это проверяет Compose, не печатая пароли. Файл `.env` сам по себе не передаётся
контейнеру: для этого и добавляется `env_file`.

## 4. Собрать бэкенд до замены работающего контейнера

Сначала сохраните текущий образ под отдельным именем и соберите новый:

```bash
cd /root/net-genius
docker image tag "$(docker compose images -q backend)" net-genius-backend:before-vm-deploy
docker compose build backend
```

Работающий контейнер от сборки не меняется. Ошибка сборки не требует перезапуска
ни БД, ни фронтенда, ни nginx.

Перед следующим шагом требуется проверить и обновить фронтенд. В опубликованной
сборке обнаружены обращения к `http://212.67.12.82:8080`, профиль с токеном
в query-параметре и изменение профиля через `PUT /auth/profile`.
Для сайта на HTTPS фронтенд должен обращаться к относительному `/api`,
передавать access-токен заголовком `Authorization: Bearer ...` и использовать
`PUT /users/profile` для редактирования профиля. Порт 8080 наружу не открываем.

Совместимые маршруты входа и регистрации помогают старому клиенту, но не
исправляют эти обращения фронтенда. До проверки пользовательских сценариев
обновление работающего контейнера не считаем готовым. Наличие `pong` этого
не проверяет. Письма также требуют существующих на фронте страниц
`/confirm-email?token=...` и `/reset-password?token=...`.

## 5. Обновить бэкенд и nginx

Новый бэкенд при старте обновляет схему существующей БД через Hibernate и
`db/profile-schema.sql`. Версионированных миграций пока нет. Это проверялось
на H2; первая проверка PostgreSQL выполняется на ВМ, поэтому резервная копия
из шага 1 обязательна.

Пересоздайте **только бэкенд**. Будет короткий перерыв API:

```bash
cd /root/net-genius
docker compose up -d --no-deps --no-build backend
docker compose logs --tail=100 backend
```

БД, frontend и nginx этой командой не пересоздаются. При ошибке старта не
продолжайте настройку наугад; сохраните текст ошибки без секретов.

Готовая конфигурация `backend/deploy/nginx.vm.conf` основана на присланном
конфиге ВМ. Она сохраняет домен, сертификаты и выдачу фронтенда, добавляет
передачу `/api-docs/swagger-config` в бэкенд, заголовки прокси и размер
multipart-запроса 6 МиБ. До замены сверьте её с актуальным файлом ВМ:

```bash
diff -u nginx/nginx.conf backend/deploy/nginx.vm.conf
```

У `diff` код 1 означает найденные различия. Если после присланной версии
на сервере добавляли другие маршруты, перенесите только нужные правки в
существующий конфиг. При использовании подготовленного файла записывайте его
в уже существующий `nginx/nginx.conf`, чтобы сохранить Docker bind mount:

```bash
cat backend/deploy/nginx.vm.conf > nginx/nginx.conf
docker compose exec -T nginx nginx -t
```

Только если `nginx -t` завершился успешно, перечитайте конфигурацию:

```bash
docker compose exec -T nginx nginx -s reload
```

Reload также заставляет nginx заново разрешить имя `backend` после замены
контейнера. `systemctl nginx` здесь не используется: nginx работает в Docker.

При ошибке `nginx -t` рабочий nginx продолжает использовать прежний конфиг.
Верните исходный файл из архива и повторите проверку. Не перезапускайте nginx
с невалидным файлом.

## 6. Проверить результат

```bash
cd /root/net-genius
docker compose ps
curl --fail --show-error https://www.simultech.ru/api/auth/ping
curl --fail --show-error https://www.simultech.ru/api-docs/swagger-config
curl --fail --show-error https://www.simultech.ru/api-docs
```

Ожидаются `pong`, JSON настроек Swagger и JSON схемы API. Swagger остаётся по
адресу `https://www.simultech.ru/swagger-ui/index.html`; сервер запросов — `/api`.
Прямой `curl http://127.0.0.1:8080/...` в этой схеме не подходит: порт бэкенда
не опубликован на ВМ.

Затем проверьте реальный сценарий: регистрация с фронтенда → письмо →
подтверждение email → вход → профиль. Отдельно проверьте письмо с документом
преподавателя и его одобрение администратором. Почта в тестах подменяется;
реальная доставка и фронтенд на ВМ автоматически не проверялись.

Логи нужных сервисов:

```bash
docker compose logs --tail=100 backend
docker compose logs --tail=100 nginx
```

## Последующие обновления и восстановление

После первой успешной настройки:

```bash
cd /root/net-genius
git -C backend pull --ff-only origin main
docker compose config --quiet
docker compose build backend
docker compose up -d --no-deps --no-build backend
docker compose exec -T nginx nginx -t
docker compose exec -T nginx nginx -s reload
```

Перед изменениями схемы БД делайте новый дамп. Изменения `.env` применяются
через `up -d --no-deps --no-build backend`, а не обычный `restart`.

Для восстановления есть старый код и конфиги в архиве, дамп БД и образ
`net-genius-backend:before-vm-deploy`. Откат контейнера не откатывает изменения
схемы автоматически; конкретные действия зависят от ошибки. Дамп не следует
восстанавливать поверх работающей БД без отдельного решения.

Не выполняйте `docker compose down -v`: ключ `-v` удаляет volume с данными.
Не меняйте имя проекта Compose и место его запуска: существующий
`postgres_data` должен остаться тем же.

# net-genius — backend (LMS + Network Simulator)

Backend системы дистанционного обучения с десктоп-симулятором сети. Реализует
контракт из спецификации [`LMS + Network Simulator.md`](LMS%20+%20Network%20Simulator.md):
роли и RBAC, подтверждение ролей, вузы и группы, приглашения, лицензии,
лабораторные работы, синхронизацию прогресса симулятора и отчётность.

## Стек

- Java 17, Spring Boot 3.4.2 (Web, Data JPA, Security, Mail, Validation)
- PostgreSQL (рантайм), H2 (тесты)
- JWT (access 15 мин) + refresh-токены в БД (30 дней)
- Argon2 (хеширование паролей, BouncyCastle)
- springdoc / Swagger UI

## Запуск

### Docker (рекомендуется)

```bash
docker compose up --build
```

Поднимает PostgreSQL и приложение на `http://localhost:8080`. Swagger UI —
`http://localhost:8080/swagger-ui.html`.

### Локально

Нужны JDK 17 и запущенный PostgreSQL (`netgenius`/`postgres`/`postgres` на 5432).

```bash
mvn spring-boot:run
# либо с локальным профилем (БД на localhost):
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

> На машинах, где JDK по умолчанию новее 17, используйте JDK 17 для сборки
> (см. `pom.xml`, `<java.version>17</java.version>`).

### Тесты

```bash
mvn test
```

Тесты выполняются на H2 in-memory (`@SpringBootTest`, MockMvc). `ApiSmokeTest`
проверяет регистрацию/логин/JWT и RBAC на трёх уровнях ролей.

## Учётные данные по умолчанию

При первом старте `DataInitializer` создаёт администратора и набор
демонстрационных лабораторных работ:

- **admin@netgenius.ru** / **Admin12345** (роль `ADMIN`)

Значения настраиваются в `application.properties` (`app.admin.*`, `app.seed.labs`).

## Роли и доступ (RBAC)

Одна роль на пользователя. Переход только `USER → STUDENT` или `USER → TEACHER`
через подтверждение администратором. Контроль доступа централизован по путям
(`SecurityConfig`):

| Префикс | Требуемая роль |
|---|---|
| `/auth/**` | публично (кроме `/auth/me`, `/auth/change-password` — требуют токен) |
| `GET /universities/**` | публично |
| `/admin/**` | `ADMIN` |
| `/teacher/**` | `TEACHER` |
| `/student/**`, `/sync/**` | `STUDENT` |
| прочее | аутентификация |

## Обзор API

- **Аутентификация** `/auth`: `register`, `login`, `refresh`, `logout`,
  `confirm-email`, `change-password`, `forgot-password`, `reset-password`, `me`
- **Пользователь** `/users`: `me`, `profile` (PUT), `email` (PUT)
- **Подтверждение ролей**: `/verification/{student,teacher,status}`,
  `/admin/verifications` (+ `{id}/approve`, `{id}/reject`)
- **Вузы**: `GET /universities`, `/teacher/university-request`,
  `/admin/university-requests` (approve/reject), `/admin/universities` (CRUD)
- **Группы**: `/teacher/groups` (CRUD), `/teacher/groups/{id}/invite`,
  `/teacher/groups/{id}/report`, `/teacher/students`
- **Приглашения**: `/student/invitations` (+ `{id}/accept`, `{id}/decline`)
- **Лицензии**: `/licenses/{activate,me,history}`, `/admin/licenses/{create,batch}`,
  `GET /admin/licenses`
- **Лабораторные**: `GET /labs`, `/labs/{id}`, `/labs/{id}/leaderboard`
- **Синхронизация**: `POST /sync/progress`

Полное описание — в Swagger UI.

## Ключевые бизнес-правила

- Email уникален; никнейм — нет. Подтверждение email требуется для чувствительных
  операций (заявки на роль, активация лицензии, синхронизация, работа с группами).
- Лицензия действует 365 дней; ключ активируется один раз (с блокировкой от гонок);
  для синхронизации симулятора требуется действующая лицензия.
- Приглашение в группу действует 7 дней; просроченные помечаются `EXPIRED`.
- Основная оценка = лучшая попытка (`best_score`); число попыток не ограничено.
- Оффлайн-события старше 14 дней при синхронизации отбрасываются.
- Студент принадлежит одному вузу; группа всегда принадлежит вузу и преподавателю.

## Безопасность

JWT + refresh-токены (ротация и отзыв при logout), Argon2, RBAC, обязательное
подтверждение email, аудит-логирование изменений (`audit_log`). HTTPS
обеспечивается на уровне reverse-proxy (nginx) при развёртывании.

## Схема БД

Управляется Hibernate (`ddl-auto=update`), включая обязательные индексы из
спецификации (через `@Table(indexes=...)`). `init.sql` намеренно не создаёт
таблицы, чтобы избежать рассогласования схемы.

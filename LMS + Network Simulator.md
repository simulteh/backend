

LMS + Network Simulator
Backend Architecture & API Contract Specification v1.0
- Общая информация
Система состоит из двух компонентов:
LMS Web Platform
## Назначение:
регистрация пользователей;
подтверждение ролей;
управление лицензиями;
управление вузами;
управление группами;
управление студентами;
просмотр прогресса;
просмотр оценок.
## Desktop Simulator
Поддерживаемые платформы:
## Windows
## Linux
## Назначение:
выполнение лабораторных работ;
прохождение уровней;
оффлайн работа;
синхронизация прогресса;
автоматическая оценка.
## 2. Роли
## Guest
Неавторизованный пользователь.
## •
## •
## •
## •
## •
## •
## •
## •
## •
## •
## •
## •
## •
## •
## •
## 1

## Доступ:
регистрация;
авторизация;
просмотр публичных страниц.
## User
Зарегистрированный пользователь.
## Доступ:
покупка лицензии;
активация лицензии;
смена пароля;
подача заявок на роли.
## Student
## Дополнительно:
просмотр оценок;
просмотр прогресса;
принятие приглашений в группы;
отправка заявок на смену вуза.
## Teacher
## Дополнительно:
создание групп;
просмотр студентов;
просмотр оценок;
приглашение студентов;
удаление студентов из групп;
подача заявки на создание вуза.
## Admin
Полный доступ.
- Основные бизнес-правила
Один пользователь имеет одну роль.
Пользователь может перейти из USER в STUDENT или TEACHER.
Одновременно быть студентом и преподавателем запрещено.
Email должен быть подтвержден.
Пользователь не может удалить аккаунт самостоятельно.
## •
## •
## •
## •
## •
## •
## •
## •
## •
## •
## •
## •
## •
## •
## •
## •
## •
## 1.
## 2.
## 3.
## 4.
## 5.
## 2

Nickname не обязан быть уникальным.
Email всегда уникален.
Группа всегда принадлежит вузу.
Группа всегда принадлежит преподавателю.
Студент может состоять в нескольких группах.
Студент принадлежит только одному вузу.
Лицензия действует 365 дней.
Один лицензионный ключ может быть активирован только один раз.
После окончания лицензии вход в симулятор запрещается.
Оффлайн работа ограничена 14 днями.
Приглашение в группу действует 7 дней.
Основной оценкой считается лучшая попытка.
Количество попыток не ограничено.
- База данных
users
user_id PK
email UNIQUE
nickname
password_hash
email_confirmed
role
created_at
updated_at
students
student_id PK
user_id FK
full_name
birth_year
phone
## 6.
## 7.
## 8.
## 9.
## 10.
## 11.
## 12.
## 13.
## 14.
## 15.
## 16.
## 17.
## 18.
## 3

university_id FK
faculty
speciality
direction
course
degree
verified
created_at
teachers
teacher_id PK
user_id FK
full_name
phone
workplace
position
verified
created_at
universities
university_id PK
name
faculty
speciality
direction
## 4

created_by_admin
created_at
university_creation_requests
request_id PK
teacher_id FK
university_name
faculty
speciality
direction
status
created_at
reviewed_at
reviewed_by
groups
group_id PK
teacher_id FK
university_id FK
group_name
created_at
student_groups
student_id FK
group_id FK
joined_at
## 5

PRIMARY KEY(student_id, group_id)
labs
lab_id PK
title
description
sort_order
is_active
created_at
student_lab_progress
student_id FK
lab_id FK
status
best_score
attempts_count
first_started_at
last_completed_at
updated_at
UNIQUE(student_id, lab_id)
lab_attempts
attempt_id PK
student_id FK
lab_id FK
started_at
## 6

completed_at
score
status
client_version
created_at
licenses
license_id PK
license_key
owner_user_id
issued_by_admin
activated_at
expires_at
is_active
license_batches
batch_id PK
university_id FK
quantity
created_by_admin
created_at
license_activation_log
log_id PK
license_id
user_id
## 7

action
created_at
verification_requests
request_id PK
user_id
request_type
status
comment
created_at
reviewed_at
reviewed_by
verification_documents
document_id PK
request_id
file_url
document_type
uploaded_at
group_invitations
invitation_id PK
teacher_id
group_id
student_id
email
## 8

invite_token
status
expires_at
created_at
university_change_requests
request_id PK
student_id
new_university_id
status
document_id
created_at
reviewed_at
reviewed_by
audit_log
audit_id PK
actor_user_id
entity_type
entity_id
action
old_value
new_value
created_at
## 9

## 5. Статусы
## Lab Status
## NOT_STARTED
## STARTED
## COMPLETED
## Invitation Status
## PENDING
## ACCEPTED
## DECLINED
## EXPIRED
## Verification Status
## PENDING
## APPROVED
## REJECTED
## 6. Авторизация
JWT Access Token
Время жизни:
15 минут
## Refresh Token
Время жизни:
30 дней
## Endpoints:
POST /auth/register
## 10

POST /auth/login
POST /auth/refresh
POST /auth/logout
POST /auth/confirm-email
POST /auth/change-password
POST /auth/forgot-password
POST /auth/reset-password
GET /auth/me
- Управление пользователями
GET /users/me
PUT /users/profile
PUT /users/email
- Подтверждение ролей
POST /verification/student
POST /verification/teacher
GET /verification/status
GET /admin/verifications
POST /admin/verifications/{id}/approve
POST /admin/verifications/{id}/reject
## 9. Университеты
GET /universities
GET /universities/{id}
## 11

POST /teacher/university-request
GET /admin/university-requests
POST /admin/university-requests/{id}/approve
POST /admin/university-requests/{id}/reject
POST /admin/universities
PUT /admin/universities/{id}
DELETE /admin/universities/{id}
## 10. Группы
POST /teacher/groups
GET /teacher/groups
GET /teacher/groups/{id}
PUT /teacher/groups/{id}
DELETE /teacher/groups/{id}
## 11. Приглашения
POST /teacher/groups/{id}/invite
GET /student/invitations
POST /student/invitations/{id}/accept
POST /student/invitations/{id}/decline
## 12. Студенты
GET /teacher/students
GET /teacher/students/{id}
DELETE /teacher/groups/{groupId}/students/{studentId}
## 12

## 13. Лицензии
POST /licenses/activate
GET /licenses/me
GET /licenses/history
POST /admin/licenses/create
POST /admin/licenses/batch
GET /admin/licenses
## 14. Лабораторные
GET /labs
GET /labs/{id}
GET /labs/{id}/leaderboard
- Синхронизация прогресса
POST /sync/progress
## Request:
{ device_id, events[] }
## Event:
{ lab_id, status, started_at, completed_at, score }
- Отчет преподавателя
GET /teacher/groups/{id}/report
## Ответ:
## Студент
## 13

## Лаба 1
## Лаба 2
## Лаба 3
## ...
## Лаба N
Для каждой лабы отображается:
лучшая оценка;
либо статус;
либо дата завершения.
## 17. Масштабирование
Расчетная нагрузка:
ВУЗы: до 200
Группы: до 60 000
Студенты: до 600 000
Преподаватели: до 60 000
Одновременные пользователи LMS: до 10 000
Требуемая СУБД:
PostgreSQL
Обязательные индексы:
users(email)
students(university_id)
student_groups(student_id)
student_groups(group_id)
groups(teacher_id)
lab_attempts(student_id)
## •
## •
## •
## 14

lab_attempts(lab_id)
licenses(owner_user_id)
verification_requests(user_id)
group_invitations(student_id)
audit_log(actor_user_id)
## 18. Безопасность
JWT Authentication
## Refresh Tokens
Password Hashing (Argon2)
HTTPS Only
## Email Verification Required
## Audit Logging Required
Role Based Access Control (RBAC)
## License Validation Required For Desktop Login
## Offline Access Limit = 14 Days
## 15
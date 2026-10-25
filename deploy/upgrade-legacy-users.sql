-- Ручная миграция существующей users перед запуском новой версии приложения.
-- Автоматически приложением не запускается; выполнение согласуется с пользователем.
-- Запускать через psql с ON_ERROR_STOP=1; при ошибке транзакция не должна продолжаться.
-- Существующие роли, учётные данные и остальные столбцы не изменяются.
BEGIN;

ALTER TABLE users ADD COLUMN IF NOT EXISTS role varchar(20) DEFAULT 'USER';
UPDATE users SET role = 'USER' WHERE role IS NULL;
ALTER TABLE users ALTER COLUMN role SET DEFAULT 'USER';
ALTER TABLE users ALTER COLUMN role SET NOT NULL;

COMMIT;

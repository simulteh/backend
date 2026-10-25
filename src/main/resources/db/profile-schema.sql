-- Ранее группы LMS обязательно ссылались на справочник вузов.
-- Новый контракт /groups принимает произвольное юридическое название организации.
-- Выполняется до Hibernate, в том числе для существующей PostgreSQL-базы; повторный запуск безопасен.
ALTER TABLE IF EXISTS groups ALTER COLUMN university_id DROP NOT NULL;

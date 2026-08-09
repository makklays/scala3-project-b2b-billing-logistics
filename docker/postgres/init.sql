
-- Создание пользователя и базы данных для нашей платформы
CREATE USER techmatrix_user WITH PASSWORD 'secure_db_pass_2026';
CREATE DATABASE billing_logistics_db OWNER techmatrix_user;
GRANT ALL PRIVILEGES ON DATABASE billing_logistics_db TO techmatrix_user;


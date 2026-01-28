-- 1. 显式创建数据库
CREATE DATABASE IF NOT EXISTS medusa;
USE medusa;

-- 2. 创建主应用用户并授权（保持不变）
CREATE USER IF NOT EXISTS '${DB_USER}'@'%' IDENTIFIED BY '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON medusa.* TO '${DB_USER}'@'%';

-- 3. 刷新权限（统一执行一次）
FLUSH PRIVILEGES;

-- SHOW GRANTS FOR 'flyway_user'@'%';

-- 4. 设置字符集（保持不变）
ALTER DATABASE medusa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

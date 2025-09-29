-- 统一字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET collation_connection = utf8mb4_0900_ai_ci;

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `openisle`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

-- 创建业务账号（容器会基于环境变量自动创建；此处再兜底 + 幂等）
CREATE USER IF NOT EXISTS 'openisle'@'%' IDENTIFIED BY 'openisle';
GRANT ALL PRIVILEGES ON `openisle`.* TO 'openisle'@'%';
FLUSH PRIVILEGES;

-- 切换到目标库
USE `openisle`;

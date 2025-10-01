SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET collation_connection = utf8mb4_0900_ai_ci;

CREATE DATABASE IF NOT EXISTS `openisle`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'openisle'@'%' IDENTIFIED BY 'openisle';
GRANT ALL PRIVILEGES ON `openisle`.* TO 'openisle'@'%';
FLUSH PRIVILEGES;

USE `openisle`;

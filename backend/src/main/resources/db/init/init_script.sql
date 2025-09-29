-- 2025-09-02
-- 本地化开发，初始化脚本
-- 抽奖的时候奖品图片是必须的，把相关代码注释掉即可跳过check

-- 设置字符集和排序规则
SET NAMES utf8;
SET CHARACTER SET utf8;
SET collation_connection = utf8_general_ci;

-- 创建 users 表（如果不存在）
CREATE TABLE IF NOT EXISTS `users` (
	`id` bigint NOT NULL AUTO_INCREMENT,
	`approved` bit(1) DEFAULT NULL,
	`avatar` varchar(255) DEFAULT NULL,
	`created_at` datetime(6) DEFAULT NULL,
	`display_medal` varchar(255) DEFAULT NULL,
	`email` varchar(255) NOT NULL,
	`experience` int DEFAULT NULL,
	`introduction` text,
	`password` varchar(255) NOT NULL,
	`password_reset_code` varchar(255) DEFAULT NULL,
	`point` int DEFAULT NULL,
	`register_reason` text,
	`role` varchar(20) DEFAULT 'USER',
	`username` varchar(50) NOT NULL,
	`verification_code` varchar(255) DEFAULT NULL,
	`verified` bit(1) DEFAULT NULL,
	PRIMARY KEY (`id`),
	UNIQUE KEY `UK_users_email` (`email`),
	UNIQUE KEY `UK_users_username` (`username`)
);

-- 清空users表
DELETE FROM `users`;
-- 插入用户，两个普通用户，一个管理员
-- username:admin/user1/user2 password:123321
INSERT INTO `users` (`id`, `approved`, `avatar`, `created_at`, `display_medal`, `email`, `experience`, `introduction`, `password`, `password_reset_code`, `point`, `register_reason`, `role`, `username`, `verification_code`, `verified`) VALUES
	(1, b'1', '', '2025-09-01 16:08:17.426430', 'PIONEER', 'adminmail@openisle.com', 70, NULL, '$2a$10$dux.NXwW09cCsdZ05BgcnOtxVqqjcmnbj3.8xcxGl/iiIlv06y7Oe', NULL, 110, '测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试', 'ADMIN', 'admin', NULL, b'1'),
	(2, b'1', '', '2025-09-03 16:08:17.426430', 'PIONEER', 'usermail2@openisle.com', 70, NULL, '$2a$10$dux.NXwW09cCsdZ05BgcnOtxVqqjcmnbj3.8xcxGl/iiIlv06y7Oe', NULL, 110, '测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试', 'USER', 'user1', NULL, b'1'),
	(3, b'1', '', '2025-09-02 17:21:21.617666', 'PIONEER', 'usermail1@openisle.com', 40, NULL, '$2a$10$dux.NXwW09cCsdZ05BgcnOtxVqqjcmnbj3.8xcxGl/iiIlv06y7Oe', NULL, 40, '测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试', 'USER', 'user2', NULL, b'1');

-- 创建 tags 表（如果不存在）
CREATE TABLE IF NOT EXISTS `tags` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `approved` bit(1) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text,
  `icon` varchar(255) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `small_icon` varchar(255) DEFAULT NULL,
  `creator_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_tags_name` (`name`),
  KEY `FK_tags_creator` (`creator_id`),
  CONSTRAINT `FK_tags_creator` FOREIGN KEY (`creator_id`) REFERENCES `users` (`id`)
);
-- 清空tags表
DELETE FROM `tags`;
-- 插入标签，三个测试用标签
INSERT INTO `tags` (`id`, `approved`, `created_at`, `description`, `icon`, `name`, `small_icon`, `creator_id`) VALUES
	(1, b'1', '2025-09-02 10:51:56.000000', '测试用标签1', NULL, '测试用标签1', NULL, NULL),
	(2, b'1', '2025-09-02 10:51:56.000000', '测试用标签2', NULL, '测试用标签2', NULL, NULL),
	(3, b'1', '2025-09-02 10:51:56.000000', '测试用标签3', NULL, '测试用标签3', NULL, NULL);

-- 创建 categories 表（如果不存在）
CREATE TABLE IF NOT EXISTS `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` text,
  `icon` varchar(255) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `small_icon` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_categories_name` (`name`)
);
-- 清空categories表
DELETE FROM `categories`;
-- 插入分类，三个测试用分类
INSERT INTO `categories` (`id`, `description`, `icon`, `name`, `small_icon`) VALUES
	(1, '测试用分类1', '1', '测试用分类1', NULL),
	(2, '测试用分类2', '2', '测试用分类2', NULL),
	(3, '测试用分类3', '3', '测试用分类3', NULL);
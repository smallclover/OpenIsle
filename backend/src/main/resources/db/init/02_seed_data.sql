USE `openisle`;
SET FOREIGN_KEY_CHECKS = 0;

-- 清空并灌入演示数据（幂等处理：先删再插）
DELETE FROM `tags`;
DELETE FROM `categories`;
DELETE FROM `users`;

-- users（密码已是 bcrypt；账号：admin/user1/user2，明文 123321）
INSERT INTO `users`
(`id`,`approved`,`avatar`,`created_at`,`display_medal`,`email`,`experience`,`introduction`,
 `password`,`password_reset_code`,`point`,`register_reason`,`role`,`username`,`verification_code`,`verified`)
VALUES
(1, b'1', '', '2025-09-01 16:08:17.426430', 'PIONEER', 'adminmail@openisle.com', 70, NULL,
 '$2a$10$dux.NXwW09cCsdZ05BgcnOtxVqqjcmnbj3.8xcxGl/iiIlv06y7Oe', NULL, 110,
 '测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试', 'ADMIN', 'admin', NULL, b'1'),
(2, b'1', '', '2025-09-03 16:08:17.426430', 'PIONEER', 'usermail2@openisle.com', 70, NULL,
 '$2a$10$dux.NXwW09cCsdZ05BgcnOtxVqqjcmnbj3.8xcxGl/iiIlv06y7Oe', NULL, 110,
 '测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试', 'USER', 'user1', NULL, b'1'),
(3, b'1', '', '2025-09-02 17:21:21.617666', 'PIONEER', 'usermail1@openisle.com', 40, NULL,
 '$2a$10$dux.NXwW09cCsdZ05BgcnOtxVqqjcmnbj3.8xcxGl/iiIlv06y7Oe', NULL, 40,
 '测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试', 'USER', 'user2', NULL, b'1');

-- categories
INSERT INTO `categories` (`id`,`description`,`icon`,`name`,`small_icon`) VALUES
(1,'测试用分类1','1','测试用分类1',NULL),
(2,'测试用分类2','2','测试用分类2',NULL),
(3,'测试用分类3','3','测试用分类3',NULL);

-- tags（此处不绑定 creator_id，避免外键顺序依赖；如需可填 1/2/3）
INSERT INTO `tags` (`id`,`approved`,`created_at`,`description`,`icon`,`name`,`small_icon`,`creator_id`) VALUES
(1,b'1','2025-09-02 10:51:56.000000','测试用标签1',NULL,'测试用标签1',NULL,NULL),
(2,b'1','2025-09-02 10:51:56.000000','测试用标签2',NULL,'测试用标签2',NULL,NULL),
(3,b'1','2025-09-02 10:51:56.000000','测试用标签3',NULL,'测试用标签3',NULL,NULL);

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE IF NOT EXISTS post_reads (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  post_id BIGINT NOT NULL,
  last_read_at DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK_post_reads_user_post (user_id, post_id),
  KEY IDX_post_reads_user (user_id),
  KEY IDX_post_reads_post (post_id),
  CONSTRAINT FK_post_reads_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT FK_post_reads_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE
);

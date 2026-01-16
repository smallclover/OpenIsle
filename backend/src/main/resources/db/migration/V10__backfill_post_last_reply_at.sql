-- Backfill last_reply_at for posts without comments to preserve latest-reply ordering
UPDATE posts
SET last_reply_at = created_at
WHERE last_reply_at IS NULL;

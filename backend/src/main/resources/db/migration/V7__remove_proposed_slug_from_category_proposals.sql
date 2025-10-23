ALTER TABLE category_proposal_posts
    DROP INDEX idx_category_proposal_posts_slug;

ALTER TABLE category_proposal_posts
    DROP COLUMN proposed_slug;

CREATE UNIQUE INDEX IF NOT EXISTS idx_category_proposal_posts_name
    ON category_proposal_posts (proposed_name);

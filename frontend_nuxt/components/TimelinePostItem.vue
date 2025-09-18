<template>
  <div class="timeline-container">
    <div class="timeline-header">
      <div class="timeline-title">发布了文章</div>
      <div class="timeline-date">{{ formattedDate }}</div>
    </div>
    <div class="article-container">
      <NuxtLink :to="postLink" class="timeline-article-link">
        {{ item.post?.title }}
      </NuxtLink>
      <div class="timeline-snippet">
        {{ strippedSnippet }}
      </div>
      <div class="article-meta" v-if="hasMeta">
        <ArticleCategory v-if="item.post?.category" :category="item.post.category" />
        <div class="article-tags" v-if="(item.post?.tags?.length ?? 0) > 0">
          <span class="article-tag" v-for="tag in item.post?.tags" :key="tag.id || tag.name">
            #{{ tag.name }}
          </span>
        </div>
        <div class="article-comment-count" v-if="item.post?.commentCount !== undefined">
          <comment-one class="article-comment-count-icon" />
          <span>{{ item.post?.commentCount }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import ArticleCategory from '~/components/ArticleCategory.vue'
import { stripMarkdown } from '~/utils/markdown'
import TimeManager from '~/utils/time'

const props = defineProps({
  item: { type: Object, required: true },
})

const postLink = computed(() => {
  const id = props.item.post?.id
  return id ? `/posts/${id}` : '#'
})

const formattedDate = computed(() => TimeManager.format(props.item.createdAt))
const strippedSnippet = computed(() => stripMarkdown(props.item.post?.snippet ?? ''))
const hasMeta = computed(() => {
  const tags = props.item.post?.tags ?? []
  const hasTags = Array.isArray(tags) && tags.length > 0
  const hasCategory = !!props.item.post?.category
  const hasCommentCount =
    props.item.post?.commentCount !== undefined && props.item.post?.commentCount !== null
  return hasTags || hasCategory || hasCommentCount
})
</script>

<style scoped>
.timeline-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  background: var(--timeline-card-background, transparent);
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.timeline-title {
  font-size: 16px;
  font-weight: 600;
}

.timeline-date {
  font-size: 12px;
  color: var(--timeline-date-color, #888);
}

.article-container {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.timeline-article-link {
  font-size: 18px;
  font-weight: 600;
  color: var(--link-color);
  text-decoration: none;
}

.timeline-article-link:hover {
  text-decoration: underline;
}

.timeline-snippet {
  color: var(--timeline-snippet-color, #666);
  font-size: 14px;
  line-height: 1.6;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.article-tag {
  background-color: var(--article-info-background-color);
  border-radius: 6px;
  padding: 2px 6px;
  font-size: 12px;
  color: var(--text-color);
}

.article-comment-count {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-color);
}

.article-comment-count-icon {
  width: 16px;
  height: 16px;
}

@media (max-width: 768px) {
  .timeline-article-link {
    font-size: 16px;
  }

  .timeline-snippet {
    font-size: 13px;
  }
}
</style>

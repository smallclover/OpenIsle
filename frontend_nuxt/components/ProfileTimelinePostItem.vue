<template>
  <div class="timeline-container">
    <div class="timeline-header">
      <div class="timeline-title">发布了文章</div>
      <div class="timeline-date">{{ formattedDate }}</div>
    </div>
    <div class="article-container">
      <NuxtLink :to="postLink" class="timeline-article-link">
        {{ props.item.post.title }}
      </NuxtLink>
      <div class="timeline-snippet">
        {{ postSnippet }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { stripMarkdown } from '~/utils/markdown'
import TimeManager from '~/utils/time'

const props = defineProps({
  item: {
    type: Object,
    required: true,
  },
})

const formattedDate = computed(() => TimeManager.format(props.item.createdAt))
const postLink = computed(() => `/posts/${props.item.post.id}`)
const postSnippet = computed(() => stripMarkdown(props.item.post?.snippet ?? ''))
</script>

<style scoped>
.timeline-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.timeline-title {
  font-weight: 600;
  color: var(--primary-color);
}

.timeline-date {
  color: var(--text-color-secondary);
  font-size: 14px;
}

.article-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.timeline-article-link {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color);
}

.timeline-article-link:hover {
  color: var(--primary-color);
}

.timeline-snippet {
  color: var(--text-color-secondary);
  font-size: 14px;
  line-height: 1.6;
}
</style>

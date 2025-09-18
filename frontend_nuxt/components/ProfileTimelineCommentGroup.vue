<template>
  <div class="timeline-container">
    <div class="timeline-header">
      <div class="timeline-title">{{ headerText }}</div>
      <div class="timeline-date">{{ headerDate }}</div>
    </div>
    <div class="comment-content">
      <div v-for="entry in entries" :key="entry.comment.id" class="comment-content-item">
        <div class="comment-content-item-main">
          <comment-one class="comment-content-item-icon" />
          <template v-if="!entry.comment.parentComment">
            <span class="comment-prefix">
              在
              <NuxtLink :to="entry.postLink" class="timeline-link">
                {{ entry.comment.post.title }}
              </NuxtLink>
              下评论了
            </span>
            <NuxtLink :to="entry.commentLink" class="timeline-comment-link">
              {{ stripContent(entry.comment.content) }}
            </NuxtLink>
          </template>
          <template v-else>
            <span class="comment-prefix">
              在
              <NuxtLink :to="entry.postLink" class="timeline-link">
                {{ entry.comment.post.title }}
              </NuxtLink>
              下对
              <NuxtLink :to="entry.parentLink" class="timeline-link">
                {{ stripContent(entry.comment.parentComment.content) }}
              </NuxtLink>
              回复了
            </span>
            <NuxtLink :to="entry.commentLink" class="timeline-comment-link">
              {{ stripContent(entry.comment.content) }}
            </NuxtLink>
          </template>
        </div>
        <div class="timeline-date">{{ formatDate(entry.createdAt) }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { stripMarkdownLength } from '~/utils/markdown'
import TimeManager from '~/utils/time'

const props = defineProps({
  item: {
    type: Object,
    required: true,
  },
})

const entries = computed(() =>
  (props.item.entries || []).map((entry) => ({
    ...entry,
    postLink: `/posts/${entry.comment.post.id}`,
    commentLink: `/posts/${entry.comment.post.id}#comment-${entry.comment.id}`,
    parentLink: entry.comment.parentComment
      ? `/posts/${entry.comment.post.id}#comment-${entry.comment.parentComment.id}`
      : undefined,
  })),
)

const commentCount = computed(
  () => entries.value.filter((entry) => !entry.comment.parentComment).length,
)

const replyCount = computed(
  () => entries.value.filter((entry) => entry.comment.parentComment).length,
)

const headerText = computed(() => {
  if (commentCount.value && replyCount.value) {
    return `发布了${commentCount.value}条评论和${replyCount.value}条回复`
  }
  if (commentCount.value) {
    return `发布了${commentCount.value}条评论`
  }
  if (replyCount.value) {
    return `发布了${replyCount.value}条回复`
  }
  return '发布了评论'
})

const headerDate = computed(() => TimeManager.format(props.item.createdAt))

const formatDate = (date) => TimeManager.format(date)

const stripContent = (content) => stripMarkdownLength(content || '', 200)
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

.comment-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-content-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.comment-content-item-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.comment-content-item-icon {
  color: var(--primary-color);
  font-size: 18px;
}

.comment-prefix {
  color: var(--text-color-secondary);
  font-size: 14px;
}

.timeline-comment-link {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  color: var(--text-color);
  font-size: 15px;
  line-height: 1.6;
}

.timeline-comment-link:hover {
  color: var(--primary-color);
}

.timeline-link {
  color: var(--primary-color);
}

.timeline-link:hover {
  color: var(--primary-color-hover);
}
</style>

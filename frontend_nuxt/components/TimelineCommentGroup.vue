<template>
  <div class="timeline-container">
    <div class="timeline-header">
      <div class="timeline-title">{{ headerText }}</div>
      <div class="timeline-date">{{ formattedDate }}</div>
    </div>
    <div class="comment-content" v-if="entries.length > 0">
      <div class="comment-content-item" v-for="entry in entries" :key="entry.comment.id">
        <div class="comment-content-item-main">
          <comment-one class="comment-content-item-icon" />
          <div class="comment-content-item-text">
            <span class="comment-content-item-prefix">
              在
              <NuxtLink :to="`/posts/${entry.comment.post.id}`" class="timeline-link">
                {{ entry.comment.post.title }}
              </NuxtLink>
              <template v-if="entry.comment.parentComment">
                下对
                <NuxtLink
                  :to="`/posts/${entry.comment.post.id}#comment-${entry.comment.parentComment.id}`"
                  class="timeline-link"
                >
                  {{ parentSnippet(entry) }}
                </NuxtLink>
                回复了
              </template>
              <template v-else> 下评论了 </template>
            </span>
            <NuxtLink
              :to="`/posts/${entry.comment.post.id}#comment-${entry.comment.id}`"
              class="timeline-comment-link"
            >
              {{ stripContent(entry.comment.content) }}
            </NuxtLink>
          </div>
        </div>
        <div class="timeline-date">{{ formatEntryDate(entry.createdAt) }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { stripMarkdownLength } from '~/utils/markdown'
import TimeManager from '~/utils/time'

const props = defineProps({
  item: { type: Object, required: true },
})

const entries = computed(() => {
  if (Array.isArray(props.item.entries)) {
    return props.item.entries
  }
  if (props.item.comment) {
    return [
      {
        type: props.item.type,
        comment: props.item.comment,
        createdAt: props.item.createdAt,
      },
    ]
  }
  return []
})

const formattedDate = computed(() => TimeManager.format(props.item.createdAt))

const hasReplies = computed(() => entries.value.some((entry) => !!entry.comment.parentComment))
const hasComments = computed(() => entries.value.some((entry) => !entry.comment.parentComment))

const headerText = computed(() => {
  const count = entries.value.length
  if (count === 0) return ''
  if (hasComments.value && hasReplies.value) {
    return `发布了${count}条评论/回复`
  }
  if (hasReplies.value) {
    return `发布了${count}条回复`
  }
  return `发布了${count}条评论`
})

const formatEntryDate = (date) => TimeManager.format(date)
const stripContent = (content) => stripMarkdownLength(content ?? '', 200)
const parentSnippet = (entry) =>
  stripMarkdownLength(entry.comment.parentComment?.content ?? '', 200)
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

.comment-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-content-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-bottom: 6px;
  border-bottom: 1px solid var(--comment-item-border, rgba(0, 0, 0, 0.05));
}

.comment-content-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.comment-content-item-main {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.comment-content-item-icon {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  margin-top: 2px;
}

.comment-content-item-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.comment-content-item-prefix {
  font-size: 14px;
  color: var(--text-color);
}

.timeline-comment-link {
  font-size: 14px;
  color: var(--link-color);
  text-decoration: none;
}

.timeline-comment-link:hover {
  text-decoration: underline;
}

.timeline-link {
  color: var(--link-color);
  text-decoration: none;
}

.timeline-link:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .comment-content-item-prefix,
  .timeline-comment-link {
    font-size: 13px;
  }
}
</style>

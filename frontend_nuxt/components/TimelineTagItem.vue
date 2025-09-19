<template>
  <div class="timeline-tag-item">
    <template v-if="mode === 'timeline'">
      <div class="tags-container">
        <div class="tags-container-item">
          <div class="timeline-tag-title">{{ title }}</div>
          <ArticleTags v-if="tag" :tags="[tag]" />
        </div>
        <div v-if="timelineDate" class="timeline-date">{{ timelineDate }}</div>
      </div>
      <div v-if="hasDescription" class="timeline-snippet">
        {{ tag?.description }}
      </div>
    </template>
    <template v-else>
      <span class="timeline-link" :class="{ clickable: isClickable }" @click="handleTagClick">
        {{ tag?.name }}<span v-if="tag?.count"> x{{ tag.count }}</span>
      </span>
      <div v-if="hasDescription" class="timeline-snippet">
        {{ tag?.description }}
      </div>
      <div v-if="summaryDate" class="timeline-date">{{ summaryDate }}</div>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import TimeManager from '~/utils/time'

const props = defineProps({
  item: { type: Object, required: true },
  mode: {
    type: String,
    default: 'timeline',
    validator: (value) => ['timeline', 'summary'].includes(value),
  },
  title: {
    type: String,
    default: '创建了标签',
  },
})

const emit = defineEmits(['tag-click'])

const tag = computed(() => props.item?.tag ?? null)
const hasDescription = computed(() => {
  const description = tag.value?.description
  return !!description
})

const timelineDate = computed(() => {
  const date = props.item?.createdAt ?? tag.value?.createdAt
  return date ? TimeManager.format(date) : ''
})

const summaryDate = computed(() => {
  const date = tag.value?.createdAt ?? props.item?.createdAt
  return date ? TimeManager.format(date) : ''
})

const isClickable = computed(() => props.mode === 'summary' && !!tag.value)

const handleTagClick = () => {
  if (!isClickable.value) return
  emit('tag-click', tag.value)
}
</script>

<style scoped>
.timeline-tag-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.tags-container {
  display: flex;
  flex-direction: row;
  gap: 10px;
  padding-top: 5px;
  justify-content: space-between;
  align-items: center;
}

.tags-container-item {
  display: flex;
  flex-direction: row;
  gap: 5px;
  align-items: center;
}

.timeline-tag-title {
  font-size: 16px;
  font-weight: 600;
}

.timeline-date {
  font-size: 12px;
  color: gray;
  margin-top: 5px;
  white-space: nowrap;
}

.timeline-snippet {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

.timeline-link {
  font-weight: bold;
  color: var(--primary-color);
  text-decoration: none;
  word-break: break-word;
  cursor: default;
}

.timeline-link.clickable {
  cursor: pointer;
}

.timeline-link.clickable:hover {
  text-decoration: underline;
}
</style>

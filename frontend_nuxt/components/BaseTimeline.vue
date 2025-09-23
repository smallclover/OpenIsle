<template>
  <div class="timeline">
    <div class="timeline-item" v-for="(item, idx) in items" :key="idx">
      <div
        class="timeline-icon"
        :class="{ clickable: !!item.iconClick || hasLink(item) }"
        @click="onIconClick(item, $event)"
      >
        <BaseUserAvatar
          v-if="item.src"
          :src="item.src"
          :user-id="item.userId"
          :to="item.avatarLink"
          class="timeline-img"
          alt="timeline item"
          :disable-link="!hasLink(item) || !!item.iconClick"
        />
        <component
          v-else-if="item.icon && (typeof item.icon !== 'string' || !item.icon.includes(' '))"
          :is="item.icon"
          :size="20"
        />
        <BaseImage v-else-if="item.emoji" :src="item.emoji" class="timeline-emoji" alt="emoji" />
      </div>
      <div class="timeline-content">
        <slot name="item" :item="item">{{ item.content }}</slot>
      </div>
    </div>
  </div>
</template>

<script>
import BaseUserAvatar from '~/components/BaseUserAvatar.vue'

export default {
  name: 'BaseTimeline',
  components: { BaseUserAvatar },
  props: {
    items: { type: Array, default: () => [] },
  },
  methods: {
    hasLink(item) {
      if (!item) return false
      if (item.avatarLink) return true
      const id = item?.userId
      return id !== undefined && id !== null && id !== ''
    },
    onIconClick(item, event) {
      if (item && item.iconClick) {
        event.preventDefault()
        item.iconClick()
      }
    },
  },
}
</script>

<style scoped>
.timeline {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.timeline-item {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  position: relative;
  margin-top: 10px;
}

.timeline-icon {
  position: sticky;
  top: 0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  color: var(--text-color);
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 10px;
  flex-shrink: 0;
}

.timeline-icon.clickable {
  cursor: pointer;
}

.timeline-img {
  width: 100%;
  height: 100%;
}

.timeline-img :deep(.base-user-avatar-img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.timeline-emoji {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

.timeline-item::before {
  content: '';
  position: absolute;
  top: 32px;
  left: 15px;
  width: 2px;
  bottom: -20px;
  background: var(--text-color);
  opacity: 0.08;
}

.timeline-item:last-child::before {
  bottom: 0px;
}

.timeline-content {
  flex: 1;
  width: calc(100% - 42px);
}

@media (max-width: 768px) {
  .timeline-icon {
    margin-right: 2px;
    width: 30px;
    height: 30px;
  }
}
</style>

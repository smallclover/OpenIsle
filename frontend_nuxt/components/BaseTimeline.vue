<template>
  <div class="timeline">
    <div class="timeline-item" v-for="(item, idx) in items" :key="idx">
      <div
        class="timeline-icon"
        :class="{ clickable: !!item.iconClick && !item.src }"
        @click="!item.src && item.iconClick && item.iconClick()"
      >
        <BaseUserAvatar
          v-if="item.src"
          :class="['timeline-img', { 'is-clickable': !!item.iconClick }]"
          :user-id="item.userId"
          :avatar="item.src"
          :username="item.userName || item.username"
          :width="32"
          :link="!item.iconClick"
          @click.stop="item.iconClick && item.iconClick()"
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
export default {
  name: 'BaseTimeline',
  props: {
    items: { type: Array, default: () => [] },
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
  width: 32px;
  height: 32px;
}

.timeline-img.is-clickable {
  cursor: pointer;
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

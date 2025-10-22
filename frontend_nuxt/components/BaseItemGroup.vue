<template>
  <div
    ref="groupRef"
    class="base-item-group"
    :class="groupClass"
    :style="groupStyle"
    @mouseenter="onMouseEnter"
    @mouseleave="onMouseLeave"
    @focusin="onFocusIn"
    @focusout="onFocusOut"
  >
    <div
      v-for="(item, index) in normalizedItems"
      :key="resolveKey(item, index)"
      class="base-item-group-item"
      :style="{ zIndex: getZIndex(index) }"
    >
      <slot name="item" :item="item" :index="index"></slot>
    </div>
    <slot name="after"></slot>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'

const props = defineProps({
  items: {
    type: Array,
    default: () => [],
  },
  itemKey: {
    type: [String, Function],
    default: null,
  },
  overlap: {
    type: [Number, String],
    default: 12,
  },
  expandedGap: {
    type: [Number, String],
    default: 8,
  },
  direction: {
    type: String,
    default: 'horizontal',
    validator: (value) => ['horizontal', 'vertical'].includes(value),
  },
  reverse: {
    type: Boolean,
    default: false,
  },
  animationDuration: {
    type: [Number, String],
    default: 200,
  },
})

const groupRef = ref(null)
const state = reactive({
  hovering: false,
  focused: false,
})

const normalizedItems = computed(() => props.items || [])

const sanitizedOverlap = computed(() => Math.max(0, Number(props.overlap) || 0))
const sanitizedExpandedGap = computed(() => Math.max(0, Number(props.expandedGap) || 0))
const sanitizedAnimationDuration = computed(() => Math.max(0, Number(props.animationDuration) || 0))

const groupClass = computed(() => [
  `base-item-group--${props.direction}`,
  {
    'is-expanded': isExpanded.value,
    'is-reversed': props.reverse,
  },
])

const groupStyle = computed(() => ({
  '--base-item-group-overlap': `${sanitizedOverlap.value}px`,
  '--base-item-group-expanded-gap': `${sanitizedExpandedGap.value}px`,
  '--base-item-group-transition-duration': `${sanitizedAnimationDuration.value}ms`,
}))

const isExpanded = computed(() => state.hovering || state.focused)

function onMouseEnter() {
  state.hovering = true
}

function onMouseLeave() {
  state.hovering = false
}

function onFocusIn() {
  state.focused = true
}

function onFocusOut(event) {
  const nextTarget = event.relatedTarget
  if (!groupRef.value) {
    state.focused = false
    return
  }
  if (!nextTarget || !groupRef.value.contains(nextTarget)) {
    state.focused = false
  }
}

function resolveKey(item, index) {
  if (typeof props.itemKey === 'function') {
    return props.itemKey(item, index)
  }
  if (props.itemKey && item && Object.prototype.hasOwnProperty.call(item, props.itemKey)) {
    return item[props.itemKey]
  }
  return index
}

function getZIndex(index) {
  if (props.reverse) {
    return index + 1
  }
  return normalizedItems.value.length - index
}
</script>

<style scoped>
.base-item-group {
  --base-item-group-overlap: 12px;
  --base-item-group-expanded-gap: 8px;
  --base-item-group-transition-duration: 200ms;
  display: inline-flex;
  position: relative;
  align-items: center;
}

.base-item-group:focus-within {
  outline: none;
}

.base-item-group--horizontal {
  flex-direction: row;
}

.base-item-group--horizontal.is-reversed {
  flex-direction: row-reverse;
}

.base-item-group--vertical {
  flex-direction: column;
  align-items: flex-start;
}

.base-item-group--vertical.is-reversed {
  flex-direction: column-reverse;
}

.base-item-group-item {
  transition:
    margin var(--base-item-group-transition-duration) ease,
    transform var(--base-item-group-transition-duration) ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.base-item-group--horizontal:not(.is-expanded) .base-item-group-item:not(:first-child) {
  margin-left: calc(var(--base-item-group-overlap) * -1);
}

.base-item-group--horizontal.is-expanded .base-item-group-item:not(:first-child) {
  margin-left: var(--base-item-group-expanded-gap);
}

.base-item-group--vertical:not(.is-expanded) .base-item-group-item:not(:first-child) {
  margin-top: calc(var(--base-item-group-overlap) * -1);
}

.base-item-group--vertical.is-expanded .base-item-group-item:not(:first-child) {
  margin-top: var(--base-item-group-expanded-gap);
}

.base-item-group.is-expanded .base-item-group-item {
  transform: translateZ(0);
}
</style>

<template>
  <component
    :is="wrapperTag"
    :to="isLink ? resolvedLink : undefined"
    class="base-user-avatar"
    :class="wrapperClass"
    :style="wrapperStyle"
    v-bind="wrapperAttrs"
    :role="isLink ? undefined : 'img'"
    :aria-label="altText"
    :title="altText"
  >
    <span class="base-user-avatar-backdrop" aria-hidden="true" />
    <BaseImage :src="currentSrc" :alt="altText" class="base-user-avatar-img" @error="onError" />
  </component>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useAttrs } from 'vue'
import BaseImage from './BaseImage.vue'

const DEFAULT_AVATAR = '/default-avatar.svg'

const props = defineProps({
  userId: {
    type: [String, Number],
    default: null,
  },
  src: {
    type: String,
    default: '',
  },
  alt: {
    type: String,
    default: '',
  },
  width: {
    type: [Number, String],
    default: null,
  },
  rounded: {
    type: Boolean,
    default: true,
  },
  disableLink: {
    type: Boolean,
    default: false,
  },
  to: {
    type: String,
    default: '',
  },
})

const attrs = useAttrs()

const currentSrc = ref(props.src || DEFAULT_AVATAR)

watch(
  () => props.src,
  (value) => {
    currentSrc.value = value || DEFAULT_AVATAR
  },
)

const resolvedLink = computed(() => {
  if (props.to) return props.to
  if (props.userId !== null && props.userId !== undefined && props.userId !== '') {
    return `/users/${props.userId}`
  }
  return null
})

const altText = computed(() => props.alt || '用户头像')

const sizeStyle = computed(() => {
  if (!props.width && props.width !== 0) return null
  const value = typeof props.width === 'number' ? `${props.width}px` : props.width
  if (!value) return null
  return { width: value, height: value }
})

const accentHue = computed(() => {
  const seed = props.userId ?? props.alt
  const source = seed !== undefined && seed !== null ? String(seed) : ''
  if (!source) return 198
  let hash = 0
  for (let index = 0; index < source.length; index += 1) {
    hash = (hash << 5) - hash + source.charCodeAt(index)
    hash |= 0
  }
  return Math.abs(hash) % 360
})

const accentStyles = computed(() => {
  const hue = accentHue.value
  return {
    '--avatar-accent': `hsl(${hue}, 74%, 54%)`,
    '--avatar-accent-light': `hsl(${hue}, 95%, 82%)`,
    '--avatar-accent-soft': `hsl(${hue}, 96%, 95%)`,
    '--avatar-accent-border': `hsla(${hue}, 70%, 48%, 0.28)`,
    '--avatar-accent-shadow': `hsla(${hue}, 68%, 36%, 0.2)`,
  }
})

const wrapperStyle = computed(() => {
  const attrStyle = attrs.style
  return [accentStyles.value, sizeStyle.value, attrStyle]
})

const isLink = computed(() => !props.disableLink && !!resolvedLink.value)

const wrapperTag = computed(() => (isLink.value ? 'NuxtLink' : 'div'))

const wrapperClass = computed(() => [
  attrs.class,
  { 'is-rounded': props.rounded, 'is-interactive': isLink.value },
])

const wrapperAttrs = computed(() => {
  const { class: _class, style: _style, to: _to, href: _href, ...rest } = attrs
  return rest
})

function onError() {
  if (currentSrc.value !== DEFAULT_AVATAR) {
    currentSrc.value = DEFAULT_AVATAR
  }
}
</script>

<style scoped>
.base-user-avatar {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 16px;
  background: linear-gradient(
    140deg,
    var(--avatar-accent-soft, rgba(17, 182, 197, 0.12)) 0%,
    var(--avatar-accent-light, rgba(17, 182, 197, 0.22)) 100%
  );
  border: 1px solid var(--avatar-accent-border, rgba(17, 182, 197, 0.2));
  box-shadow:
    0 1px 2px rgba(15, 52, 67, 0.08),
    0 3px 8px var(--avatar-accent-shadow, rgba(17, 182, 197, 0.18));
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease,
    border-color 0.3s ease,
    background 0.3s ease;
}

.base-user-avatar.is-rounded {
  border-radius: 50%;
}

.base-user-avatar:not(.is-rounded) {
  border-radius: 0;
}

.base-user-avatar-backdrop {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(circle at 28% 28%, rgba(255, 255, 255, 0.72), transparent 62%),
    linear-gradient(150deg, rgba(255, 255, 255, 0.08), transparent),
    linear-gradient(
      140deg,
      var(--avatar-accent-soft, rgba(17, 182, 197, 0.08)) 0%,
      var(--avatar-accent-light, rgba(17, 182, 197, 0.18)) 100%
    );
  opacity: 0.75;
  transition:
    opacity 0.35s ease,
    transform 0.35s ease;
  z-index: 0;
}

.base-user-avatar-img {
  position: relative;
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  z-index: 1;
  border-radius: inherit;
  transition: transform 0.35s ease;
}

.base-user-avatar.is-interactive:hover,
.base-user-avatar.is-interactive:focus-visible {
  transform: translateY(-1px) scale(1.02);
  border-color: var(--avatar-accent, var(--primary-color, #0a6e78));
  box-shadow:
    0 6px 16px var(--avatar-accent-shadow, rgba(17, 182, 197, 0.24)),
    0 3px 6px rgba(15, 52, 67, 0.18);
  outline: none;
}

.base-user-avatar.is-interactive:hover .base-user-avatar-backdrop,
.base-user-avatar.is-interactive:focus-visible .base-user-avatar-backdrop {
  opacity: 1;
  transform: scale(1.05);
}

.base-user-avatar.is-interactive:hover .base-user-avatar-img,
.base-user-avatar.is-interactive:focus-visible .base-user-avatar-img {
  transform: scale(1.02);
}
</style>

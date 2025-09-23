<template>
  <NuxtLink
    :to="resolvedLink"
    class="base-user-avatar"
    :class="wrapperClass"
    :style="wrapperStyle"
    v-bind="wrapperAttrs"
  >
    <BaseImage :src="currentSrc" :alt="altText" class="base-user-avatar-img" @error="onError" />
  </NuxtLink>
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

const wrapperStyle = computed(() => {
  const attrStyle = attrs.style
  return [sizeStyle.value, attrStyle]
})

const wrapperClass = computed(() => [attrs.class, { 'is-rounded': props.rounded }])

const wrapperAttrs = computed(() => {
  const { class: _class, style: _style, ...rest } = attrs
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
  --base-avatar-ring-width: var(--avatar-ring-width, 1.5px);
  --base-avatar-ring: var(--avatar-ring, linear-gradient(135deg, #6366f1, #ec4899));
  --base-avatar-surface: var(
    --avatar-surface,
    var(--avatar-placeholder-color, rgba(255, 255, 255, 0.88))
  );
  --base-avatar-shadow: var(--avatar-shadow, 0 12px 30px -18px rgba(15, 23, 42, 0.55));

  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
  box-sizing: border-box;
  color: inherit;
  border-radius: 50%;
  border: var(--base-avatar-ring-width) solid transparent;
  background:
    var(--base-avatar-surface) padding-box,
    var(--base-avatar-ring) border-box;
  background-clip: padding-box, border-box;
  background-origin: border-box;
  box-shadow: var(--base-avatar-shadow);
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease,
    filter 0.25s ease;
}

.base-user-avatar.is-rounded {
  border-radius: 50%;
}

.base-user-avatar:not(.is-rounded) {
  border-radius: var(--avatar-square-radius, 0);
}

.base-user-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  border-radius: inherit;
  position: relative;
  z-index: 1;
  transition:
    transform 0.25s ease,
    filter 0.25s ease;
  backface-visibility: hidden;
}

.base-user-avatar:hover,
.base-user-avatar:focus-visible {
  transform: translateY(-1px) scale(1.02);
  box-shadow: 0 18px 35px -20px rgba(15, 23, 42, 0.65);
}

.base-user-avatar:focus-visible {
  outline: 2px solid rgba(96, 165, 250, 0.6);
  outline-offset: 3px;
}

.base-user-avatar:active {
  transform: translateY(0) scale(0.99);
}

.base-user-avatar:hover .base-user-avatar-img,
.base-user-avatar:focus-visible .base-user-avatar-img {
  transform: scale(1.03);
  filter: saturate(1.08);
}

@media (prefers-reduced-motion: reduce) {
  .base-user-avatar,
  .base-user-avatar-img {
    transition: none;
  }

  .base-user-avatar:hover,
  .base-user-avatar:focus-visible,
  .base-user-avatar:active {
    transform: none;
  }

  .base-user-avatar:hover .base-user-avatar-img,
  .base-user-avatar:focus-visible .base-user-avatar-img {
    transform: none;
  }
}
</style>

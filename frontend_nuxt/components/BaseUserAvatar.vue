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
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background-color: var(--avatar-placeholder-color, #f0f0f0);
}

.base-user-avatar.is-rounded {
  border-radius: 50%;
}

.base-user-avatar:not(.is-rounded) {
  border-radius: 0;
}

.base-user-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
</style>

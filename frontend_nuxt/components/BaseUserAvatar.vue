<template>
  <component :is="wrapperTag" v-bind="wrapperAttrs" :class="containerClass" :style="mergedStyle">
    <BaseImage :src="currentSrc" :alt="altText" class="base-user-avatar-img" @error="handleError" />
  </component>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useAttrs } from 'vue'

const DEFAULT_AVATAR = '/default-avatar.svg'

const props = defineProps({
  userId: {
    type: [String, Number],
    required: true,
  },
  avatar: {
    type: String,
    default: '',
  },
  username: {
    type: String,
    default: '',
  },
  width: {
    type: [Number, String],
    default: 40,
  },
  alt: {
    type: String,
    default: '',
  },
  link: {
    type: Boolean,
    default: true,
  },
})

const attrs = useAttrs()
const currentSrc = ref(props.avatar || DEFAULT_AVATAR)

watch(
  () => props.avatar,
  (newVal) => {
    currentSrc.value = newVal || DEFAULT_AVATAR
  },
)

const wrapperTag = computed(() => (props.link ? 'NuxtLink' : 'div'))
const sizeStyle = computed(() => {
  const value = typeof props.width === 'number' ? `${props.width}px` : props.width || '40px'
  return {
    width: value,
    height: value,
  }
})

const altText = computed(() => {
  if (props.alt) return props.alt
  if (props.username) return `${props.username}的头像`
  return '用户头像'
})

const containerClass = computed(() => {
  const classes = ['base-user-avatar']
  if (props.link) classes.push('is-link')
  if (attrs.class) classes.push(attrs.class)
  return classes
})

const mergedStyle = computed(() => {
  if (!attrs.style) return sizeStyle.value
  return [sizeStyle.value, attrs.style]
})

const wrapperAttrs = computed(() => {
  const { class: _class, style: _style, ...rest } = attrs
  if (props.link) {
    return {
      ...rest,
      to: `/users/${props.userId}`,
    }
  }
  return rest
})

function handleError() {
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
  border-radius: 50%;
  overflow: hidden;
  background-color: var(--avatar-background, rgba(0, 0, 0, 0.05));
}

.base-user-avatar.is-link {
  cursor: pointer;
}

.base-user-avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  display: block;
}
</style>

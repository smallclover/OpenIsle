<template>
  <NuxtLink
    :to="resolvedLink"
    class="base-user-avatar"
    :class="wrapperClass"
    :style="wrapperStyle"
    v-bind="wrapperAttrs"
  >
    <BaseImage :src="currentSrc" :alt="altText" :class="imageClass" @error="onError" />
    <span v-if="showInitial" class="base-user-avatar-initial">{{ userInitial }}</span>
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

const identifier = computed(() => {
  if (props.userId !== null && props.userId !== undefined && props.userId !== '') {
    return String(props.userId)
  }
  if (props.alt && props.alt.trim()) {
    return props.alt.trim()
  }
  return altText.value
})

const initialSource = computed(() => {
  if (props.alt && props.alt.trim() && props.alt.trim() !== '用户头像') {
    return props.alt.trim()
  }
  if (attrs.title && typeof attrs.title === 'string' && attrs.title.trim()) {
    return attrs.title.trim()
  }
  if (props.userId !== null && props.userId !== undefined && props.userId !== '') {
    return String(props.userId)
  }
  return ''
})

const isDefaultAvatar = computed(() => currentSrc.value === DEFAULT_AVATAR)

function parseCssSize(value) {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return { numeric: value, unit: 'px' }
  }
  if (typeof value !== 'string') return null
  const trimmed = value.trim()
  if (!trimmed) return null
  const directNumber = Number(trimmed)
  if (!Number.isNaN(directNumber)) {
    return { numeric: directNumber, unit: 'px' }
  }
  const match = trimmed.match(/^(-?\d*\.?\d+)([a-z%]+)$/i)
  if (!match) return null
  return { numeric: Number(match[1]), unit: match[2] }
}

const sizeStyle = computed(() => {
  if (!props.width && props.width !== 0) return null
  const value = typeof props.width === 'number' ? `${props.width}px` : props.width
  if (!value) return null
  const parsed = parseCssSize(value)
  const style = { width: value, height: value, '--avatar-size': value }
  if (parsed && Number.isFinite(parsed.numeric)) {
    const computedFont = (parsed.numeric * 0.42).toFixed(2)
    const normalized = computedFont.replace(/\.00$/, '')
    style['--avatar-font-size'] = `${normalized}${parsed.unit}`
  }
  return style
})

function stringToColorSeed(value) {
  if (!value) return 0
  let hash = 0
  for (let i = 0; i < value.length; i += 1) {
    hash = (hash << 5) - hash + value.charCodeAt(i)
    hash |= 0
  }
  return Math.abs(hash)
}

const accentStyle = computed(() => {
  if (!isDefaultAvatar.value) return null
  const seed = stringToColorSeed(identifier.value)
  const hue = seed % 360
  const altHue = (hue + 37) % 360
  const saturation = 72
  const lightness = 78
  const start = `hsl(${hue}, ${saturation}%, ${Math.min(lightness + 8, 95)}%)`
  const end = `hsl(${altHue}, ${Math.max(saturation - 12, 45)}%, ${Math.max(lightness - 12, 48)}%)`
  return {
    '--avatar-background': `linear-gradient(135deg, ${start}, ${end})`,
    '--avatar-border-color': `hsla(${hue}, ${Math.max(saturation - 24, 32)}%, ${Math.max(
      lightness - 35,
      28,
    )}%, 0.55)`,
    '--avatar-text-color': '#ffffff',
  }
})

const wrapperStyle = computed(() => {
  const attrStyle = attrs.style
  return [
    { '--avatar-font-size': 'clamp(0.75rem, 0.6rem + 0.4vw, 1.75rem)' },
    sizeStyle.value,
    accentStyle.value,
    attrStyle,
  ]
})

const wrapperClass = computed(() => [
  attrs.class,
  {
    'is-rounded': props.rounded,
    'has-default': isDefaultAvatar.value,
    'is-interactive': !props.disableLink && Boolean(resolvedLink.value),
  },
])

const imageClass = computed(() => ['base-user-avatar-img', { 'is-default': isDefaultAvatar.value }])

const userInitial = computed(() => {
  const source = initialSource.value || ''
  const trimmed = source.trim()
  if (!trimmed) return ''
  const match = trimmed.match(/[\p{L}\p{N}]/u)
  if (match && match[0]) return match[0].toUpperCase()
  return trimmed.charAt(0).toUpperCase()
})

const showInitial = computed(() => isDefaultAvatar.value && Boolean(userInitial.value))

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
  --avatar-background: var(--avatar-placeholder-color, #f0f0f0);
  --avatar-border-color: rgba(15, 23, 42, 0.08);
  --avatar-text-color: rgba(255, 255, 255, 0.86);
  --avatar-shadow: 0 6px 18px rgba(15, 23, 42, 0.12);
  --avatar-shadow-hover: 0 10px 24px rgba(15, 23, 42, 0.16);
  --avatar-size: 3rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
  background: var(--avatar-background);
  border: 1px solid var(--avatar-border-color);
  border-radius: var(--avatar-border-radius, 16px);
  box-shadow: var(--avatar-shadow);
  color: var(--avatar-text-color);
  transition:
    transform 0.28s ease,
    box-shadow 0.32s ease,
    border-color 0.32s ease,
    filter 0.28s ease;
  isolation: isolate;
}

.base-user-avatar.is-rounded {
  border-radius: 50%;
}

.base-user-avatar:not(.is-rounded) {
  border-radius: var(--avatar-border-radius, 16px);
}

.base-user-avatar.is-interactive {
  cursor: pointer;
}

.base-user-avatar.is-interactive:hover,
.base-user-avatar.is-interactive:focus-visible {
  transform: translateY(-1px) scale(1.01);
  box-shadow: var(--avatar-shadow-hover);
  border-color: rgba(59, 130, 246, 0.4);
}

.base-user-avatar.has-default::after {
  content: '';
  position: absolute;
  inset: 1px;
  border-radius: inherit;
  background: rgba(15, 23, 42, 0.12);
  mix-blend-mode: soft-light;
  opacity: 0.25;
  pointer-events: none;
  transition: opacity 0.28s ease;
}

.base-user-avatar.has-default:hover::after,
.base-user-avatar.has-default:focus-visible::after {
  opacity: 0.18;
}

.base-user-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  filter: saturate(108%);
  transition:
    opacity 0.35s ease,
    transform 0.35s ease,
    filter 0.35s ease;
}

.base-user-avatar-img.is-default {
  opacity: 0.32;
  filter: saturate(90%) brightness(1.05);
  mix-blend-mode: multiply;
}

.base-user-avatar-initial {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: var(--avatar-font-size, calc(var(--avatar-size) * 0.42));
  text-transform: uppercase;
  letter-spacing: 0.04em;
  pointer-events: none;
  user-select: none;
  color: inherit;
  text-shadow: 0 1px 4px rgba(15, 23, 42, 0.4);
}

@media (prefers-reduced-motion: reduce) {
  .base-user-avatar {
    transition: none;
  }

  .base-user-avatar-img {
    transition: none;
  }
}
</style>

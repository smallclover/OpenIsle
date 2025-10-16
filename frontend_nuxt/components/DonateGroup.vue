<template>
  <div class="donate-container">
    <div class="donate-viewer">
      <div class="donate-viewer-item-container" @mouseenter="cancelHide" @mouseleave="scheduleHide">
        <BaseItemGroup :items="donateItems" :overlap="10" :expanded-gap="2" :direction="vertical">
          <template #item="{ item }">
            <BaseUserAvatar
              :user-id="1"
              :src="avatar"
              alt="avatar"
              :width="25"
              :disable-link="true"
            />
          </template>
        </BaseItemGroup>
        <div class="donate-counts-text">100</div>
      </div>
    </div>
  </div>
  <div
    v-if="panelVisible"
    class="donate-panel"
    ref="reactionsPanelRef"
    :style="panelInlineStyle"
    @mouseenter="cancelHide"
    @mouseleave="scheduleHide"
  >
    <div class="donate-option">
      <financing />
      <div class="donate-counts-text">100</div>
    </div>
    <div class="donate-option">
      <financing />
      <div class="donate-counts-text">300</div>
    </div>
    <div class="donate-option">
      <financing />
      <div class="donate-counts-text">500</div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const emit = defineEmits(['update:modelValue'])

const props = defineProps({})

const avatar = ref('')
const panelVisible = ref(false)
const reactionsPanelRef = ref(null)
const panelInlineStyle = ref({})
const donateItems = ref([
  {
    id: 1,
    count: 100,
  },
  {
    id: 2,
    count: 300,
  },
  {
    id: 3,
    count: 500,
  },
])
let hideTimer = null

const scheduleHide = () => {
  clearTimeout(hideTimer)
  hideTimer = setTimeout(() => {
    panelVisible.value = false
  }, 500)
}
const cancelHide = () => {
  clearTimeout(hideTimer)
}

const updatePanelInlineStyle = () => {
  if (!panelVisible.value) return
  const panelEl = reactionsPanelRef.value
  if (!panelEl) return
  const parentEl = panelEl.closest('.reactions-container')?.parentElement
  if (!parentEl) return
  const parentWidth = parentEl.clientWidth - 20
  panelInlineStyle.value = {
    width: 'max-content',
    maxWidth: `${parentWidth}px`,
  }
}

watch(panelVisible, async (visible) => {
  if (visible) {
    await nextTick()
    updatePanelInlineStyle()
  }
})

onMounted(async () => {
  window.addEventListener('resize', updatePanelInlineStyle)

  const updateAvatar = async () => {
    if (authState.loggedIn) {
      const user = await loadCurrentUser()
      if (user && user.avatar) {
        avatar.value = user.avatar
      }
    }
  }
  await updateAvatar()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updatePanelInlineStyle)
})
</script>

<style scoped>
.donate-viewer-item-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 2px;
}
</style>

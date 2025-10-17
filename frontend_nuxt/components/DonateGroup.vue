<template>
  <div class="donate-container">
    <ToolTip content="打赏作者" placement="bottom" v-if="donateItems.length > 0">
      <div class="donate-viewer" @click="openPanel">
        <div
          class="donate-viewer-item-container"
          @mouseenter="cancelHide"
          @mouseleave="scheduleHide"
        >
          <BaseItemGroup :items="donateItems" :overlap="10" :expanded-gap="2" :direction="vertical">
            <template #item="{ item }">
              <BaseUserAvatar
                :user-id="1"
                :src="avatar"
                alt="avatar"
                :width="20"
                :disable-link="true"
              />
            </template>
          </BaseItemGroup>
          <div class="donate-counts-text">100</div>
        </div>
      </div>
    </ToolTip>
    <ToolTip content="赞赏作者" placement="bottom" v-else>
      <div class="donate-viewer-item placeholder" @click="openPanel">
        <financing class="donate-viewer-item-placeholder-icon" />
      </div>
    </ToolTip>
    <div
      v-if="panelVisible"
      class="donate-panel"
      ref="donatePanelRef"
      :style="panelInlineStyle"
      @mouseenter="cancelHide"
      @mouseleave="scheduleHide"
    >
      <div class="donate-option">
        <financing class="donate-option-icon" />
        <div class="donate-counts-text">10</div>
      </div>
      <div class="donate-option">
        <financing class="donate-option-icon" />
        <div class="donate-counts-text">30</div>
      </div>
      <div class="donate-option">
        <financing class="donate-option-icon" />
        <div class="donate-counts-text">100</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Finance } from '@icon-park/vue-next'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const emit = defineEmits(['update:modelValue'])

const props = defineProps({})

const avatar = ref('')
const panelVisible = ref(false)
const donatePanelRef = ref(null)
const panelInlineStyle = ref({})
const donateItems = ref([
  // {
  //   id: 1,
  //   count: 100,
  // },
  // {
  //   id: 2,
  //   count: 300,
  // },
  // {
  //   id: 3,
  //   count: 500,
  // },
])
let hideTimer = null

const openPanel = () => {
  clearTimeout(hideTimer)
  panelVisible.value = true
}

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
  const panelEl = donatePanelRef.value
  if (!panelEl) return
  const parentEl = panelEl.closest('.donate-container')?.parentElement.parentElement
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
.donate-container {
  position: relative;
  display: flex;
  flex-direction: row;
  align-items: center;
}

.donate-viewer-item-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 2px;
}

.donate-viewer {
  border-radius: 13px;
  padding: 3px;
  padding-right: 6px;
  cursor: pointer;
  transition: background-color 0.5s ease;
}

.donate-viewer:hover {
  background-color: var(--secondary-color-hover);
}

.donate-counts-text {
  color: var(--primary-color);
  font-size: 14px;
}

.donate-panel {
  position: absolute;
  bottom: 35px;
  background-color: var(--background-color);
  border: 1px solid var(--normal-border-color);
  border-radius: 20px;
  padding: 5px 10px;
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  z-index: 10;
  gap: 5px;
  box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.1);
}

.donate-viewer-item.placeholder {
  display: flex;
  cursor: pointer;
  flex-direction: row;
  padding: 2px 10px;
  gap: 5px;
  border: 1px solid var(--normal-border-color);
  border-radius: 10px;
  margin-right: 5px;
  font-size: 14px;
  color: var(--text-color);
  align-items: center;
  background-color: var(--normal-light-background-color);
}

.donate-viewer-item {
  font-size: 16px;
}

.donate-viewer-item-placeholder-icon {
  opacity: 0.5;
}

.donate-option {
  cursor: pointer;
  padding: 3px 6px;
  border-radius: 4px;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 2px;
}

.donate-option:hover {
  background-color: var(--normal-light-background-color);
}

.donate-option-icon {
  color: var(--primary-color);
}
</style>

<template>
  <div class="donate-container">
    <ToolTip content="打赏作者" placement="bottom" v-if="donationList.length > 0">
      <div class="donate-viewer" @click="openPanel">
        <div
          class="donate-viewer-item-container"
          @mouseenter="cancelHide"
          @mouseleave="scheduleHide"
        >
          <BaseItemGroup
            :items="donationList"
            :overlap="10"
            :expanded-gap="2"
            :direction="vertical"
          >
            <template #item="{ item }">
              <BaseUserAvatar
                :user-id="item.userId"
                :src="item.avatar"
                :alt="item.username"
                :width="20"
                :disable-link="true"
              />
            </template>
          </BaseItemGroup>
          <div class="donate-counts-text">{{ totalAmount }}</div>
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
      <div
        v-for="option in donateOptions"
        :key="option"
        class="donate-option"
        :class="{ disabled: donating || isAuthorUser || !authState.loggedIn }"
        @click="handleDonate(option)"
      >
        <financing class="donate-option-icon" />
        <div class="donate-counts-text">{{ option }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Finance } from '@icon-park/vue-next'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { toast } from '~/main'
import { authState, getToken } from '~/utils/auth'

const financing = Finance

const props = defineProps({
  postId: {
    type: [Number, String],
    required: true,
  },
  authorId: {
    type: [Number, String],
    required: true,
  },
  isAuthor: {
    type: Boolean,
    default: false,
  },
})

const config = useRuntimeConfig()
const API_BASE_URL = config.public.apiBaseUrl

const panelVisible = ref(false)
const donatePanelRef = ref(null)
const panelInlineStyle = ref({})
const donationSummary = ref({ totalAmount: 0, donations: [] })
const donating = ref(false)
let hideTimer = null

const donateOptions = [10, 30, 100]
const donationList = computed(() => donationSummary.value?.donations ?? [])
const totalAmount = computed(() => donationSummary.value?.totalAmount ?? 0)
const isAuthorUser = computed(() => {
  if (props.isAuthor) return true
  if (!authState.userId || !props.authorId) return false
  return Number(authState.userId) === Number(props.authorId)
})

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

const normalizeSummary = (data) => ({
  totalAmount: data?.totalAmount ?? 0,
  donations: Array.isArray(data?.donations) ? data.donations : [],
})

const loadDonations = async () => {
  try {
    const res = await fetch(`${API_BASE_URL}/api/posts/${props.postId}/donations`)
    if (!res.ok) return
    const data = await res.json()
    donationSummary.value = normalizeSummary(data)
  } catch (e) {
    // ignore network errors for donation summary
  }
}

const handleDonate = async (amount) => {
  if (!amount || donating.value) return
  if (!authState.loggedIn) {
    toast.error('请先登录后再打赏')
    panelVisible.value = false
    return
  }
  if (isAuthorUser.value) {
    toast.warning('不能给自己打赏')
    return
  }
  try {
    donating.value = true
    const token = getToken()
    const res = await fetch(`${API_BASE_URL}/api/posts/${props.postId}/donations`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : '',
      },
      body: JSON.stringify({ amount }),
    })
    const data = await res.json().catch(() => null)
    if (!res.ok) {
      if (res.status === 401) {
        toast.error('请先登录后再打赏')
      } else {
        toast.error(data?.error || '打赏失败')
      }
      return
    }
    donationSummary.value = normalizeSummary(data)
    toast.success('打赏成功，感谢你的支持！')
    panelVisible.value = false
  } catch (e) {
    toast.error('打赏失败，请稍后再试')
  } finally {
    donating.value = false
  }
}

onMounted(async () => {
  window.addEventListener('resize', updatePanelInlineStyle)
  await loadDonations()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updatePanelInlineStyle)
})

watch(
  () => props.postId,
  async () => {
    donationSummary.value = { totalAmount: 0, donations: [] }
    await loadDonations()
  },
)
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

.donate-option.disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.donate-option.disabled:hover {
  background-color: transparent;
}

.donate-option-icon {
  color: var(--primary-color);
}

@media (max-width: 768px) {
  .donate-viewer-item.placeholder {
    padding: 4px 8px;
    gap: 3px;
    border: 1px solid var(--normal-border-color);
    border-radius: 10px;
    margin-right: 3px;
    margin-bottom: 3px;
    font-size: 12px;
    color: var(--text-color);
    align-items: center;
  }
}
</style>

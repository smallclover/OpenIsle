<template>
  <div class="search-dropdown">
    <Dropdown
      ref="dropdown"
      v-model="selected"
      :fetch-options="fetchResults"
      remote
      menu-class="search-menu"
      option-class="search-option"
      :show-search="isMobile"
      @update:search="keyword = $event"
      @close="onClose"
    >
      <template #display="{ setSearch }">
        <div class="search-input">
          <search-icon class="search-input-icon" />
          <input
            class="text-input"
            v-model="keyword"
            placeholder="Search users"
            @input="setSearch(keyword)"
          />
        </div>
      </template>
      <template #option="{ option }">
        <div class="search-option-item">
          <BaseUserAvatar
            :src="option.avatar"
            :user-id="option.id"
            :alt="option.username"
            class="avatar"
            :disable-link="true"
          />
          <div class="result-body">
            <div
              class="result-main"
              v-html="renderHighlight(option.highlightedUsername, option.username)"
            ></div>
            <div
              v-if="option.introduction"
              class="result-sub"
              v-html="renderHighlight(option.highlightedIntroduction, option.introduction)"
            ></div>
          </div>
        </div>
      </template>
    </Dropdown>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import Dropdown from '~/components/Dropdown.vue'
import { stripMarkdown } from '~/utils/markdown'
import { useIsMobile } from '~/utils/screen'
import { getToken } from '~/utils/auth'
import BaseUserAvatar from '~/components/BaseUserAvatar.vue'
const config = useRuntimeConfig()
const API_BASE_URL = config.public.apiBaseUrl

const emit = defineEmits(['close'])

const keyword = ref('')
const selected = ref(null)
const results = ref([])
const dropdown = ref(null)
const isMobile = useIsMobile()

const toggle = () => {
  dropdown.value.toggle()
}

const onClose = () => emit('close')

const fetchResults = async (kw) => {
  if (!kw) return []
  const res = await fetch(`${API_BASE_URL}/api/search/users?keyword=${encodeURIComponent(kw)}`)
  if (!res.ok) return []
  const data = await res.json()
  results.value = data.map((u) => ({
    id: u.id,
    username: u.username,
    avatar: u.avatar,
    introduction: u.introduction,
    highlightedUsername: u.highlightedText,
    highlightedIntroduction: u.highlightedSubText || u.highlightedExtra,
  }))
  return results.value
}

const escapeHtml = (value = '') =>
  String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')

const renderHighlight = (highlighted, fallback) => {
  if (highlighted) {
    return highlighted
  }
  const plain = stripMarkdown(fallback || '')
  if (!plain) {
    return ''
  }
  return escapeHtml(plain)
}

watch(selected, async (val) => {
  if (!val) return
  const user = results.value.find((u) => u.id === val)
  if (!user) return
  const token = getToken()
  if (!token) {
    navigateTo('/login', { replace: true })
  } else {
    try {
      const res = await fetch(`${API_BASE_URL}/api/messages/conversations`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ recipientId: user.id }),
      })
      if (res.ok) {
        const data = await res.json()
        navigateTo(`/message-box/${data.conversationId}`, { replace: true })
      }
    } catch (e) {
      // ignore
    }
  }
  selected.value = null
  keyword.value = ''
})

defineExpose({
  toggle,
})
</script>

<style scoped>
.search-dropdown {
  margin-top: 20px;
  width: 500px;
}

.search-input {
  padding: 10px;
  display: flex;
  align-items: center;
  width: 100%;
}

.text-input {
  background-color: var(--app-menu-background-color);
  color: var(--text-color);
  border: none;
  outline: none;
  width: 100%;
  margin-left: 10px;
  font-size: 16px;
}

.search-menu {
  width: 100%;
  max-width: 600px;
}

@media (max-width: 768px) {
  .search-dropdown {
    width: 100%;
  }
}

.search-option-item {
  display: flex;
  gap: 10px;
}

.search-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
}

:deep(mark) {
  color: var(--primary-color);
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
}

.avatar :deep(.base-user-avatar-img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.result-body {
  display: flex;
  flex-direction: column;
}

.result-main {
  font-weight: bold;
}

.result-sub {
  font-size: 12px;
  color: #666;
}
</style>

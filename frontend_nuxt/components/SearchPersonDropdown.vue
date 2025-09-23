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
          <BaseImage
            :src="option.avatar || '/default-avatar.svg'"
            class="avatar"
            @error="handleAvatarError"
          />
          <div class="result-body">
            <div class="result-main" v-html="highlight(option.username)"></div>
            <div
              v-if="option.introduction"
              class="result-sub"
              v-html="highlight(option.introduction)"
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
  }))
  return results.value
}

const highlight = (text) => {
  text = stripMarkdown(text || '')
  if (!keyword.value) return text
  const reg = new RegExp(keyword.value, 'gi')
  return text.replace(reg, (m) => `<span class="highlight">${m}</span>`)
}

const handleAvatarError = (e) => {
  e.target.src = '/default-avatar.svg'
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

:deep(.highlight) {
  color: var(--primary-color);
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
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

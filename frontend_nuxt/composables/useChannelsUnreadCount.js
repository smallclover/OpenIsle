import { ref, computed, watch } from 'vue'
import { useWebSocket } from './useWebSocket'
import { getToken } from '~/utils/auth'

const count = ref(0)
let isInitialized = false

export function useChannelsUnreadCount() {
  const config = useRuntimeConfig()
  const API_BASE_URL = config.public.apiBaseUrl
  const { subscribe, isConnected, connect } = useWebSocket()

  const fetchChannelUnread = async () => {
    const token = getToken()
    if (!token) {
      count.value = 0
      return
    }
    try {
      const response = await fetch(`${API_BASE_URL}/api/channels/unread-count`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      if (response.ok) {
        const data = await response.json()
        count.value = data
      }
    } catch (e) {
      console.error('Failed to fetch channel unread count:', e)
    }
  }

  const setupWebSocketListener = () => {
    const destination = '/user/queue/channel-unread'

    subscribe(destination, (message) => {
      const unread = parseInt(message.body, 10)
      if (!isNaN(unread)) {
        count.value = unread
      }
    }).then((subscription) => {
      if (subscription) {
        console.log('频道未读消息订阅成功')
      }
    })
  }

  const initialize = () => {
    const token = getToken()
    if (!token) {
      count.value = 0
      return
    }

    if (!isConnected.value) {
      connect(token)
    }

    fetchChannelUnread()
    setupWebSocketListener()
  }

  const setFromList = (channels) => {
    count.value = Array.isArray(channels) ? channels.filter((c) => c.unreadCount > 0).length : 0
  }

  const hasUnread = computed(() => count.value > 0)

  if (!isInitialized) {
    const token = getToken()
    if (token) {
      isInitialized = true
      initialize()
    }
  }

  return {
    count,
    hasUnread,
    fetchChannelUnread,
    initialize,
    setFromList,
  }
}

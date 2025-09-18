import { ref, watch, onMounted } from 'vue'
import { useWebSocket } from './useWebSocket'
import { getToken } from '~/utils/auth'

const count = ref(0)
let isInitialized = false

export function useUnreadCount() {
  const config = useRuntimeConfig()
  const API_BASE_URL = config.public.apiBaseUrl
  const { subscribe, isConnected, connect } = useWebSocket()

  const fetchUnreadCount = async () => {
    const token = getToken()
    if (!token) {
      count.value = 0
      return
    }
    try {
      const response = await fetch(`${API_BASE_URL}/api/messages/unread-count`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      if (response.ok) {
        const data = await response.json()
        count.value = data
      }
    } catch (error) {
      console.error('Failed to fetch unread count:', error)
    }
  }

  const setupWebSocketListener = () => {
    console.log('设置未读消息订阅...')
    const destination = '/user/queue/unread-count'

    subscribe(destination, (message) => {
      const unreadCount = parseInt(message.body, 10)
      if (!isNaN(unreadCount)) {
        count.value = unreadCount
      }
    }).then((subscription) => {
      if (subscription) {
        console.log('未读消息订阅成功')
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

    fetchUnreadCount()
    setupWebSocketListener()
  }

  if (!isInitialized) {
    const token = getToken()
    if (token) {
      isInitialized = true
      initialize()
    }
  }

  return {
    count,
    fetchUnreadCount,
    initialize,
  }
}

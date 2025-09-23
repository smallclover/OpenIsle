import { navigateTo, useRuntimeConfig } from 'nuxt/app'
import { reactive, ref } from 'vue'
import { toast } from '~/composables/useToast'
import { authState, getToken } from '~/utils/auth'
import { reactionEmojiMap } from '~/utils/reactions'

export const notificationState = reactive({
  unreadCount: 0,
})

const iconMap = {
  POST_VIEWED: 'HistoryIcon',
  COMMENT_REPLY: 'MessageOne',
  POST_REVIEWED: 'CheckCorrect',
  POST_REVIEW_REQUEST: 'FileText',
  POST_UPDATED: 'Edit',
  USER_ACTIVITY: 'UserIcon',
  FOLLOWED_POST: 'Pin',
  USER_FOLLOWED: 'AddUser',
  USER_UNFOLLOWED: 'ReduceUser',
  POST_SUBSCRIBED: 'Bookmark',
  POST_UNSUBSCRIBED: 'Bookmark',
  REGISTER_REQUEST: 'AlarmClock',
  ACTIVITY_REDEEM: 'PaperMoneyTwo',
  POINT_REDEEM: 'Gift',
  LOTTERY_WIN: 'MedalOne',
  LOTTERY_DRAW: 'Fireworks',
  POLL_VOTE: 'ChartHistogram',
  POLL_RESULT_OWNER: 'RankingList',
  POLL_RESULT_PARTICIPANT: 'ChartLine',
  MENTION: 'HashtagKey',
  POST_DELETED: 'ClearIcon',
  POST_FEATURED: 'Star',
}

export async function fetchUnreadCount() {
  const config = useRuntimeConfig()
  const API_BASE_URL = config.public.apiBaseUrl
  try {
    const token = getToken()
    if (!token) {
      notificationState.unreadCount = 0
      return 0
    }
    const res = await fetch(`${API_BASE_URL}/api/notifications/unread-count`, {
      headers: { Authorization: `Bearer ${token}` },
    })
    if (!res.ok) {
      notificationState.unreadCount = 0
      return 0
    }
    const data = await res.json()
    notificationState.unreadCount = data.count
    return data.count
  } catch (e) {
    notificationState.unreadCount = 0
    return 0
  }
}

export async function markNotificationsRead(ids) {
  try {
    const config = useRuntimeConfig()
    const API_BASE_URL = config.public.apiBaseUrl

    const token = getToken()
    if (!token || !ids || ids.length === 0) return false
    const res = await fetch(`${API_BASE_URL}/api/notifications/read`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ ids }),
    })
    return res.ok
  } catch (e) {
    return false
  }
}

export async function fetchNotificationPreferences() {
  try {
    const config = useRuntimeConfig()
    const API_BASE_URL = config.public.apiBaseUrl

    const token = getToken()
    if (!token) return []
    const res = await fetch(`${API_BASE_URL}/api/notifications/prefs`, {
      headers: { Authorization: `Bearer ${token}` },
    })
    if (!res.ok) return []
    return await res.json()
  } catch (e) {
    return []
  }
}

export async function updateNotificationPreference(type, enabled) {
  try {
    const config = useRuntimeConfig()
    const API_BASE_URL = config.public.apiBaseUrl
    const token = getToken()
    if (!token) return false
    const res = await fetch(`${API_BASE_URL}/api/notifications/prefs`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ type, enabled }),
    })
    return res.ok
  } catch (e) {
    return false
  }
}

export async function fetchEmailNotificationPreferences() {
  try {
    const config = useRuntimeConfig()
    const API_BASE_URL = config.public.apiBaseUrl

    const token = getToken()
    if (!token) return []
    const res = await fetch(`${API_BASE_URL}/api/notifications/email-prefs`, {
      headers: { Authorization: `Bearer ${token}` },
    })
    if (!res.ok) return []
    return await res.json()
  } catch (e) {
    return []
  }
}

export async function updateEmailNotificationPreference(type, enabled) {
  try {
    const config = useRuntimeConfig()
    const API_BASE_URL = config.public.apiBaseUrl
    const token = getToken()
    if (!token) return false
    const res = await fetch(`${API_BASE_URL}/api/notifications/email-prefs`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ type, enabled }),
    })
    return res.ok
  } catch (e) {
    return false
  }
}

/**
 * 处理信息的高阶函数
 * @returns
 */
function createFetchNotifications() {
  const notifications = ref([])
  const isLoadingMessage = ref(false)
  const hasMore = ref(true)

  const fetchNotifications = async ({
    page = 0,
    size = 30,
    unread = false,
    append = false,
  } = {}) => {
    const config = useRuntimeConfig()
    const API_BASE_URL = config.public.apiBaseUrl
    try {
      const token = getToken()
      if (!token) {
        toast.error('请先登录')
        return
      }
      if (!append) notifications.value = []
      isLoadingMessage.value = true
      const res = await fetch(
        `${API_BASE_URL}/api/notifications${unread ? '/unread' : ''}?page=${page}&size=${size}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        },
      )
      isLoadingMessage.value = false
      if (!res.ok) {
        toast.error('获取通知失败')
        return
      }
      const data = await res.json()
      const arr = []

      for (const n of data) {
        if (n.type === 'COMMENT_REPLY') {
          arr.push({
            ...n,
            src: n.comment.author.avatar,
            iconClick: () => {
              markNotificationRead(n.id)
              navigateTo(`/users/${n.comment.author.id}`, { replace: true })
            },
          })
        } else if (n.type === 'REACTION') {
          arr.push({
            ...n,
            emoji: reactionEmojiMap[n.reactionType],
            iconClick: () => {
              if (n.fromUser) {
                markNotificationRead(n.id)
                navigateTo(`/users/${n.fromUser.id}`, { replace: true })
              }
            },
          })
        } else if (n.type === 'POST_VIEWED') {
          arr.push({
            ...n,
            src: n.fromUser ? n.fromUser.avatar : null,
            icon: n.fromUser ? undefined : iconMap[n.type],
            iconClick: () => {
              if (n.fromUser) {
                markNotificationRead(n.id)
                navigateTo(`/users/${n.fromUser.id}`, { replace: true })
              }
            },
          })
        } else if (n.type === 'POST_DELETED') {
          arr.push({
            ...n,
            src: n.fromUser ? n.fromUser.avatar : null,
            icon: n.fromUser ? undefined : iconMap[n.type],
            iconClick: () => {
              if (n.fromUser) {
                markNotificationRead(n.id)
                navigateTo(`/users/${n.fromUser.id}`, { replace: true })
              }
            },
          })
        } else if (n.type === 'LOTTERY_WIN' || n.type === 'LOTTERY_DRAW') {
          arr.push({
            ...n,
            icon: iconMap[n.type],
            iconClick: () => {
              if (n.post) {
                markNotificationRead(n.id)
                navigateTo(`/posts/${n.post.id}`)
              }
            },
          })
        } else if (
          n.type === 'POLL_VOTE' ||
          n.type === 'POLL_RESULT_OWNER' ||
          n.type === 'POLL_RESULT_PARTICIPANT'
        ) {
          arr.push({
            ...n,
            icon: iconMap[n.type],
            iconClick: () => {
              if (n.post) {
                markNotificationRead(n.id)
                navigateTo(`/posts/${n.post.id}`)
              }
            },
          })
        } else if (n.type === 'POST_UPDATED' || n.type === 'USER_ACTIVITY') {
          arr.push({
            ...n,
            src: n.comment.author.avatar,
            iconClick: () => {
              markNotificationRead(n.id)
              navigateTo(`/users/${n.comment.author.id}`, { replace: true })
            },
          })
        } else if (n.type === 'MENTION') {
          arr.push({
            ...n,
            icon: iconMap[n.type],
            iconClick: () => {
              if (n.fromUser) {
                markNotificationRead(n.id)
                navigateTo(`/users/${n.fromUser.id}`, { replace: true })
              }
            },
          })
        } else if (n.type === 'USER_FOLLOWED' || n.type === 'USER_UNFOLLOWED') {
          arr.push({
            ...n,
            icon: iconMap[n.type],
            iconClick: () => {
              if (n.fromUser) {
                markNotificationRead(n.id)
                navigateTo(`/users/${n.fromUser.id}`, { replace: true })
              }
            },
          })
        } else if (
          n.type === 'FOLLOWED_POST' ||
          n.type === 'POST_SUBSCRIBED' ||
          n.type === 'POST_UNSUBSCRIBED'
        ) {
          arr.push({
            ...n,
            icon: iconMap[n.type],
            iconClick: () => {
              if (n.post) {
                markNotificationRead(n.id)
                navigateTo(`/posts/${n.post.id}`, { replace: true })
              }
            },
          })
        } else if (n.type === 'POST_REVIEW_REQUEST') {
          arr.push({
            ...n,
            src: n.fromUser ? n.fromUser.avatar : null,
            icon: n.fromUser ? undefined : iconMap[n.type],
            iconClick: () => {
              if (n.post) {
                markNotificationRead(n.id)
                navigateTo(`/posts/${n.post.id}`, { replace: true })
              }
            },
          })
        } else if (n.type === 'POST_FEATURED') {
          arr.push({
            ...n,
            icon: iconMap[n.type],
            iconClick: () => {
              if (n.post) {
                markNotificationRead(n.id)
                navigateTo(`/posts/${n.post.id}`, { replace: true })
              }
            },
          })
        } else if (n.type === 'REGISTER_REQUEST') {
          arr.push({
            ...n,
            icon: iconMap[n.type],
            iconClick: () => {},
          })
        } else {
          arr.push({
            ...n,
            icon: iconMap[n.type],
          })
        }
      }

      if (append) notifications.value.push(...arr)
      else notifications.value = arr
      hasMore.value = data.length === size
    } catch (e) {
      console.error(e)
      isLoadingMessage.value = false
    }
  }

  const markNotificationRead = async (id) => {
    if (!id) return
    const n = notifications.value.find((n) => n.id === id)
    if (!n || n.read) return
    n.read = true
    if (notificationState.unreadCount > 0) notificationState.unreadCount--
    const ok = await markNotificationsRead([id])
    if (!ok) {
      n.read = false
      notificationState.unreadCount++
    } else {
      fetchUnreadCount()
    }
  }

  const markAllRead = async () => {
    // 除了 REGISTER_REQUEST 类型消息
    const idsToMark = notifications.value
      .filter((n) => n.type !== 'REGISTER_REQUEST' && !n.read)
      .map((n) => n.id)
    if (idsToMark.length === 0) return
    notifications.value.forEach((n) => {
      if (n.type !== 'REGISTER_REQUEST') n.read = true
    })
    notificationState.unreadCount = notifications.value.filter((n) => !n.read).length
    const ok = await markNotificationsRead(idsToMark)
    if (!ok) {
      notifications.value.forEach((n) => {
        if (idsToMark.includes(n.id)) n.read = false
      })
      await fetchUnreadCount()
      return
    }
    fetchUnreadCount()
    if (authState.role === 'ADMIN') {
      toast.success('已读所有消息（注册请求除外）')
    } else {
      toast.success('已读所有消息')
    }
  }
  return {
    fetchNotifications,
    markNotificationRead,
    notifications,
    isLoadingMessage,
    markAllRead,
    hasMore,
  }
}

export const {
  fetchNotifications,
  markNotificationRead,
  notifications,
  isLoadingMessage,
  markAllRead,
  hasMore,
} = createFetchNotifications()

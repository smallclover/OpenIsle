import { reactive } from 'vue'

const TOKEN_KEY = 'token'

export const authState = reactive({
  loggedIn: false,
  userId: null,
  username: null,
  role: null,
  avatar: null,
})

if (import.meta.client) {
  authState.loggedIn =
    localStorage.getItem(TOKEN_KEY) !== null && localStorage.getItem(TOKEN_KEY) !== ''
}

export function getToken() {
  return import.meta.client ? localStorage.getItem(TOKEN_KEY) : null
}

export async function setToken(token) {
  if (import.meta.client) {
    localStorage.setItem(TOKEN_KEY, token)
    await loadCurrentUser()
  }
}

export function clearToken() {
  if (import.meta.client) {
    localStorage.removeItem(TOKEN_KEY)
    clearUserInfo()
    authState.loggedIn = false
  }
}

export function setUserInfo(user) {
  if (import.meta.client) {
    authState.userId = user.id
    authState.username = user.username
    authState.avatar = user.avatar
    authState.role = user.role
  }
}

export function clearUserInfo() {
  if (import.meta.client) {
    authState.userId = null
    authState.username = null
    authState.avatar = null
    authState.role = null
  }
}

export async function fetchCurrentUser() {
  const config = useRuntimeConfig()
  const API_BASE_URL = config.public.apiBaseUrl
  const token = getToken()
  if (!token) return null
  try {
    const res = await fetch(`${API_BASE_URL}/api/users/me`, {
      headers: { Authorization: `Bearer ${token}` },
    })
    if (!res.ok) return null
    return await res.json()
  } catch (e) {
    return null
  }
}

export async function loadCurrentUser() {
  const user = await fetchCurrentUser()
  if (user) {
    setUserInfo(user)
  } else {
    clearUserInfo()
  }
  authState.loggedIn = user !== null
}

export function isLogin() {
  return authState.loggedIn
}

export async function checkToken() {
  const config = useRuntimeConfig()
  const API_BASE_URL = config.public.apiBaseUrl
  const token = getToken()
  if (!token) return false
  try {
    const res = await fetch(`${API_BASE_URL}/api/auth/check`, {
      headers: { Authorization: `Bearer ${token}` },
    })
    if (res.ok) {
      await setToken(token)
    } else {
      clearToken()
    }
  } catch (e) {
    clearToken()
  }
}

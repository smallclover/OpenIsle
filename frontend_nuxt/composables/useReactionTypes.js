import { ref } from 'vue'

const reactionTypes = ref([])
let isLoading = false
let isInitialized = false

export function useReactionTypes() {
  const config = useRuntimeConfig()
  const API_BASE_URL = config.public.apiBaseUrl

  const fetchReactionTypes = async () => {
    if (isInitialized || isLoading) {
      reactionTypes.value = [...(window.reactionTypes || [])]
      return reactionTypes.value
    }

    isLoading = true
    try {
      const token = getToken()
      const res = await fetch(`${API_BASE_URL}/api/reaction-types`, {
        headers: { Authorization: token ? `Bearer ${token}` : '' },
      })
      if (res.ok) {
        reactionTypes.value = await res.json()
        window.reactionTypes = [...reactionTypes.value]
        isInitialized = true
      } else {
        reactionTypes.value = []
      }
    } catch (error) {
      console.error('Failed to fetch reaction types:', error)
      reactionTypes.value = []
    } finally {
      isLoading = false
    }
    return reactionTypes.value
  }

  const initialize = async () => {
    if (!isInitialized) {
      await fetchReactionTypes()
    }
    return reactionTypes.value
  }

  return {
    reactionTypes: readonly(reactionTypes),
    fetchReactionTypes,
    initialize,
    isInitialized: readonly(isInitialized),
  }
}

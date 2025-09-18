import { ref, readonly, watch } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs.min.js'
import { useRuntimeConfig } from '#app'

const client = ref(null)
const isConnected = ref(false)
const activeSubscriptions = ref(new Map())
// Store callbacks to allow for re-subscription after reconnect
const resubscribeCallbacks = new Map()

// Helper for unified subscription logging
const logSubscriptionActivity = (action, destination, subscriptionId = 'N/A') => {
  console.log(
    `[SUB_MAN] ${action} | Dest: ${destination} | SubID: ${subscriptionId} | Active: ${activeSubscriptions.value.size}`,
  )
}

const connect = (token) => {
  if (isConnected.value || (client.value && client.value.active)) {
    return
  }

  const config = useRuntimeConfig()
  const WEBSOCKET_URL = config.public.websocketUrl
  const socketUrl = `${WEBSOCKET_URL}/api/sockjs`

  const stompClient = new Client({
    webSocketFactory: () => new SockJS(socketUrl),
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    debug: function (str) {},
    reconnectDelay: 10000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  })

  stompClient.onConnect = (frame) => {
    isConnected.value = true
    resubscribeCallbacks.forEach((callback, destination) => {
      doSubscribe(destination, callback)
    })
  }

  stompClient.onStompError = (frame) => {
    console.error('Full frame:', frame)
  }

  stompClient.onWebSocketError = (event) => {}

  stompClient.onWebSocketClose = (event) => {
    isConnected.value = false
    activeSubscriptions.value.clear()
    logSubscriptionActivity('Cleared all subscriptions due to WebSocket close', 'N/A')
  }

  stompClient.onDisconnect = (frame) => {
    isConnected.value = false
  }

  stompClient.activate()
  client.value = stompClient
}

const unsubscribe = (destination) => {
  if (!destination) {
    return false
  }
  const subscription = activeSubscriptions.value.get(destination)
  if (subscription) {
    try {
      subscription.unsubscribe()
      logSubscriptionActivity('Unsubscribed', destination, subscription.id)
    } catch (e) {
      console.error(`Error during unsubscribe for ${destination}:`, e)
    } finally {
      activeSubscriptions.value.delete(destination)
      resubscribeCallbacks.delete(destination)
    }
    return true
  } else {
    return false
  }
}

const unsubscribeAll = () => {
  logSubscriptionActivity('Unsubscribing from ALL', `Total: ${activeSubscriptions.value.size}`)
  const destinations = [...activeSubscriptions.value.keys()]
  destinations.forEach((dest) => {
    unsubscribe(dest)
  })
}

const disconnect = () => {
  unsubscribeAll()
  if (client.value) {
    try {
      client.value.deactivate()
    } catch (e) {
      console.error('Error during client deactivation:', e)
    }
    client.value = null
    isConnected.value = false
  }
}

const doSubscribe = (destination, callback) => {
  try {
    if (!client.value || !client.value.connected) {
      return null
    }

    if (activeSubscriptions.value.has(destination)) {
      unsubscribe(destination)
    }

    const subscription = client.value.subscribe(destination, (message) => {
      callback(message)
    })

    if (subscription) {
      activeSubscriptions.value.set(destination, subscription)
      resubscribeCallbacks.set(destination, callback) // Store for re-subscription
      logSubscriptionActivity('Subscribed', destination, subscription.id)
      return subscription
    } else {
      return null
    }
  } catch (error) {
    console.error(`Exception during subscription to ${destination}:`, error)
    return null
  }
}

const subscribe = (destination, callback) => {
  if (!destination) {
    return Promise.resolve(null)
  }

  return new Promise((resolve) => {
    if (client.value && client.value.connected) {
      const sub = doSubscribe(destination, callback)
      resolve(sub)
    } else {
      const unwatch = watch(
        isConnected,
        (newVal) => {
          if (newVal) {
            setTimeout(() => {
              const sub = doSubscribe(destination, callback)
              unwatch()
              resolve(sub)
            }, 100)
          }
        },
        { immediate: false },
      )

      setTimeout(() => {
        unwatch()
        if (!isConnected.value) {
          resolve(null)
        }
      }, 15000)
    }
  })
}

export function useWebSocket() {
  return {
    client: readonly(client),
    isConnected,
    connect,
    disconnect,
    subscribe,
    unsubscribe,
    unsubscribeAll,
    activeSubscriptions: readonly(activeSubscriptions),
  }
}

export const isClient = typeof window !== 'undefined' && typeof document !== 'undefined'

export const isMac = getIsMac()

function getIsMac() {
  if (!isClient) {
    return false
  }

  try {
    // 优先使用现代浏览器的 navigator.userAgentData API
    if (navigator.userAgentData && navigator.userAgentData.platform) {
      return navigator.userAgentData.platform === 'macOS'
    }

    // 降级到传统的 User-Agent 检测
    if (navigator.userAgent) {
      return /Mac|iPhone|iPad|iPod/i.test(navigator.userAgent)
    }

    // 默认返回false
    return false
  } catch (error) {
    // 异常处理，记录错误并返回默认值
    console.warn('检测Mac设备时发生错误:', error)
    return false
  }
}

/**
 * 文件上传配置
 */

export const UPLOAD_CONFIG = {
  // 视频文件配置
  VIDEO: {
    // 文件大小限制 (字节)
    MAX_SIZE: 20 * 1024 * 1024,

    // 支持的输入格式
    SUPPORTED_FORMATS: ['mp4', 'webm', 'avi', 'mov', 'wmv', 'flv', 'mkv', 'm4v', 'ogv'],
  },

  // 图片文件配置
  IMAGE: {
    MAX_SIZE: 5 * 1024 * 1024, // 5MB
    TARGET_SIZE: 5 * 1024 * 1024, // 5MB
    SUPPORTED_FORMATS: ['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg', 'bmp'],
  },

  // 音频文件配置
  AUDIO: {
    MAX_SIZE: 5 * 1024 * 1024, // 5MB
    TARGET_SIZE: 5 * 1024 * 1024, // 5MB
    SUPPORTED_FORMATS: ['mp3', 'wav', 'ogg', 'aac', 'm4a'],
  },

  // 通用文件配置
  GENERAL: {
    MAX_SIZE: 100 * 1024 * 1024, // 100MB
    CHUNK_SIZE: 5 * 1024 * 1024, // 5MB 分片大小
  },

  // 用户体验配置
  UI: {
    SUCCESS_DURATION: 2000,
    ERROR_DURATION: 3000,
    WARNING_DURATION: 3000,
  },
}

/**
 * 获取文件类型配置
 */
export function getFileTypeConfig(filename) {
  const ext = filename.split('.').pop().toLowerCase()

  if (UPLOAD_CONFIG.VIDEO.SUPPORTED_FORMATS.includes(ext)) {
    return { type: 'video', config: UPLOAD_CONFIG.VIDEO }
  }

  if (UPLOAD_CONFIG.IMAGE.SUPPORTED_FORMATS.includes(ext)) {
    return { type: 'image', config: UPLOAD_CONFIG.IMAGE }
  }

  if (UPLOAD_CONFIG.AUDIO.SUPPORTED_FORMATS.includes(ext)) {
    return { type: 'audio', config: UPLOAD_CONFIG.AUDIO }
  }

  return { type: 'general', config: UPLOAD_CONFIG.GENERAL }
}

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

/**
 * 计算压缩节省的费用 (示例函数)
 */
export function calculateSavings(originalSize, compressedSize, costPerMB = 0.01) {
  const originalMB = originalSize / (1024 * 1024)
  const compressedMB = compressedSize / (1024 * 1024)
  const savedMB = originalMB - compressedMB
  const savedCost = savedMB * costPerMB

  return {
    savedMB: savedMB.toFixed(2),
    savedCost: savedCost.toFixed(4),
    originalCost: (originalMB * costPerMB).toFixed(4),
    compressedCost: (compressedMB * costPerMB).toFixed(4),
  }
}

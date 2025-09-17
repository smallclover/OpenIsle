/**
 * 视频上传工具
 */

import { UPLOAD_CONFIG } from '../config/uploadConfig.js'

// 导出配置供外部使用
export const VIDEO_CONFIG = UPLOAD_CONFIG.VIDEO

/**
 * 检查文件大小是否超出限制
 */
export function checkFileSize(file) {
  return {
    isValid: file.size <= VIDEO_CONFIG.MAX_SIZE,
    actualSize: file.size,
    maxSize: VIDEO_CONFIG.MAX_SIZE,
  }
}

/**
 * 格式化文件大小显示
 */
export function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

/**
 * WebCodecs + MP4Box.js video compressor
 * Simplified transcoding using browser WebCodecs API
 */
import { createFile } from 'mp4box'
import { UPLOAD_CONFIG } from '../config/uploadConfig.js'

export function isWebCodecSupported() {
  return (
    typeof window !== 'undefined' &&
    'VideoEncoder' in window &&
    'MediaStreamTrackProcessor' in window &&
    'VideoFrame' in window
  )
}

/**
 * Compress a video File using WebCodecs and MP4Box.js
 * @param {File} file original video file
 * @param {Object} options optional callbacks
 * @param {Function} options.onProgress progress callback
 * @returns {Promise<File>} compressed file
 */
export async function compressVideoWithWebCodecs(file, { onProgress = () => {} } = {}) {
  if (!isWebCodecSupported()) {
    throw new Error('当前浏览器不支持 WebCodecs')
  }

  onProgress({ stage: 'initializing', progress: 0 })

  const url = URL.createObjectURL(file)
  const video = document.createElement('video')
  video.src = url
  await video.play()
  video.pause()

  onProgress({ stage: 'preparing', progress: 10 })

  const stream = video.captureStream()
  const track = stream.getVideoTracks()[0]
  const processor = new MediaStreamTrackProcessor({ track })
  const reader = processor.readable.getReader()

  const { width, height, frameRate = 30 } = track.getSettings()
  const bitrate = UPLOAD_CONFIG.VIDEO.TARGET_BITRATE || 1_000_000

  const chunks = []
  const encoder = new VideoEncoder({
    output: (chunk) => {
      const copy = new Uint8Array(chunk.byteLength)
      chunk.copyTo(copy)
      chunks.push({ type: chunk.type, timestamp: chunk.timestamp, data: copy })
    },
    error: (e) => console.error('编码失败', e),
  })

  encoder.configure({
    codec: 'avc1.42001E',
    width,
    height,
    bitrate,
    framerate: frameRate,
  })

  let processed = 0
  const totalFrames = Math.ceil(video.duration * frameRate)

  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    encoder.encode(value)
    value.close()
    processed++
    onProgress({ stage: 'compressing', progress: Math.round((processed / totalFrames) * 80) })
  }

  await encoder.flush()

  onProgress({ stage: 'packaging', progress: 90 })

  const mp4 = createFile()
  const trackId = mp4.addTrack({
    id: 1,
    type: 'avc1',
    width,
    height,
    timescale: frameRate,
  })

  chunks.forEach((chunk) => {
    mp4.addSample(trackId, chunk.data.buffer, {
      duration: 1,
      dts: chunk.timestamp,
      cts: 0,
      is_sync: chunk.type === 'key',
    })
  })

  const streamOut = mp4.getBuffer()
  const outBuffer = streamOut.buffer.slice(0, streamOut.position)
  const outFile = new File([outBuffer], file.name.replace(/\.[^.]+$/, '.mp4'), {
    type: 'video/mp4',
  })

  onProgress({ stage: 'completed', progress: 100 })

  return outFile
}

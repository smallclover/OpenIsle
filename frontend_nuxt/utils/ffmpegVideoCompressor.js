/**
 * FFmpeg.wasm 视频压缩器
 *
 * 用法：
 *   const { $ffmpeg } = useNuxtApp()
 *   const ff = await $ffmpeg() // 插件里已完成 ffmpeg.load()
 *   const out = await compressVideoWithFFmpeg(ff, file, { onProgress, strictSize: false })
 *
 * 设计要点：
 *  - 本文件不再负责加载/初始化，只负责转码逻辑；和 Nuxt 插件解耦。
 *  - 针对【同一个 ffmpeg 实例】做串行队列，避免并发 exec 踩内存文件系统。
 *  - 使用 nanoid 生成唯一文件名；日志环形缓冲；默认 CRF+VBV，可选 strictSize（two-pass）。
 *  - 体积明显小于目标时直通返回，减少无谓重编码。
 */

import { fetchFile } from '@ffmpeg/util'
import { nanoid } from 'nanoid'
import { UPLOAD_CONFIG } from '../config/uploadConfig.js'

/*************************
 * 每实例一个串行队列     *
 *************************/
// WeakMap<FFmpeg, { q: (()=>Promise<any>)[], running: boolean, resolvers: {res,rej}[] }>
const queues = new WeakMap()

function enqueueOn(instance, taskFn) {
  return new Promise((res, rej) => {
    let st = queues.get(instance)
    if (!st) {
      st = { q: [], running: false, resolvers: [] }
      queues.set(instance, st)
    }
    st.q.push(taskFn)
    st.resolvers.push({ res, rej })
    drain(instance)
  })
}

async function drain(instance) {
  const st = queues.get(instance)
  if (!st || st.running) return
  st.running = true
  try {
    while (st.q.length) {
      const task = st.q.shift()
      const rr = st.resolvers.shift()
      try {
        rr.res(await task())
      } catch (e) {
        rr.rej(e)
      }
    }
  } finally {
    st.running = false
  }
}

/*****************
 * 工具函数       *
 *****************/
function decideScale(widthHint) {
  if (!widthHint) return { filter: null, width: null }
  const evenW = widthHint % 2 === 0 ? widthHint : widthHint - 1
  return { filter: `scale=${evenW}:-2:flags=bicubic,setsar=1`, width: evenW }
}

function calculateParamsByRatio(originalSize, targetSize) {
  const ratio = Math.min(targetSize / originalSize, 1)
  const crf = ratio < 0.35 ? 29 : ratio < 0.5 ? 27 : ratio < 0.7 ? 25 : 23
  const preset = ratio < 0.35 ? 'slow' : ratio < 0.5 ? 'medium' : 'veryfast'
  const s =
    ratio < 0.35
      ? decideScale(720)
      : ratio < 0.6
        ? decideScale(960)
        : ratio < 0.8
          ? decideScale(1280)
          : { filter: null, width: null }
  const audioBitrateK = ratio < 0.5 ? 96 : ratio < 0.7 ? 128 : 160
  const profile = s.width && s.width <= 1280 ? 'main' : 'high'
  return { crf, preset, scaleFilter: s.filter, scaleWidth: s.width, audioBitrateK, profile }
}

function makeRingLogger(capBytes = 4000) {
  const buf = []
  let total = 0
  function push(s) {
    if (!s) return
    buf.push(s)
    total += s.length
    while (total > capBytes) total -= buf.shift().length
  }
  return { push, dump: () => buf.slice() }
}

function parseDurationFromLogs(logs) {
  // 避免正则：查找 Duration: 后的 00:00:00.xx
  const text = logs.join(' ')
  const idx = text.indexOf('Duration:')
  if (idx === -1) return null
  let i = idx + 'Duration:'.length
  while (i < text.length && text[i] === ' ') i++
  function read2(start) {
    const a = text.charCodeAt(start) - 48
    const b = text.charCodeAt(start + 1) - 48
    if (a < 0 || a > 9 || b < 0 || b > 9) return null
    return a * 10 + b
  }
  const hh = read2(i)
  if (hh === null) return null
  i += 2
  if (text[i++] !== ':') return null
  const mm = read2(i)
  if (mm === null) return null
  i += 2
  if (text[i++] !== ':') return null
  const s1 = read2(i)
  if (s1 === null) return null
  i += 2
  if (text[i++] !== '.') return null
  let j = i
  while (j < text.length && text.charCodeAt(j) >= 48 && text.charCodeAt(j) <= 57) j++
  const frac = parseFloat('0.' + text.slice(i, j) || '0')
  return hh * 3600 + mm * 60 + s1 + frac
}

export function isFFmpegSupported() {
  return typeof WebAssembly !== 'undefined' && typeof Worker !== 'undefined'
}

/**
 * 读取 ffmpeg 核心版本（通过 -version），会进入队列避免并发冲突
 */
export async function getFFmpegInfo(ffmpegInstance) {
  return enqueueOn(ffmpegInstance, async () => {
    const logs = []
    const onLog = ({ type, message }) => {
      if (type === 'info' || type === 'fferr') logs.push(message)
    }
    ffmpegInstance.on('log', onLog)
    try {
      await ffmpegInstance.exec(['-version'])
    } finally {
      ffmpegInstance.off('log', onLog)
    }
    const line = logs.find((l) => l.toLowerCase().includes('ffmpeg version')) || ''
    const parts = line.trim().split(' ').filter(Boolean)
    const version = parts.length > 2 ? parts[2] : parts[1] || null
    return { version }
  })
}

/**
 * 压缩：接受一个已经 load() 完成的 ffmpeg 实例
 * @param {*} ffmpegInstance 已初始化的 FFmpeg 实例（来自 Nuxt 插件）
 * @param {File|Blob} file 输入文件
 * @param {{ onProgress?:(p:{stage:string,progress:number})=>void, signal?:AbortSignal, strictSize?:boolean, targetSize?:number }} opts
 */
export async function compressVideoWithFFmpeg(ffmpegInstance, file, opts = {}) {
  return enqueueOn(ffmpegInstance, () => doCompress(ffmpegInstance, file, opts))
}

async function doCompress(ffmpegInstance, file, opts) {
  const onProgress = opts.onProgress || (() => {})
  const { signal, strictSize = false } = opts

  onProgress({ stage: 'preparing', progress: 10 })

  const targetSize = opts.targetSize ?? UPLOAD_CONFIG?.VIDEO?.TARGET_SIZE ?? 12 * 1024 * 1024

  // 小体积直通
  const sizeKnown = 'size' in file && typeof file.size === 'number'
  if (sizeKnown && file.size <= targetSize * 0.9) {
    onProgress({ stage: 'skipped', progress: 100 })
    return file
  }

  const params = calculateParamsByRatio(sizeKnown ? file.size : targetSize * 2, targetSize)
  const { crf, preset, scaleFilter, audioBitrateK, profile } = params

  const name = 'name' in file && typeof file.name === 'string' ? file.name : 'input.mp4'
  const dot = name.lastIndexOf('.')
  const outName = (dot > -1 ? name.slice(0, dot) : name) + '.mp4'

  const ext = dot > -1 ? name.slice(dot + 1).toLowerCase() : 'mp4'
  const id = nanoid()
  const inputName = `input-${id}.${ext}`
  const outputName = `output-${id}.mp4`
  const passlog = `ffpass-${id}`

  // 监听
  const ring = makeRingLogger()
  const onFfmpegProgress = ({ progress: p }) => {
    const adjusted = 20 + p * 70
    onProgress({ stage: 'compressing', progress: Math.min(90, adjusted) })
  }
  const onFfmpegLog = ({ type, message }) => {
    if (type === 'fferr' || type === 'info') ring.push(message)
  }
  ffmpegInstance.on('progress', onFfmpegProgress)
  ffmpegInstance.on('log', onFfmpegLog)

  let aborted = false
  const abortHandler = () => {
    aborted = true
  }
  if (signal) signal.addEventListener('abort', abortHandler, { once: true })

  try {
    await ffmpegInstance.writeFile(inputName, await fetchFile(file))
    onProgress({ stage: 'analyzing', progress: 20 })

    let durationSec = null
    try {
      await ffmpegInstance.exec(['-hide_banner', '-i', inputName, '-f', 'null', '-'])
      durationSec = parseDurationFromLogs(ring.dump())
    } catch {
      durationSec = durationSec ?? parseDurationFromLogs(ring.dump())
    }

    let videoBitrate = null
    if (durationSec && sizeKnown && targetSize < file.size) {
      const totalTargetBits = targetSize * 8
      const audioBits = audioBitrateK * 1000 * durationSec
      const maxVideoBits = Math.max(totalTargetBits - audioBits, totalTargetBits * 0.7)
      const bps = Math.max(180000, Math.floor(maxVideoBits / durationSec))
      videoBitrate = String(Math.min(bps, 5000000))
    }

    const baseArgs = [
      '-hide_banner',
      '-i',
      inputName,
      '-c:v',
      'libx264',
      '-pix_fmt',
      'yuv420p',
      '-profile:v',
      profile,
      '-movflags',
      '+faststart',
      '-preset',
      preset,
      '-c:a',
      'aac',
      '-b:a',
      `${audioBitrateK}k`,
      '-ac',
      '2',
    ]
    if (scaleFilter) baseArgs.push('-vf', scaleFilter)

    const onePassArgs = [...baseArgs, '-crf', String(crf)]
    if (videoBitrate)
      onePassArgs.push('-maxrate', videoBitrate, '-bufsize', String(parseInt(videoBitrate, 10) * 2))

    const twoPassFirst = [
      '-y',
      '-hide_banner',
      '-i',
      inputName,
      '-c:v',
      'libx264',
      '-b:v',
      `${videoBitrate || '1000000'}`,
      '-pass',
      '1',
      '-passlogfile',
      passlog,
      '-an',
      '-f',
      'mp4',
      '/dev/null',
    ]
    const twoPassSecond = [
      ...baseArgs,
      '-b:v',
      `${videoBitrate || '1000000'}`,
      '-pass',
      '2',
      '-passlogfile',
      passlog,
      outputName,
    ]

    if (aborted) throw new DOMException('Aborted', 'AbortError')

    if (!strictSize) {
      await ffmpegInstance.exec([...onePassArgs, outputName])
    } else {
      if (!videoBitrate) videoBitrate = '1000000'
      await ffmpegInstance.exec(twoPassFirst)
      onProgress({ stage: 'second-pass', progress: 85 })
      await ffmpegInstance.exec(twoPassSecond)
    }

    if (aborted) throw new DOMException('Aborted', 'AbortError')

    onProgress({ stage: 'finalizing', progress: 95 })
    const out = await ffmpegInstance.readFile(outputName)

    const mime = 'video/mp4'
    const blob = new Blob([out], { type: mime })
    const hasFileCtor = typeof File === 'function'
    const result = hasFileCtor ? new File([blob], outName, { type: mime }) : blob

    onProgress({ stage: 'completed', progress: 100 })
    return result
  } finally {
    try {
      await ffmpegInstance.deleteFile(inputName)
    } catch {}
    try {
      await ffmpegInstance.deleteFile(outputName)
    } catch {}
    try {
      await ffmpegInstance.deleteFile(`${passlog}-0.log`)
    } catch {}
    try {
      await ffmpegInstance.deleteFile(`${passlog}-0.log.mbtree`)
    } catch {}

    ffmpegInstance.off('progress', onFfmpegProgress)
    ffmpegInstance.off('log', onFfmpegLog)
    if (signal) signal.removeEventListener('abort', abortHandler)
  }
}

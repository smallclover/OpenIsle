<template>
  <div class="post-poll-container" v-if="poll">
    <div class="poll-top-container">
      <div class="poll-options-container">
        <div v-if="showPollResult || pollEnded || hasVoted">
          <div v-for="(opt, idx) in poll.options" :key="idx" class="poll-option-result">
            <div class="poll-option-info-container">
              <div class="poll-option-text">{{ opt }}</div>
              <div class="poll-option-progress-info">
                {{ pollPercentages[idx] }}% ({{ pollVotes[idx] || 0 }}人已投票)
              </div>
            </div>
            <div class="poll-option-progress">
              <div
                class="poll-option-progress-bar"
                :style="{ width: pollPercentages[idx] + '%' }"
              ></div>
            </div>
            <div class="poll-participants">
              <BaseImage
                v-for="p in pollOptionParticipants[idx] || []"
                :key="p.id"
                class="poll-participant-avatar"
                :src="p.avatar"
                alt="avatar"
                @click="gotoUser(p.id)"
              />
            </div>
          </div>
        </div>
        <div v-else>
          <div class="poll-title-section">
            <div class="poll-option-title" v-if="poll.multiple">多选</div>
            <div class="poll-option-title" v-else>单选</div>

            <div class="poll-left-time">
              <stopwatch class="poll-left-time-icon" />
              <div class="poll-left-time-title">离结束</div>
              <div class="poll-left-time-value">{{ countdown }}</div>
            </div>
          </div>
          <template v-if="poll.multiple">
            <div
              v-for="(opt, idx) in poll.options"
              :key="idx"
              class="poll-option"
              @click="toggleOption(idx)"
            >
              <input
                type="checkbox"
                :checked="selectedOptions.includes(idx)"
                class="poll-option-input"
              />
              <span class="poll-option-text">{{ opt }}</span>
            </div>

            <div class="multi-selection-container">
              <div class="join-poll-button" @click="submitMultiPoll"><check /> 确认投票</div>
            </div>
          </template>
          <template v-else>
            <div
              v-for="(opt, idx) in poll.options"
              :key="idx"
              class="poll-option"
              @click="selectOption(idx)"
            >
              <input
                type="radio"
                :checked="selectedOption === idx"
                name="poll-option"
                class="poll-option-input"
              />
              <span class="poll-option-text">{{ opt }}</span>
            </div>

            <div class="single-selection-container">
              <div class="join-poll-button" @click="submitSinglePoll"><check /> 确认投票</div>
            </div>
          </template>
        </div>
      </div>
      <div class="poll-info">
        <div class="total-votes">{{ pollParticipants.length }}</div>
        <div class="total-votes-title">投票人</div>
      </div>
    </div>
    <div class="poll-bottom-container">
      <div
        v-if="showPollResult && !pollEnded && !hasVoted"
        class="poll-option-button"
        @click="showPollResult = false"
      >
        <arrow-left /> 投票
      </div>
      <div
        v-else-if="!pollEnded && !hasVoted"
        class="poll-option-button"
        @click="showPollResult = true"
      >
        <chart-histogram /> 结果
      </div>
      <div v-else-if="pollEnded" class="poll-option-hint"><stopwatch /> 投票已结束</div>
      <div v-else class="poll-option-hint">
        <div>您已投票，等待结束查看结果</div>
        <div class="poll-left-time">
          <stopwatch class="poll-left-time-icon" />
          <div class="poll-left-time-title">离结束</div>
          <div class="poll-left-time-value">{{ countdown }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { getToken, authState } from '~/utils/auth'
import { toast } from '~/main'
import { useRuntimeConfig } from '#imports'
import { useCountdown } from '~/composables/useCountdown'

const props = defineProps({
  poll: { type: Object, required: true },
  postId: { type: [String, Number], required: true },
})
const emit = defineEmits(['refresh'])

const loggedIn = computed(() => authState.loggedIn)
const showPollResult = ref(false)

const pollParticipants = computed(() => props.poll?.participants || [])
const pollOptionParticipants = computed(() => props.poll?.optionParticipants || {})
const pollVotes = computed(() => props.poll?.votes || {})
const totalPollVotes = computed(() => Object.values(pollVotes.value).reduce((a, b) => a + b, 0))
const pollPercentages = computed(() =>
  props.poll
    ? props.poll.options.map((_, idx) => {
        const c = pollVotes.value[idx] || 0
        return totalPollVotes.value ? ((c / totalPollVotes.value) * 100).toFixed(1) : 0
      })
    : [],
)
// 倒计时
const { countdown, isEnded } = useCountdown(props.poll?.endTime)
const pollEnded = computed(() => isEnded.value)
const hasVoted = computed(() => {
  if (!loggedIn.value) return false
  return pollParticipants.value.some((p) => p.id === Number(authState.userId))
})
watch([hasVoted, pollEnded], ([voted, ended]) => {
  if (voted || ended) showPollResult.value = true
})

const gotoUser = (id) => navigateTo(`/users/${id}`, { replace: true })

const config = useRuntimeConfig()
const API_BASE_URL = config.public.apiBaseUrl
const voteOption = async (idx) => {
  const token = getToken()
  if (!token) {
    toast.error('请先登录')
    return
  }
  const res = await fetch(`${API_BASE_URL}/api/posts/${props.postId}/poll/vote?option=${idx}`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` },
  })
  const data = await res.json().catch(() => ({}))
  if (res.ok) {
    toast.success('投票成功')
    emit('refresh')
    showPollResult.value = true
  } else {
    toast.error(data.error || '操作失败')
  }
}

const selectedOption = ref(null)
const selectOption = (idx) => {
  selectedOption.value = idx
}
const submitSinglePoll = async () => {
  if (selectedOption.value === null) {
    toast.error('请选择一个选项')
    return
  }
  await voteOption(selectedOption.value)
}

const selectedOptions = ref([])
const toggleOption = (idx) => {
  const i = selectedOptions.value.indexOf(idx)
  if (i >= 0) {
    selectedOptions.value.splice(i, 1)
  } else {
    selectedOptions.value.push(idx)
  }
}
const submitMultiPoll = async () => {
  const token = getToken()
  if (!token) {
    toast.error('请先登录')
    return
  }
  if (!selectedOptions.value.length) {
    toast.error('请选择至少一个选项')
    return
  }
  const params = selectedOptions.value.map((o) => `option=${o}`).join('&')
  const res = await fetch(`${API_BASE_URL}/api/posts/${props.postId}/poll/vote?${params}`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` },
  })
  const data = await res.json().catch(() => ({}))
  if (res.ok) {
    toast.success('投票成功')
    emit('refresh')
    showPollResult.value = true
  } else {
    toast.error(data.error || '操作失败')
  }
}
</script>

<style scoped>
.post-poll-container {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background-color: var(--lottery-background-color);
  border-radius: 10px;
  padding: 10px;
}

.poll-option-button {
  color: var(--text-color);
  padding: 5px 10px;
  border-radius: 8px;
  background-color: var(--poll-option-button-background-color);
  cursor: pointer;
  width: fit-content;
}

.poll-top-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  border-bottom: 1px solid var(--normal-border-color);
}

.poll-options-container {
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  flex: 4;
  border-right: 1px solid var(--normal-border-color);
}

.poll-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 100px;
}

.total-votes {
  font-size: 40px;
  font-weight: bold;
  opacity: 0.8;
}

.total-votes-title {
  font-size: 18px;
  opacity: 0.5;
}

.poll-option {
  margin-bottom: 10px;
  margin-right: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.poll-option-result {
  margin-bottom: 10px;
  margin-right: 10px;
  gap: 5px;
  display: flex;
  flex-direction: column;
}

.poll-option-input {
  margin-right: 10px;
  width: 18px;
  height: 18px;
  accent-color: var(--primary-color);
  border-radius: 50%;
  border: 2px solid var(--primary-color);
}

.poll-option-text {
  font-size: 18px;
}

.poll-bottom-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}

.poll-left-time {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.poll-left-time-icon {
  font-size: 13px;
}

.poll-option-hint {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.poll-left-time-title {
  font-size: 13px;
  opacity: 0.7;
}

.poll-left-time-value {
  font-size: 13px;
  font-weight: bold;
  color: var(--primary-color);
}

.poll-option-progress {
  position: relative;
  background-color: rgb(187, 187, 187);
  height: 20px;
  border-radius: 5px;
  overflow: hidden;
}

.poll-option-progress-bar {
  background-color: var(--primary-color);
  height: 100%;
}

.poll-option-info-container {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
}

.poll-option-progress-info {
  font-size: 12px;
  line-height: 20px;
  color: var(--text-color);
}

.multi-selection-container,
.single-selection-container {
  margin-top: 30px;
  margin-bottom: 10px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
}

.multi-selection-title,
.single-selection-title {
  font-size: 13px;
  color: var(--text-color);
}

.poll-title-section {
  display: flex;
  gap: 30px;
  flex-direction: row;
  margin-bottom: 20px;
}

.poll-option-title {
  font-size: 18px;
  font-weight: bold;
}

.poll-left-time {
  font-size: 18px;
}

.info-icon {
  margin-right: 5px;
}

.join-poll-button {
  padding: 5px 10px;
  background-color: var(--primary-color);
  color: white;
  border-radius: 8px;
  cursor: pointer;
}

.join-poll-button:hover {
  background-color: var(--primary-color-hover);
}

.poll-participants {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.poll-participant-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
}
</style>

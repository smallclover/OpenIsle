<template>
  <div class="post-prize-container" v-if="lottery">
    <div class="prize-content">
      <div class="prize-info">
        <div class="prize-info-left">
          <div class="prize-icon">
            <BaseImage
              class="prize-icon-img"
              v-if="lottery.prizeIcon"
              :src="lottery.prizeIcon"
              alt="prize"
            />
            <gift v-else class="default-prize-icon" />
          </div>
          <div class="prize-name">{{ lottery.prizeDescription }}</div>
          <div class="prize-count">x {{ lottery.prizeCount }}</div>
        </div>
        <div class="prize-end-time prize-info-right">
          <stopwatch v-if="!lotteryEnded" class="prize-end-time-icon" />
          <div v-if="!isMobile && !lotteryEnded" class="prize-end-time-title">离结束</div>
          <div class="prize-end-time-value">{{ countdown }}</div>
          <div v-if="!isMobile" class="join-prize-button-container-desktop">
            <div
              v-if="loggedIn && !hasJoined && !lotteryEnded"
              class="join-prize-button"
              @click="joinLottery"
            >
              <div class="join-prize-button-text">
                参与抽奖 <paper-money-two class="join-prize-button-text-icon" />
                {{ lottery.pointCost }}
              </div>
            </div>
            <div v-else-if="hasJoined" class="join-prize-button-disabled">
              <div class="join-prize-button-text">已参与</div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="isMobile" class="join-prize-button-container-mobile">
        <div
          v-if="loggedIn && !hasJoined && !lotteryEnded"
          class="join-prize-button"
          @click="joinLottery"
        >
          <div class="join-prize-button-text">
            参与抽奖 <paper-money-two class="join-prize-button-text-icon" /> {{ lottery.pointCost }}
          </div>
        </div>
        <div v-else-if="hasJoined" class="join-prize-button-disabled">
          <div class="join-prize-button-text">已参与</div>
        </div>
      </div>
    </div>
    <div class="prize-member-container">
      <BaseImage
        v-for="p in lotteryParticipants"
        :key="p.id"
        class="prize-member-avatar"
        :src="p.avatar"
        alt="avatar"
        @click="gotoUser(p.id)"
      />
      <div v-if="lotteryEnded && lotteryWinners.length" class="prize-member-winner">
        <medal-one class="medal-icon"></medal-one>
        <span class="prize-member-winner-name">获奖者: </span>
        <BaseImage
          v-for="w in lotteryWinners"
          :key="w.id"
          class="prize-member-avatar"
          :src="w.avatar"
          alt="avatar"
          @click="gotoUser(w.id)"
        />
        <div v-if="lotteryWinners.length === 1" class="prize-member-winner-name">
          {{ lotteryWinners[0].username }}
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
import { useIsMobile } from '~/utils/screen'
import { useCountdown } from '~/composables/useCountdown'

const props = defineProps({
  lottery: { type: Object, required: true },
  postId: { type: [String, Number], required: true },
})
const emit = defineEmits(['refresh'])

const isMobile = useIsMobile()
const loggedIn = computed(() => authState.loggedIn)
const lotteryParticipants = computed(() => props.lottery?.participants || [])
const lotteryWinners = computed(() => props.lottery?.winners || [])
// 倒计时和结束flg
const { countdown, isEnded } = useCountdown(props.lottery?.endTime)
const lotteryEnded = computed(() => isEnded.value)
const hasJoined = computed(() => {
  if (!loggedIn.value) return false
  return lotteryParticipants.value.some((p) => p.id === Number(authState.userId))
})

const gotoUser = (id) => navigateTo(`/users/${id}`, { replace: true })

const config = useRuntimeConfig()
const API_BASE_URL = config.public.apiBaseUrl
const joinLottery = async () => {
  const token = getToken()
  if (!token) {
    toast.error('请先登录')
    return
  }
  const res = await fetch(`${API_BASE_URL}/api/posts/${props.postId}/lottery/join`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` },
  })
  const data = await res.json().catch(() => ({}))
  if (res.ok) {
    toast.success('已参与抽奖')
    emit('refresh')
  } else {
    toast.error(data.error || '操作失败')
  }
}
</script>

<style scoped>
.post-prize-container {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background-color: var(--lottery-background-color);
  border-radius: 10px;
  padding: 10px;
}

.prize-info {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  width: 100%;
  align-items: center;
}

.join-prize-button-container-mobile {
  margin-top: 15px;
  margin-bottom: 10px;
}

.prize-icon {
  width: 24px;
  height: 24px;
}

.default-prize-icon {
  font-size: 24px;
  opacity: 0.5;
}

.prize-icon-img {
  width: 100%;
  height: 100%;
}

.prize-name {
  font-size: 13px;
  opacity: 0.7;
  margin-left: 10px;
}

.prize-count {
  font-size: 13px;
  font-weight: bold;
  opacity: 0.7;
  margin-left: 10px;
  color: var(--primary-color);
}

.prize-end-time {
  display: flex;
  flex-direction: row;
  align-items: center;
  font-size: 13px;
  opacity: 0.7;
  margin-left: 10px;
}

.prize-end-time-icon {
  font-size: 13px;
  margin-right: 5px;
}

.prize-end-time-title {
  font-size: 13px;
  opacity: 0.7;
  margin-right: 5px;
}

.prize-end-time-value {
  font-size: 13px;
  font-weight: bold;
  color: var(--primary-color);
}

.prize-info-left,
.prize-info-right {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.join-prize-button {
  margin-left: 10px;
  background-color: var(--primary-color);
  color: white;
  padding: 5px 10px;
  border-radius: 8px;
  cursor: pointer;
  text-align: center;
}

.join-prize-button:hover {
  background-color: var(--primary-color-hover);
}

.join-prize-button-disabled {
  text-align: center;
  margin-left: 10px;
  background-color: var(--primary-color);
  color: white;
  padding: 5px 10px;
  border-radius: 8px;
  background-color: var(--primary-color-disabled);
  opacity: 0.5;
  cursor: not-allowed;
}

.prize-member-avatar {
  width: 30px;
  height: 30px;
  margin-left: 3px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
}

.prize-member-winner {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 5px;
  margin-top: 10px;
}

.medal-icon {
  font-size: 16px;
  color: var(--primary-color);
}

.prize-member-winner-name {
  font-size: 13px;
  opacity: 0.7;
}

@media (max-width: 768px) {
  .join-prize-button,
  .join-prize-button-disabled {
    margin-left: 0;
  }
}
</style>

<template>
  <div class="proposal-section">
    <div class="proposal-row">
      <span class="proposal-row-title rule">
        <info-icon class="proposal-description-title-icon" />提案规则说明</span
      >
      <div class="proposal-description-content">
        <p>📛 拟议分类名称需保持唯一，请勿与现有分类或正在提案中的名称重复。</p>
        <p>📝 请在下方详细说明提案目的、预期价值及补充材料，方便大家快速理解。</p>
        <p>🗳️ 提案提交后将开放 3 天投票，需达到至少 60% 的赞成率并满 10 人参与方可通过。</p>
        <p>🤝 讨论请遵循社区守则，保持礼貌和善，欢迎附上相关案例或参考链接。</p>
      </div>
    </div>
    <div class="proposal-row">
      <span class="proposal-row-title">拟议分类名称</span>
      <BaseInput v-model="data.proposedName" placeholder="请输入分类名称" />
    </div>
    <div class="proposal-row">
      <span class="proposal-row-title">提案描述</span>
      <BaseInput v-model="data.proposalDescription" placeholder="简要说明提案目的与理由" />
    </div>
  </div>
</template>

<script setup>
import BaseInput from '~/components/BaseInput.vue'

defineProps({
  data: { type: Object, required: true },
})
</script>

<style scoped>
.proposal-section {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 200px;
}

.proposal-row-title {
  font-size: 16px;
  color: var(--text-color);
  font-weight: bold;
  margin-bottom: 10px;
}

.proposal-row-title.rule {
  margin-bottom: 0;
}

.proposal-row {
  display: flex;
  flex-direction: column;
}

.proposal-activity {
  margin-top: 20px;
  padding: 20px;
}

.proposal-description-title-text {
  font-size: 14px;
  font-weight: bold;
  margin-left: 5px;
}

.proposal-description-title-icon {
  margin-right: 5px;
}

.proposal-description-content {
  font-size: 12px;
  opacity: 0.8;
}
</style>

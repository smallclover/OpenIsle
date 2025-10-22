<template>
  <div class="proposal-section">
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

.proposal-row {
  display: flex;
  flex-direction: column;
}
</style>

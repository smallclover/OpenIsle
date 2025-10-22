<template>
  <div class="poll-section">
    <div class="poll-options-row">
      <span class="poll-row-title">投票选项</span>
      <div class="poll-option-item" v-for="(opt, idx) in data.options" :key="idx">
        <BaseInput v-model="data.options[idx]" placeholder="选项内容" />
        <close-icon class="remove-option-icon" @click="removeOption(idx)" />
      </div>
      <div class="add-option" @click="addOption">添加选项</div>
    </div>
    <div class="poll-time-row">
      <span class="poll-row-title">投票结束时间</span>
      <client-only>
        <flat-pickr v-model="data.endTime" :config="dateConfig" class="time-picker" />
      </client-only>
    </div>
    <div class="poll-multiple-row">
      <span class="poll-row-title">多选</span>
      <BaseSwitch v-model="data.multiple" />
    </div>
  </div>
</template>

<script setup>
import 'flatpickr/dist/flatpickr.css'
import FlatPickr from 'vue-flatpickr-component'
import BaseInput from '~/components/BaseInput.vue'
import BaseSwitch from '~/components/BaseSwitch.vue'

const props = defineProps({
  data: {
    type: Object,
    required: true,
  },
})

const dateConfig = { enableTime: true, time_24hr: true, dateFormat: 'Y-m-d H:i' }

const addOption = () => {
  props.data.options.push('')
}

const removeOption = (idx) => {
  if (props.data.options.length > 2) {
    props.data.options.splice(idx, 1)
  }
}
</script>

<style scoped>
.poll-section {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 200px;
}
.poll-row-title {
  font-size: 16px;
  color: var(--text-color);
  font-weight: bold;
  margin-bottom: 10px;
}
.poll-option-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.remove-option-icon {
  cursor: pointer;
}
.add-option {
  color: var(--primary-color);
  cursor: pointer;
  width: fit-content;
  margin-top: 5px;
}
.poll-options-row,
.poll-time-row {
  display: flex;
  flex-direction: column;
}
.poll-multiple-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.time-picker {
  max-width: 200px;
  height: 30px;
  background-color: var(--lottery-background-color);
  border-radius: 5px;
  border: 1px solid var(--border-color);
}
</style>

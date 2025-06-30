<script setup lang="ts">

import { ref, onMounted, nextTick, computed } from 'vue'
import type { ThinkingStatus } from 'vue-element-plus-x/types/Thinking'

const statusValue = ref<ThinkingStatus>('thinking')
</script>

<style scoped>
.label {
  color: hsl(0, 0%, 4%);
}
</style>

<template>
  <el-radio-group v-model="statusValue" style="margin-bottom: 12px;">
    <el-radio-button value="thinking">
      thinking
    </el-radio-button>
    <el-radio-button value="end">
      end
    </el-radio-button>
  </el-radio-group>

  <Thinking
      :status="statusValue"
      auto-collapse
      content="欢迎使用 Element-Plus-X.............."
      button-width="250px"
      max-width="100%"
  />
</template>
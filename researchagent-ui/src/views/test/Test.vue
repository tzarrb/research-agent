<script setup lang="ts">
import type { BubbleListItemProps, BubbleListProps } from 'vue-element-plus-x/types/BubbleList'

type listType = BubbleListItemProps & {
  key: number
  role: 'user' | 'ai'
}

// 示例调用
const list: BubbleListProps<listType>['list'] = generateFakeItems(5)

function generateFakeItems(count: number): listType[] {
  const messages: listType[] = []
  for (let i = 0; i < count; i++) {
    const role = i % 2 === 0 ? 'ai' : 'user'
    const placement = role === 'ai' ? 'start' : 'end'
    const key = i + 1
    const content = role === 'ai'
        ? '## 技术栈\n' +
        '\n' +
        '- **核心框架**: Spring Boot、SpringCloud\n' +
        '- **AI 能力**: Spring AI、Spring AI Alibaba (集成阿里云 DashScope)\n' +
        '- **向量存储**: PGVector、Redis Vector Store\n' +
        '- **动态配置**: Nacos\n' +
        '- **检索增强生成**: RAG 架构\n' +
        '- **Agent**: 多Agent路由架构\n' +
        '- **前端框架**: Vue3、Element Plus、Element Plus X'.repeat(5)
        : `💖 感谢使用 Element Plus X ! 你的支持，是我们开源的最强动力 ~`
    const loading = false
    const shape = 'corner'
    const variant = role === 'ai' ? 'filled' : 'outlined'
    const isMarkdown = true
    const typing = role === 'ai' ? (i === count - 1 ? { step: 5, suffix: '🍆', interval: 35 } : false) : false
    const avatar = role === 'ai'
        ? 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
        : 'https://avatars.githubusercontent.com/u/76239030?v=4'

    messages.push({
      key, // 唯一标识
      role, // user | ai 自行更据模型定义
      placement, // start | end 气泡位置
      content, // 消息内容 流式接受的时候，只需要改这个值即可
      loading, // 当前气泡的加载状态
      shape, // 气泡的形状
      variant, // 气泡的样式
      isMarkdown, // 是否渲染为 markdown
      typing, // 是否开启打字器效果 该属性不会和流式接受冲突
      isFog: role === 'ai', // 是否开启打字雾化效果，该效果 v1.1.6 新增，且在 typing 为 true 时生效，该效果会覆盖 typing 的 suffix 属性
      avatar,
      avatarSize: '24px', // 头像占位大小
      avatarGap: '12px', // 头像与气泡之间的距离
    })
  }
  return messages
}
</script>

<template>
  <BubbleList :list="list" max-height="350px" >
  </BubbleList>
</template>
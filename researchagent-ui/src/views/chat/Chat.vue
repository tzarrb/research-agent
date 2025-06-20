<template>
  <nav_bar/>
  <div class="chat-container">
    <div class="chat-messages" ref="messagesContainer">
      <div v-if="messages.length === 0" class="welcome-message">
        <p class="welcome-message-title">
          <span class="welcome-logo">
            <svg width="36" height="36" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="24" cy="24" r="22" fill="#3A8DFF"/>
              <rect x="14" y="18" width="20" height="14" rx="7" fill="#fff"/>
              <ellipse cx="19.5" cy="25" rx="2.5" ry="3" fill="#3A8DFF"/>
              <ellipse cx="28.5" cy="25" rx="2.5" ry="3" fill="#3A8DFF"/>
              <rect x="20" y="30" width="8" height="2" rx="1" fill="#B3D1FF"/>
              <rect x="22" y="10" width="4" height="6" rx="2" fill="#3A8DFF"/>
            </svg>
          </span>
          我是 AI探索者，很高兴见到你！
        </p>
        <p class="welcome-message-content">我可以帮你写代码、读文件、写作各种创意内容，请把你的任务交给我吧~</p>
        <p class="welcome-message-content">我还在不断优化中，有问题别着急~</p>
      </div>
      <div v-else v-for="(message, index) in messages" :key="index" class="message" :class="message.type">
        <img
          v-if="message.type === 'ai'"
          class="avatar"
          src="https://api.dicebear.com/7.x/bottts/svg?seed=ai"
          alt="AI"
        />
        <img
          v-if="message.type === 'user'"
          class="avatar"
          src="https://api.dicebear.com/7.x/personas/svg?seed=user"
          alt="User"
        />
        <div class="message-content-wrapper">
          <div
            class="message-content markdown-body"
            v-if="message.type === 'ai'"
            v-html="renderMarkdown(message.content)"
          ></div>
          <div
            class="message-content"
            v-if="message.type === 'user'"
          >{{ message.content }}</div>
          <button class="copy-btn" v-if="message.type === 'user'" @click="copyMessage(message.content)">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#555" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
              <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
            </svg>
            <span class="copy-tooltip">复制</span>
          </button>
          <button class="copy-btn" v-if="message.type === 'ai' && message.done" @click="copyMessage(message.content)">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#555" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
              <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
            </svg>
            <span class="copy-tooltip">复制</span>
          </button>
        </div>
        
      </div>
    </div>
    <div class="chat-input-container">
      <div class="chat-input-row-1">
        <textarea
          v-model="userInput" 
          @keyup.enter="sendMessage"
          placeholder="给 ResearchAgent 发送消息"
          :disabled="isLoading"
          rows="2"
        />
      </div>
      <div class="chat-input-row-2">
        <div class="option-buttons">
          <button :class="['option-btn', useLocal ? 'selected' : '']" @click="useLocal = !useLocal">
            <span class="checkbox-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <rect x="2" y="2" width="20" height="20" rx="6" :fill="useLocal ? '#3A8DFF' : '#eaf3ff'" :stroke="useLocal ? '#3A8DFF' : '#bbb'" stroke-width="2"/>
                <path d="M7 8.5V16a1 1 0 0 0 1 1h8" :stroke="useLocal ? '#fff' : '#bbb'" stroke-width="1.5" fill="none"/>
                <path d="M7 8.5A2.5 2.5 0 0 1 9.5 6H17a1 1 0 0 1 1 1v9a1 1 0 0 1-1 1h-8a1 1 0 0 1-1-1V8.5z" :stroke="useLocal ? '#fff' : '#bbb'" stroke-width="1.5" fill="none"/>
                <rect x="10" y="10" width="4" height="1" rx="0.5" :fill="useLocal ? '#fff' : '#bbb'"/>
              </svg>
            </span>
            <span :style="{color: useLocal ? '#2266bb' : '#bbb'}">本地知识库</span>
          </button>
          <button :class="['option-btn', useWeb ? 'selected' : '']" @click="useWeb = !useWeb">
            <span class="checkbox-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <rect x="2" y="2" width="20" height="20" rx="6" :fill="useWeb ? '#3A8DFF' : '#eaf3ff'" :stroke="useWeb ? '#3A8DFF' : '#bbb'" stroke-width="2"/>
                <circle cx="12" cy="12" r="5" :stroke="useWeb ? '#fff' : '#bbb'" stroke-width="1.5" fill="none"/>
                <path d="M12 7a5 5 0 0 0 0 10" :stroke="useWeb ? '#fff' : '#bbb'" stroke-width="1.5" fill="none"/>
                <path d="M7 12h10" :stroke="useWeb ? '#fff' : '#bbb'" stroke-width="1.5" fill="none"/>
                <path d="M12 7a8 8 0 0 1 0 10" :stroke="useWeb ? '#fff' : '#bbb'" stroke-width="1.5" fill="none"/>
              </svg>
            </span>
            <span :style="{color: useWeb ? '#2266bb' : '#bbb'}">联网搜索</span>
          </button>
        </div>
        <button class="send-btn option-send-btn" @click="sendMessage" :disabled="isLoading || !userInput.trim()">
          <svg v-if="!isLoading" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 23V5" />
            <path d="M5 12l7-7 7 7" />
          </svg>
          <svg v-else width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="5" y="5" width="14" height="14" rx="2"/>
          </svg>
        </button>
      </div>
    </div>
    </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
// 导入组件
import nav_bar from '@/components/bar/NavBar.vue'
//全局缓存
import { useMainStore } from '@/store';

const mainStore = useMainStore();

// 配置 marked
marked.setOptions({
  highlight: function(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value
    }
    return hljs.highlightAuto(code).value
  },
  breaks: true, // 支持换行
  gfm: true, // 支持 GitHub 风格的 Markdown
})

const messages = ref([])
const userInput = ref('')
const isLoading = ref(false)
const messagesContainer = ref(null)
const useLocal = ref(false)
const useWeb = ref(false)
const conversantId = ref('')

const renderMarkdown = (content) => {
  return marked(content)
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const sendMessage = async () => {
  if (!userInput.value.trim() || isLoading.value) return

  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: userInput.value
  })

  const message = userInput.value
  userInput.value = ''
  isLoading.value = true

  // 添加AI消息占位
  messages.value.push({
    type: 'ai',
    content: '',
    done: false
  })

  try {
    const response = await fetch(`http://localhost:18080/research-agent/ai/chat/sse/chat?userMessage=${encodeURIComponent(message)}&enableLocal=${useLocal.value}&enableWeb=${useWeb.value}`, {
      headers: {
        'Accept': 'text/event-stream',
        'sessionId': conversantId.value
      }
    })

    if (response.headers.has('sessionId')) {
      conversantId.value = response.headers.get('sessionId');
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = '' // 添加缓冲区

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value)
      buffer += chunk // 将新数据添加到缓冲区
      
      // 处理SSE格式的数据
      const lines = buffer.split('\n')
      buffer = lines.pop() || '' // 保留最后一个不完整的行

      for (const line of lines) {
        if (line.startsWith('data:')) {
          try {
            const jsonStr = line.slice(5).trim()
            if (jsonStr) {
              const data = JSON.parse(jsonStr)
              if (data.content) {
                messages.value[messages.value.length - 1].content += data.content
                await scrollToBottom()
              }
            }
          } catch (e) {
            console.error('Error parsing JSON:', e)
          }
        }
      }
    }
    // AI消息流式加载结束，done设为true
    messages.value[messages.value.length - 1].done = true
  } catch (error) {
    console.error('Error:', error)
    messages.value[messages.value.length - 1].content = '抱歉，发生了错误，请稍后重试。'
    messages.value[messages.value.length - 1].done = true
  } finally {
    isLoading.value = false
    await scrollToBottom()
  }
}

const copyMessage = async (content) => {
  try {
    await navigator.clipboard.writeText(content)
  } catch (e) {
    alert('复制失败')
  }
}

onMounted(() => {
  scrollToBottom()
})
</script>

<style>
.chat-container {
  width: 100%;
  max-width: 1000px;
  height: 90vh;
  margin: 10px auto;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 32px rgba(0,0,0,0.08);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.chat-messages {
  flex: 1 1 0%;
  overflow-y: auto;
  padding: 32px 24px 16px 24px;
  /* background: #f7f9fb; */
}

.message {
  margin-bottom: 18px;
  max-width: 90%;
  display: flex;
  align-items: flex-start;
}

.message.user {
  flex-direction: row-reverse;
}

.message.user .message-content {
  background: #007bff;
  color: white;
  text-align: right;
  /* width: 0; */
}

.message.ai {
  margin-right: auto;
}

.message-content-wrapper {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.message-content {
  /* width: 700px; */
  /* max-width: 95%; */
  min-height: 32px;
  padding: 12px 18px;
  border-radius: 18px;
  display: inline-block;
  font-size: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.03);
  /* background: #fff; */
  border: 1px solid #e6e6e6;
  word-break: break-word;
}

.ai .message-content {
  /* background: #fff; */
  border: 1px solid #ddd;
  color: #222;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  margin: 0 12px;
  background: #eee;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.copy-btn {
  position: relative;
  margin: 3px 5px 1px auto;
  display: block;
  background: #f5f4f4;
  border: none;
  border-radius: 4px;
  padding: 2px;
  cursor: pointer;
  opacity: 0.6;
  transition: opacity 0.2s, background 0.2s;
}
.copy-btn:hover {
  opacity: 1;
  background-color: #e6f0ff !important;
}
.copy-btn svg {
  display: block;
}
.copy-tooltip {
  display: none;
  position: absolute;
  bottom: 120%;
  left: 50%;
  transform: translateX(-50%);
  background: #222;
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.2s;
  z-index: 99;
}
.copy-btn:hover .copy-tooltip {
  display: block;
  opacity: 1;
}



button {
  /* padding: 12px 28px; */
  background: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  transition: background 0.2s;
}

button:disabled {
  background: #9a9999;
  cursor: not-allowed;
}

button:hover:not(:disabled) {
  background: #0056b3;
}

/* Markdown 样式 */
.markdown-body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
  line-height: 1.6;
  word-wrap: break-word;
}

.markdown-body :deep(pre) {
  background-color: #f6f8fa;
  border-radius: 6px;
  padding: 16px;
  overflow: auto;
}

.markdown-body :deep(code) {
  font-family: SFMono-Regular, Consolas, "Liberation Mono", Menlo, monospace;
  font-size: 85%;
  padding: 0.2em 0.4em;
  margin: 0;
  background-color: rgba(27, 31, 35, 0.05);
  border-radius: 3px;
}

.markdown-body :deep(pre code) {
  padding: 0;
  margin: 0;
  background-color: transparent;
  border: 0;
  word-break: normal;
  white-space: pre;
}

.markdown-body :deep(p) {
  margin-top: 0;
  margin-bottom: 16px;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 2em;
  margin-top: 0;
  margin-bottom: 16px;
}

.markdown-body :deep(table) {
  border-spacing: 0;
  border-collapse: collapse;
  margin-top: 0;
  margin-bottom: 16px;
}

.markdown-body :deep(table th),
.markdown-body :deep(table td) {
  padding: 6px 13px;
  border: 1px solid #dfe2e5;
}

.markdown-body :deep(table tr) {
  background-color: #fff;
  border-top: 1px solid #c6cbd1;
}

.markdown-body :deep(table tr:nth-child(2n)) {
  background-color: #f6f8fa;
}

.markdown-body :deep(blockquote) {
  padding: 0 1em;
  color: #6a737d;
  border-left: 0.25em solid #dfe2e5;
  margin: 0 0 16px 0;
}

.markdown-body :deep(img) {
  max-width: 100%;
  box-sizing: content-box;
  background-color: #fff;
}
.send-btn{
  width: 36px;
  height: 36px;
  border-radius: 20px;
  margin: 12px 0;
}

.welcome-message {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #555;
  font-size: 20px;
  text-align: center;
  opacity: 0.85;
  user-select: none;
}
.welcome-message-title{
  font-size: 24px;
  font-weight: 400;
  color: #222;
  display: flex;
  align-items: center;
  gap: 10px;
}
.welcome-logo {
  display: inline-flex;
  align-items: center;
  margin-right: 6px;
}
.welcome-message-content{
  font-size: 16px;
  color: #555;
}
.chat-input-container{
  margin: 20px;
  background-color: var(--dsr-input-bg);
  border-radius: 20px;
}

.chat-input-row-1 {
  width: 100%;
  margin-bottom: 6px;
}
.chat-input-row-2 {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin: 10px;
}
.option-buttons {
  display: flex;
  gap: 10px;
  margin: 0;
}
.option-send-btn {
  margin-left: auto;
  height: 36px;
  width: 36px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #007bff;
  color: white;
  border: none;
  cursor: pointer;
  transition: background 0.2s;
}
.option-send-btn:disabled {
  background: #9a9999;
  cursor: not-allowed;
}
.option-send-btn:hover:not(:disabled) {
  background: #0056b3;
}
.option-btn {
  display: flex;
  align-items: center;
  background: #f4f8fe;
  border: 1.5px solid #e0eaff;
  color: #bbb;
  border-radius: 20px;
  padding: 4px 16px;
  font-size: 15px;
  font-weight: 400;
  cursor: pointer;
  transition: background 0.2s, border 0.2s, color 0.2s;
  outline: none;
  min-width: 110px;
}
.option-btn.selected {
  background: #eaf3ff;
  border: 1.5px solid #3A8DFF;
  color: #2266bb;
}
.option-btn svg {
  flex-shrink: 0;
}
.checkbox-icon {
  display: flex;
  align-items: center;
  margin-right: 6px;
}
.option-btn:hover{
  background: #d7e5f8!important;
}


.chat-input-row-1 {
  display: flex;
  gap: 12px;
  padding: 5px;
  /* background-color: transparent; */
  border-radius: 20px;
  border: 0;
  box-sizing: border-box;
}

textarea {
  flex: 1;
  padding: 12px;
  border: 0;
  border-radius: 20px;
  font-size: 16px;
  background-color: transparent;
  resize: none;
  font-family: inherit;
  word-break: break-word;
}

textarea:focus {
  outline: none;
  box-shadow: none;
  border-color: #ddd;
}
body{
  --dsr-main: #4d6bfe;
    --dsr-main-2: rgba(77, 107, 254, .4);
    --dsr-main-3: rgba(77, 107, 254, .2);
    --dsr-bg: rgb(var(--ds-rgb-white));
    --dsr-text-0: rgb(var(--ds-rgb-black));
    --dsr-text-1: rgb(var(--ds-rgb-neutral-800));
    --dsr-text-2: rgb(var(--ds-rgb-neutral-600));
    --dsr-text-3: rgb(var(--ds-rgb-neutral-400));
    --dsr-text-4: rgb(var(--ds-rgb-zinc-350));
    --dsr-border-1: rgb(var(--ds-rgb-neutral-350));
    --dsr-border-2: rgb(var(--ds-rgb-neutral-200));
    --dsr-input-border: #dce0e9;
    --dsr-input-bg: rgb(var(--ds-rgb-gray-100));
    --dsr-button-main-bg: var(--dsr-main);
    --dsr-button-main-bg-hover: #4166d5;
    --dsr-button-second-bg: var(--dsr-main-3);
    --dsr-button-grey-0: rgb(var(--ds-rgb-neutral-150));
    --dsr-button-grey-1: rgb(var(--ds-rgb-neutral-100));
    --dsr-button-grey-2: rgb(var(--ds-rgb-neutral-50));
    --dsr-delete-button-bg: rgb(var(--ds-rgb-red-500) / .85);
    --dsr-delete-button-bg-hover: rgb(var(--ds-rgb-red-550));
    --dsr-tooltip-fg: #eff6ff;
    --dsr-tooltip-bg: rgb(var(--ds-rgb-neutral-850));
    --dsr-side-bg: #f9fbff;
    --dsr-side-hover-bg-rgb: 239, 246, 255;
    --dsr-side-hover-bg: rgb(var(--ds-rgb-blue-50));
    --dsr-icon-fg-1: rgb(var(--ds-rgb-neutral-650));
    --dsr-icon-hover-0: rgb(var(--ds-rgb-neutral-150));
    --dsr-icon-hover-1: rgb(var(--ds-rgb-neutral-100));
    --dsr-side-icon-hover: rgb(var(--ds-rgb-slate-100));
    --dsr-error-fg: rgb(var(--ds-rgb-red-550));
    --dsr-risk-text: #e4773d;
    --dsr-risk-border: rgba(228, 119, 61, .1);
    --dsr-risk-fill: rgba(228, 119, 61, .05);
}

:root, page {
    --ds-rgb-black: 0 0 0;
    --ds-rgb-white: 255 255 255;
    --ds-rgb-slate-50: 248 250 252;
    --ds-rgb-slate-100: 241 245 249;
    --ds-rgb-slate-150: 233 238 244;
    --ds-rgb-slate-200: 226 232 240;
    --ds-rgb-slate-250: 214 222 232;
    --ds-rgb-slate-300: 203 213 225;
    --ds-rgb-slate-350: 175 188 204;
    --ds-rgb-slate-400: 148 163 184;
    --ds-rgb-slate-450: 124 139 161;
    --ds-rgb-slate-500: 100 116 139;
    --ds-rgb-slate-550: 85 100 122;
    --ds-rgb-slate-600: 71 85 105;
    --ds-rgb-slate-650: 61 75 95;
    --ds-rgb-slate-700: 51 65 85;
    --ds-rgb-slate-750: 40 53 72;
    --ds-rgb-slate-800: 30 41 59;
    --ds-rgb-slate-850: 22 32 50;
    --ds-rgb-slate-900: 15 23 42;
    --ds-rgb-slate-950: 2 6 23;
    --ds-rgb-gray-50: 249 250 251;
    --ds-rgb-gray-100: 243 244 246;
    --ds-rgb-gray-150: 236 237 240;
    --ds-rgb-gray-200: 229 231 235;
    --ds-rgb-gray-250: 219 222 227;
    --ds-rgb-gray-300: 209 213 219;
    --ds-rgb-gray-350: 182 188 197;
    --ds-rgb-gray-400: 156 163 175;
    --ds-rgb-gray-450: 131 138 151;
    --ds-rgb-gray-500: 107 114 128;
    --ds-rgb-gray-550: 91 99 113;
    --ds-rgb-gray-600: 75 85 99;
    --ds-rgb-gray-650: 65 75 90;
    --ds-rgb-gray-700: 55 65 81;
    --ds-rgb-gray-750: 43 53 68;
    --ds-rgb-gray-800: 31 41 55;
    --ds-rgb-gray-850: 24 32 47;
    --ds-rgb-gray-900: 17 24 39;
    --ds-rgb-gray-950: 3 7 18;
    --ds-rgb-zinc-50: 250 250 250;
    --ds-rgb-zinc-100: 244 244 245;
    --ds-rgb-zinc-150: 236 236 238;
    --ds-rgb-zinc-200: 228 228 231;
    --ds-rgb-zinc-250: 220 220 223;
    --ds-rgb-zinc-300: 212 212 216;
    --ds-rgb-zinc-350: 186 186 193;
    --ds-rgb-zinc-400: 161 161 170;
    --ds-rgb-zinc-450: 137 137 146;
    --ds-rgb-zinc-500: 113 113 122;
}
</style> 
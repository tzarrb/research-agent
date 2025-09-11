<template>
  <div class="common-layout">
    <el-container>
      <el-aside>
        <div class="slider-container" style="display: flex; flex-direction: column;">
          <div>
            <p class="slider-title">ResearchAgent</p>
          </div>
          <div class="conversation-container">
            <Conversations
                :items="conversationItems"
                row-key="key"
                :label-max-width="200"
                :show-tooltip="true"
                show-to-top-btn
                show-built-in-menu
            >
              <template #more-filled>
                <el-icon><EditPen /></el-icon>
              </template>

              <template #menu="{ item }">
                <div class="menu-buttons">
                  <el-button
                      v-for="menuItem in conversationMenuItems"
                      :key="menuItem.key"
                      link
                      size="small"
                      @click.stop="handleMenuClick(menuItem.key, item)"
                  >
                    <el-icon v-if="menuItem.icon">
                      <component :is="menuItem.icon" />
                    </el-icon>
                    <span v-if="menuItem.label">{{ menuItem.label }}</span>
                  </el-button>
                </div>
              </template>
            </Conversations>
          </div>
          <div class="slider-footer">
            <div class="slider-footer-btn">
              <el-button size="default" icon="HomeFilled" round @click="router.push('/home')">首页</el-button>
              <el-button size="default" icon="Upload" round @click="router.push('/upload')">知识库</el-button>
            </div>

            <!-- 用户状态区域 -->
            <div class="slider-footer-user">
              <template v-if="mainStore.user.account">
                <!-- 用户头像/账户信息 -->
                <div class="slider-footer-user__info">
                  <el-avatar :size="34" :src="mainStore.user.avatar" :alt="mainStore.user.account"/>
                  <!--<span class="slider-footer-user__name">{{ mainStore.user.account }}</span>-->
                  <!-- <el-badge :value="mainStore.user.msgCount" class="slider-footer-user__badge">
                    <el-button size="small">消息</el-button>
                  </el-badge> -->
                </div>
              </template>
              <template v-else>
                <!-- 登录入口 -->
                <el-button type="primary" @click="router.push('/login')">登录</el-button>
              </template>
            </div>
          </div>
        </div>
      </el-aside>
      <el-main style="background-color: #fdfeff">
        <div class="chat-container">
          <div class="chat-messages" ref="messagesContainer">
            <div v-if="messages.length === 0" class="welcome-message">
              <Welcome
                  icon="https://mdn.alipayobjects.com/huamei_iwk9zp/afts/img/A*s5sNRo5LjfQAAAAAAAAAAAAADgCCAQ/fmt.webp"
                  variant="borderless"
                  title="欢迎使用 Research Agent 💖"
                  extra=""
                  description="我是 AI探索者，很高兴见到你！"
              />
              <p class="welcome-message-content">我可以帮你写代码、读文件、写作各种创意内容，请把你的任务交给我吧~</p>
              <p class="welcome-message-content">我还在不断优化中，有问题别着急~</p>
            </div>
            <div v-else>
              <!-- v-for="(message, index) in messages" :key="index" class="message" :class="message.type" -->
              <BubbleList :list="messages" max-height="350px">
                <!-- 自定义头像 -->
                <template #avatar="{ item }">
                  <div class="avatar-wrapper">
                    <img :src="item.role === 'ai' ? avatarAi : avatarUser" alt="avatar">
                  </div>
                </template>

                <!-- 自定义头部 -->
                <template #header="{ item }">
                  <div class="header-wrapper">
                    <!-- <div class="header-name">
                      {{ item.role === 'ai' ? '智能体 🍧' : '🧁 用户' }}
                    </div> -->
                    <div class="thinking-wrapper" style="height: 100px"  v-if="item.role === 'ai' && item.thinkingStatus" >
                      <Thinking
                          :status="item.thinkingStatus"
                          :content="item.thinkingContent"
                          auto-collapse
                          button-width="250px"
                          max-width="100%"
                      />
                    </div>
                  </div>
                </template>

                <!-- 自定义气泡内容 Markdown渲染需手动处理-->
                <template #content="{ item }">
                  <div class="content-wrapper">
                    <div class="content-text">
                      <Typewriter :content="item.content" :is-markdown="true" :md-plugins="mdPlugins" :highlight="highlight" />
                    </div>
                  </div>
                </template>

                <!-- 自定义底部 -->
                <template #footer="{ item }">
                  <div class="footer-wrapper">
                    <div class="footer-container">
                      <el-button v-if="item.role === 'ai' && item.error && item.done" type="info" icon="Refresh" size="small" circle @click="refreshSseRequest" />
                      <el-button v-if="(item.role === 'ai' && !item.error && item.done) || item.role === 'user'" color="#626aef" icon="DocumentCopy" size="small" circle @click="copyMessage(item.content)" />
                    </div>
                    <div class="footer-time">
                      {{ item.datetime ? new Date(item.datetime).toLocaleTimeString() : new Date().toLocaleTimeString() }}
                    </div>
                  </div>
                </template>

                <!-- 自定义 loading -->
                <template #loading="{ item }">
                  <div class="loading-container">
                    <span>加</span>
                    <span>载</span>
                    <span>中</span>
                    <span>~</span>
                  </div>
                </template>
              </BubbleList>
            </div>
          </div>
          <div class="chat-input-container">
            <Sender v-model="sendValue" :loading="isLoad" @submit="sendSseRequest" @cancel="abortSseRequest" variant="updown" submit-type="enter" :auto-size="{ minRows: 2, maxRows: 5 }" clearable allow-speech placeholder="💌 给 ResearchAgent 发送消息">
              <template #prefix>
                <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
                  <el-upload
                      v-model:file-list="fileList"
                      ref="upload"
                      class="upload-demo"
                      action="#"
                      :limit="1"
                      :auto-upload="false"
                      :on-change="handleChange"
                      :on-exceed="handleExceed"
                      :on-remove="handleRemove"
                  >
                    <el-button icon="Paperclip" round plain color="#626aef"></el-button>
                  </el-upload>

                  <div :class="{ isThink }" style="display: flex; align-items: center; gap: 4px; padding: 2px 12px; border: 1px solid silver; border-radius: 15px; cursor: pointer; font-size: 12px;" @click="isThink = !isThink">
                    <el-icon><ElementPlus/></el-icon>
                    <span>深度思考</span>
                  </div>
                  <div :class="{ isWeb }" style="display: flex; align-items: center; gap: 4px; padding: 2px 12px; border: 1px solid silver; border-radius: 15px; cursor: pointer; font-size: 12px;" @click="isWeb = !isWeb">
                    <el-icon><ChromeFilled /></el-icon>
                    <span>联网搜索</span>
                  </div>
                  <div :class="{ isLocal }" style="display: flex; align-items: center; gap: 4px; padding: 2px 12px; border: 1px solid silver; border-radius: 15px; cursor: pointer; font-size: 12px;" @click="isLocal = !isLocal">
                    <el-icon><Document /></el-icon>
                    <span>本地知识库</span>
                  </div>
                </div>
              </template>
              <!-- 自定义发送按钮 -->
              <!-- <template #action-list>
                <div style="display: flex; align-items: center; gap: 8px;">
                  <el-button v-if="isLoad" type="primary" plain circle @click="sendCancel">
                    <el-icon class="isloaidng"><Loading /></el-icon>
                  </el-button>

                  <el-button v-else plain circle @click="sendMessage">
                    <el-icon><Position /></el-icon>
                  </el-button>
                </div>
              </template> -->
            </Sender>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>

</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
// 导入组件
import {ElMessage, genFileId, UploadInstance, UploadProps, UploadUserFile} from "element-plus";
import { useSend, XRequest } from 'vue-element-plus-x';

import markdownItMermaid from '@jsonlee_12138/markdown-it-mermaid'
// 这里是组件库内置的一个 代码高亮库 Prismjs，自定义的 hooks 例子。(仅供集成参考)代码地址：https://github.com/HeJiaYue520/Element-Plus-X/blob/main/packages/components/src/hooks/usePrism.ts
import { usePrism } from 'vue-element-plus-x'
// 这里可以引入 Prism 的核心样式，也可以自己引入其他第三方主题样式
import 'vue-element-plus-x/styles/prism.min.css'

// 全局缓存
import { useMainStore } from '@/store';
// 全局路由
import {RouterLink} from "vue-router";
import router from '@/router'

// 全局缓存
const mainStore = useMainStore();

const mdPlugins = [markdownItMermaid({ delay: 100, forceLegacyMathML: true })]
const highlight = usePrism()

const uploadRef = ref<UploadInstance>()

const avatarUser = ref('http://gips3.baidu.com/it/u=3886271102,3123389489&fm=3028&app=3028&f=JPEG&fmt=auto?w=1280&h=960')
const avatarAi = ref('https://mdn.alipayobjects.com/huamei_iwk9zp/afts/img/A*s5sNRo5LjfQAAAAAAAAAAAAADgCCAQ/fmt.webp')

const messagesContainer = ref(null)
const messages = ref([])
const sendValue = ref('')
const inputValue = ref('')
const conversantId = ref('')

const isLoad = ref(false)
const isThink = ref(false)
const isLocal = ref(false)
const isWeb = ref(false)

const upload = ref<UploadInstance>()
const fileList = ref([])
const selectedFile = ref(null)

const handleExceed: UploadProps['onExceed'] = (files) => {
  upload.value!.clearFiles()

  const file = files[0] as UploadRawFile
  file.uid = genFileId()
  upload.value!.handleStart(file)
}

const handleRemove: UploadProps['onRemove'] = (uploadFile, uploadFiles) => {
  fileList.value = fileList.value.filter(file => file !== uploadFile.raw!)
  selectedFile.value = null
}

const handleChange: UploadProps['onChange'] = (uploadFile, uploadFiles) => {
  fileList.value.push(uploadFile.raw!)
  selectedFile.value = uploadFile.raw!
}

// 会话记录 =============================================================================================================
const conversationItems = ref([])

const conversationMenuItems = [
  {
    key: 'edit',
    label: '编辑',
    icon: "Edit",
    command: {
      self_id: '1',
      self_message: '编辑',
      self_type: 'text',
    },
  },
  {
    key: 'delete',
    label: '删除',
    icon: "Delete",
    disabled: true,
    divided: true,
  },
  {
    key: 'share',
    label: '分享',
    icon: "Share",
    command: 'share',
  },
]

// 处理菜单点击
function handleMenuClick(menuKey: string, item: any) {
  console.log('菜单点击', menuKey, item)

  switch (menuKey) {
    case 'edit':
      console.log(`编辑: ${item.label}`)
      ElMessage.warning(`编辑: ${item.label}`)
      break
    case 'delete':
      console.log(`删除: ${item.label}`)
      ElMessage.error(`删除: ${item.label}`)
      break
    case 'share':
      console.log(`分享: ${item.label}`)
      ElMessage.success(`分享: ${item.label}`)
      break
  }
}

// 刷新请求
const uploadFile = async () => {

}

// 消息发送 =============================================================================================================
// messages.push({
//   key, // 唯一标识
//   role, // user | ai 自行更据模型定义
//   placement, // start | end 气泡位置
//   content, // 消息内容 流式接受的时候，只需要改这个值即可
//   loading, // 当前气泡的加载状态
//   shape, // 气泡的形状，可选值为 'round'（圆角）或 'corner'（有角）。
//   variant, // 气泡的样式变体，可选值为 'filled'（填充）、'borderless'（无边框）、'outlined'（轮廓）、'shadow'（阴影）
//   isMarkdown, // 是否渲染为 markdown
//   typing, // 是否开启打字器效果 该属性不会和流式接受冲突
//   isFog: role === 'ai', // 是否开启打字雾化效果，该效果 v1.1.6 新增，且在 typing 为 true 时生效，该效果会覆盖 typing 的 suffix 属性
//   avatar, // 头像地址
//   avatarSize: '24px', // 头像占位大小
//   avatarGap: '12px', // 头像与气泡之间的距离
//   maxWidth: '500px', // 气泡最大宽度
//   done: false, //流消息加载完成
// })

// Element X SSE 请求 ===============================================================================================
const sseRequest = new XRequest({
  baseURL: 'http://localhost:18080/research-agent/ai',
  type: 'fetch',
  transformer: (e) => {
    console.log('transformer:', e)
    const a = e.trim().split('\n')
    const r = a.pop()
    return r
  },
  onMessage: (msg) => {
    console.log('onMessage:', msg)

    let jsonStr = msg.trim()
    if (!jsonStr) return

    if (jsonStr.startsWith('data:')) {
      jsonStr = jsonStr.slice(5).trim() // 去掉前面的 'data:'
      if (!jsonStr) return
    }

    try {
      const data = JSON.parse(jsonStr)

      if (data.conversantId) {
        conversantId.value = data.conversantId
      }

      // 更新气泡消息内容和状态
      if (data.content) {
        messages.value[messages.value.length - 1].loading = false
        messages.value[messages.value.length - 1].content += data.content
        scrollToBottom()
      }

      // 更新推理内容和状态
      if (isThink.value) {
        messages.value[messages.value.length - 1].thinkingStatus = 'thinking'
        if (data.reasoningContent) {
          messages.value[messages.value.length - 1].thinkingContent += data.reasoningContent
          scrollToBottom()
        } else if (messages.value[messages.value.length - 1].thinkingStatus === 'thinking') {
          messages.value[messages.value.length - 1].thinkingStatus = 'end'
        }
      }

      // 添加消息记录
      if (!conversationItems.value.some(item => item.key === conversantId.value)) {
        const message = inputValue.value.length > 20 ? `${inputValue.value.slice(0, 50)}...` : inputValue.value
        conversationItems.value.push({
          key: conversantId.value,
          label: message,
          disabled: false,
        })
      }
    } catch (e) {
      console.error('Error parsing JSON:', e)
    }
  },
  onError: (es, e) => {
    console.log('onError:', es, e)
    // '抱歉，发生了错误，请稍后重试。'
    isLoad.value = false
    messages.value[messages.value.length - 1].content += es
    messages.value[messages.value.length - 1].error = true
    messages.value[messages.value.length - 1].done = true
    messages.value[messages.value.length - 1].loading = false
  },
  onOpen: () => {
    console.log('onOpen')
  },
  onAbort: (messages) => {
    console.log('onAbort', messages)
  },
  onFinish: (data) => {
    console.log('onFinish:', data)

    isLoad.value = false
    // AI消息流式加载结束，done设为true
    messages.value[messages.value.length - 1].done = true
    if (!messages.value[messages.value.length - 1].error) {
      //当content中出现"错误"，"失败"等字符串时，按错误处理
      if (messages.value[messages.value.length - 1].content.includes('错误')
          || messages.value[messages.value.length - 1].content.includes('失败')
          || messages.value[messages.value.length - 1].content.includes('failed')) {
        messages.value[messages.value.length - 1].error = true
      }
      //当content内容为空时
      if (messages.value[messages.value.length - 1].content.trim() === '') {
        messages.value[messages.value.length - 1].content = '抱歉，发生了错误，请稍后重试。'
        messages.value[messages.value.length - 1].error = true
      }
    }

    // 这里就是执行，useSend 的 finish 方法
    finish()
  },
})

function sendSseRequest() {
  console.log('开始请求服务器====================')
  const message = sendHandler()
  if (!message.trim()) return

  const requestBody = {
    userMessage: encodeURIComponent(message),
    enableLocal: isLocal.value,
    enableWeb: isWeb.value,
    enableThink: isThink.value,
    mediaFile: selectedFile.value
  }

  // 创建 FormData 对象
  const formData = new FormData()
  // formData.append('userMessage', encodeURIComponent(message))
  // formData.append('enableLocal', isLocal.value)
  // formData.append('enableWeb', isWeb.value)
  // formData.append('enableThink', isThink.value)
  formData.append('chatRequest', JSON.stringify(requestBody))

  // 添加文件参数（mediaFile）
  if (selectedFile.value) {
    formData.append('mediaFile', selectedFile.value)
  }

  sseRequest.send(`/chat/sse/chat`, {
    method: 'POST', // GET POST PUT
    headers: {
      'Accept': 'text/event-stream',
      'sessionId': conversantId.value
    },
    body: formData // body: JSON.stringify(requestBody)，body: formData
  })
}

// 刷新请求
const refreshSseRequest = async () => {
  messages.value[messages.value.length - 1].loading = true
  messages.value[messages.value.length - 1].content = ''

  const requestBody = {
    userMessage: encodeURIComponent(inputValue.value),
    enableLocal: isLocal.value,
    enableWeb: isWeb.value,
    enableThink: isThink.value
  }

  // 创建 FormData 对象
  const formData = new FormData()
  formData.append('chatRequest', JSON.stringify(requestBody))

  // 添加文件参数（mediaFile）
  if (selectedFile.value) {
    formData.append('mediaFile', selectedFile.value)
  }

  sseRequest.send(`/chat/sse/chat`, {
    method: 'POST', // GET POST PUT
    headers: {
      'Accept': 'text/event-stream',
      'sessionId': conversantId.value
    },
    body: formData
  })
}

function abortSseRequest() {
  // 服务端请求取消
  sseRequest.abort()

  isLoad.value = false
  messages.value[messages.value.length - 1].done = true
  messages.value[messages.value.length - 1].error = true
  messages.value[messages.value.length - 1].loading = false
  messages.value[messages.value.length - 1].content += "\n 请求已取消！！！"
}

// useSend 的 abort 和 finish 是一样的方法。
// 为了体现 这边 xrequest 请求，支持手动中断，和 结束回调。
// 所以 也在 useSend 中，也暴露了一个名字叫 finish 的方法。
const { send, loading, abort, finish } = useSend({
  sendHandler: sendSseRequest,
  abortHandler: abortSseRequest,
})

// 复制消息 =================================================================================
const copyMessage = async (content) => {
  try {
    await navigator.clipboard.writeText(content)
  } catch (e) {
    alert('复制失败')
  }
}

// 滚动到消息容器底部
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped lang="less">
// 左边框样式
.el-aside{
  width: 300px;
}
.slider-container {
  background-color: #f5f5f5;
  padding: 0px;
}
.slider-title{
  width: 100%;
  height: 60px;
  font-size: 24px;
  text-align: center;
  line-height: 60px;
  font-weight: bold;
  color: #027cff;
}
.slider-footer {
  //width: 100%;
  height: 40px;
  margin: 0px 8px;
  display: flex;
  align-items: center;
}

.slider-footer-user {
  display: flex;
  align-items: center;
  width: 80px;
  height: 100%;
  margin-left: auto; /* 关键调整：自动左外边距实现右对齐 */
}

.slider-footer-user__info {
  display: flex;
  align-items: center;
}

.slider-footer-user__name {
  font-weight: 50;
  color: #1a1a1a;
  margin-right: 0px;
}

.slider-footer-user__badge {
  display: inline-block;
  transform: scale(0.8);
}

.slider-footer-btn__login {
  //padding: 8px 10px;
  background-color: #f5f5f5;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.slider-footer-btn__login:hover {
  background-color: #e9e9e9;
}

.conversation-container {
  height: calc(100vh - 120px);
  margin: 10px 10px;
  padding: 0px;
}

.menu-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  padding: 12px;

  // 自定义菜单按钮-el-button样式
  .el-button {
    padding: 4px 8px;
    margin-left: 0;

    .el-icon {
      margin-right: 8px;
    }
  }
}

// 消息列表样式
.avatar-wrapper {
  width: 40px;
  height: 40px;
  img {
    width: 100%;
    height: 100%;
    border-radius: 50%;
  }
}

.header-wrapper {
  .header-name {
    font-size: 14px;
    color: #979797;
  }
}

// 消息内容样式
.content-wrapper {
  .content-text {
    font-size: 14px;
    color: #333;
    padding: 6px;
    //background: linear-gradient(to right, #fdfcfb 0%, #ffd1ab 100%);
    border-radius: 15px;
    //box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
}

.footer-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  .footer-time {
    font-size: 12px;
    margin-top: 3px;
  }
}

.footer-container {
  :deep(.el-button+.el-button) {
    margin-left: 8px;
  }
}

.loading-container {
  font-size: 14px;
  color: #333;
  padding: 12px;
  //background: linear-gradient(to right, #fdfcfb 0%, #ffd1ab 100%);
  border-radius: 15px;
  //box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.loading-container span {
  display: inline-block;
  margin-left: 8px;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(5px);
  }
  50% {
    transform: translateY(-5px);
  }
}

.loading-container span:nth-child(4n) {
  animation: bounce 1.2s ease infinite;
}
.loading-container span:nth-child(4n+1) {
  animation: bounce 1.2s ease infinite;
  animation-delay: .3s;
}
.loading-container span:nth-child(4n+2) {
  animation: bounce 1.2s ease infinite;
  animation-delay: .6s;
}
.loading-container span:nth-child(4n+3) {
  animation: bounce 1.2s ease infinite;
  animation-delay: .9s;
}

// 用户输入容器样式
.isThink {
  color: #626aef;
  border: 1px solid #626aef !important;
  border-radius: 15px;
  padding: 3px 12px;
  font-weight: 700;
}
.isWeb {
  color: #626aef;
  border: 1px solid #626aef !important;
  border-radius: 15px;
  padding: 3px 12px;
  font-weight: 700;
}
.isLocal {
  color: #626aef;
  border: 1px solid #626aef !important;
  border-radius: 15px;
  padding: 3px 12px;
  font-weight: 700;
}

// 输入框样式
.action-list-self-wrap {
  display: flex;
  align-items: center;
  & > span {
    width: 120px;
    font-weight: 600;
    color: var(--el-color-primary);
  }
}
.isloaidng {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>


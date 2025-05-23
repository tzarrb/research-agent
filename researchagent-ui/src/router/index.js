import { createRouter, createWebHistory } from 'vue-router'
import AiChat from "@/components/AiChat.vue";

const routes = [
  {
    path: '/',
    name: 'chat',
    component: AiChat
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router

import { createRouter, createWebHistory } from 'vue-router'
import Chat from "@/views/chat/Chat.vue";
import Home from "@/views/home/Index.vue";
import Login from "@/views/login/Login.vue";
import UploadFile from '../views/upload/UploadFile.vue';

const routes = [
  {
    path: '/',
    name: 'Chat',
    component: Chat
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
  },
  {
    path: '/home',
    name: 'Home',
    component: Home,
  },
  {
    path: '/upload',
    name: 'Upload',
    component: UploadFile
  }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})


export default router

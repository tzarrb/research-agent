<template>
  <div id="top-bar">
    <div class="nav-bar">
      <div class="nav-bar-menu">
        <RouterLink to="/home" class="nav-bar-btn">
          首页
        </RouterLink>
        <RouterLink to="/" class="nav-bar-btn">
          聊天
        </RouterLink>
        <RouterLink to="/upload" class="nav-bar-btn">
          知识库
        </RouterLink>
      </div>

      <!-- 用户状态区域 -->
      <div class="nav-bar-user">
        <template v-if="mainStore.user.account">
          <!-- 用户头像/账户信息 -->
          <div class="nav-bar-user__info">
            <span class="nav-bar-user__name">{{ mainStore.user.account }}</span>
            <el-badge :value="mainStore.user.msgCount" class="nav-bar-user__badge">
              <el-button size="small">消息</el-button>
            </el-badge>
          </div>
        </template>
        <template v-else>
          <!-- 登录入口 -->
          <RouterLink to="/login" class="nav-bar-btn nav-bar-btn--login">
            登录
          </RouterLink>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useMainStore } from '@/store';
import { useRouter } from 'vue-router';

const mainStore = useMainStore();
const router = useRouter();
</script>

<style scoped>
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.nav-bar-menu {
  //width: 60%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}
.nav-bar-btn {
  padding: 12px 30px;
  font-size: 16px;
  color: #333;
  text-decoration: none;
  border-radius: 6px;
  transition: background-color 0.2s ease;
}

.nav-bar-user {
  display: flex;
  align-items: center;
  height: 100%;
  margin-left: auto; /* 关键调整：自动左外边距实现右对齐 */
}

.nav-bar-user__info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-bar-user__name {
  font-weight: 500;
  color: #1a1a1a;
  margin-right: 8px;
}

.nav-bar-user__badge {
  display: inline-block;
  transform: scale(0.8);
}

.nav-bar-btn--login {
  padding: 8px 16px;
  background-color: #f5f5f5;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.nav-bar-btn--login:hover {
  background-color: #e9e9e9;
}
</style>
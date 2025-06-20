<template>
  <div class="login">
    <div class="login__content">
      <div class="login__content__title">探索智能体</div>
      <!-- 账号输入框 -->
      <el-input class="login__content__input login__content__username-input" placeholder="账号" v-model="account" clearable>
        <template #prepend>
          <i class="el-icon-user" style="font-size: 16px;"></i>
        </template>
      </el-input>
      <!-- 密码输入框 -->
      <el-input class="login__content__input login__content__password-input" placeholder="密码" v-model="password" show-password clearable>
        <template #prepend>
          <i class="el-icon-lock" style="font-size: 16px;"></i>
        </template>
      </el-input>
      <el-button class="login__content__login-btn" color="#3e3f4a" type="primary" size="large" @click="loginHandler">登   录</el-button>
    </div>
  </div>
</template>

<script setup lang="ts" name="Login">
import useLogin from './hooks/useLogin';

const {
  account,
  password,
  loginHandler,
} = useLogin();

</script>

<style lang="less" scoped>
.login {
  &__content {
    margin: 20vh auto 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 50vw;
    min-width: 320px;
    padding: 24px;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
    border-radius: 12px;
    background: #ffffff;

    &__title {
      font-size: 32px;
      font-weight: 700;
      text-align: center;
      color: #1a1a1a;
      margin-bottom: 36px;
    }

    &__input {
      width: 100%;
      font-size: 16px;
      margin-bottom: 16px;
      :deep(.el-input__wrapper) {
        display: flex;
        align-items: center;
        justify-content: center;
      }
      // 修复Element Plus输入框圆角
      :deep(.el-input__inner) {
        width: 50%;
        height: 42px;
        line-height: 42px;
        border-radius: 6px;
        padding-left: 40px;
      }
      // 图标区域垂直居中
      :deep(.el-input__prepend) {
        display: flex;
        align-items: center;
        height: 48px;
        background-color: transparent;
        border-radius: 8px 0 0 8px;
      }
    }

    &__login-btn {
      width: 50%;
      height: 48px;
      font-size: 20px;
      font-weight: 600;
      border-radius: 8px;
      margin-top: 8px;
    }
  }
}
</style>
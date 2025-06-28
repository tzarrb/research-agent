import { createApp } from 'vue'
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css'
import ElementPlusX from 'vue-element-plus-x'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
// 同步全局的信息
// import { sync } from 'vuex-router-sync'

//导入样式
import './styles/main.css'


// 跳转前检查登录
// router.beforeEach((to, from, next) => {
//     // // 如果目标路由是登录页，直接放行
//     if (to.name === 'Login') {
//         next();
//         return;
//     }
//
//     // 检查是否有token
//     // const token = localStorage.getItem('token');
//     const token = store.getters.token
//     if (!token) {
//         // 如果没有token，重定向到登录页
//         next({ name: 'Login' });
//     } else {
//         // 有token，继续导航
//         next();
//     }
// })

// 跳转后操作
// router.afterEach((to) => {
//     window.scrollTo(0, 0)
// })

// 同步store和router
// sync(store, router)


const app = createApp(App)
// 批量注册图标的方法
function registerIcons(app) {
    for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
        app.component(key, component)
    }
}

// 注册路由
app.use(router)
// 调用函数批量注册所有图标
registerIcons(app)
app.use(ElementPlus);
app.use(ElementPlusX);
app.use(createPinia());

app.mount('#app')
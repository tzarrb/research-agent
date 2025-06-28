import { ref } from 'vue';
import { useMainStore } from '@/store';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

export default function () {
    const mainStore = useMainStore();
    const router = useRouter();

    /**输入框的值 */
    const account = ref('');
    const password = ref('');

    /**发起正则请求 */
    async function loginHandler() {
        const accountVal = account.value.trim();
        if (!accountVal) {
            const errorMsg = '账号不能为空!';
            ElMessage.error(errorMsg);
            return;
        }
        const passwordVal = password.value.trim();
        if (!passwordVal) {
            const errorMsg = '密码不能为空!';
            ElMessage.error(errorMsg);
            return;
        }

        // 保存账号到store
        mainStore.user.account = accountVal;
        mainStore.user.avatar = "http://gips2.baidu.com/it/u=3944689179,983354166&fm=3028&app=3028&f=JPEG&fmt=auto?w=1024&h=1024";

        await router.push('/');
    }

    return {
        /**输入框的值 */
        account,
        password,
        /**登录操作 */
        loginHandler,
    };
}
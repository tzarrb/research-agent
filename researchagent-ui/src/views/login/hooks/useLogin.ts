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
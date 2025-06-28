import { defineStore } from 'pinia';
// getters
import getters from './getters'
import * as actions from './actions'

export const useMainStore = defineStore('main', {
    state: () => ({
        user: {
            token: '',
            account: '',
            avatar: '',
            msgCount: 0
        },
        menu: {
            menuTree: [],
            openMenuList: [],
            currentPath: '',
        },
        config: {
            loading: false,
            timestamp: 0,
            locking: false,
            winWidth: window.innerWidth,
        },
    }),
    getters: getters
});
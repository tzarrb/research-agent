const getters = {
  isLogin: state => Boolean(state.apiKey.trim()),
  token: state => state.user.token,
  account: state => state.user.account,
  msgCount: state => state.user.msgCount,
  menuTree: state => state.menu.menuTree,
  openMenuList: state => state.menu.openMenuList,
  currentPath: state => state.menu.currentPath,
  timestamp: state => state.config.timestamp,
  locking: state => state.config.locking,
  loading: state => state.config.loading,
  winWidth: state => state.config.winWidth
}

export default getters

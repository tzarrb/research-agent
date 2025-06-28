<template>
  <div class="home base-page">
    <div class="nav-bar">
      RsAgent - 百宝箱 🧰
    </div>
    <div class="home__body">
      <RouterLink to="/" class="home__body__card bg-blue">
        AgentChat
      </RouterLink>
      <div class="home__body__coming-card bg-gray">
        Coming Soon ...
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.home {
  .nav-bar {
    height: 64px;
    line-height: 64px;
    padding: 0 32px;
    font-size: 26px;
    font-weight: bold;
    color: #090909;
    text-align: center;
    //background-color: #fdfbfb;
  }
  &__body {
    height: 90vh;
    display: flex;
    flex-wrap: wrap;
    padding: 20px 40px;
    justify-content: center;
    align-items: center;

    &__card {
      width: 200px;
      height: 200px;
      display: flex;
      font-size: 18px;
      justify-content: center;
      align-items: center;
      margin: 0 32px 32px 0;
      border-radius: 12px;
      color: #4a4545;
      font-weight: bold;
      background-color: #bde0e1;
      box-shadow: 0px 0px 12px rgba(0, 0, 0, .12);

      &:hover {
        cursor: pointer;
        background-image: linear-gradient(135deg, rgba(7, 11, 23, 0) 0%, rgba(7, 11, 23, 0.3) 100%);
      }
    }

    &__coming-card {
      width: 200px;
      height: 200px;
      display: flex;
      font-size: 18px;
      justify-content: center;
      align-items: center;
      margin: 0 32px 32px 0;
      border-radius: 12px;
      color: #a69c9c;
      font-weight: bold;
      box-shadow: 0px 0px 12px rgba(0, 0, 0, .12);
      cursor: not-allowed;
      user-select: none;
    }
  }
}
</style>

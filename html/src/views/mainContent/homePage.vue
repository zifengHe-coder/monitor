<template>
  <!-- 主页 -->
  <div id="homePage">
    <div>
      <p>WELCOME</p>
      <p>工业软件安全检测</p>
      <img src="../../assets/indexIcon.png" />
    </div>
  </div>
</template>
<style lang="less" scoped>
#homePage{
  height: 100%;
  background: url('../../assets/indexBg.png');
  background-size: 100% 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 540px;
  min-width: 930px;
  div{
    p{
      color: #2a7bc1;
      text-align: center;
      margin-left: 46px;
      &:nth-child(1){
        font-size: 62px;
        font-weight: 500;
        letter-spacing: 5px;
        margin-bottom: 0;
      }
      &:nth-child(2){
        font-size: 22px;
        margin-bottom: 50px;
      }
    }
  }
  img{
    display: block;
    margin: 0 auto;
    &:nth-child(1){
      margin-bottom: 60px;
    }
  }
}
</style>
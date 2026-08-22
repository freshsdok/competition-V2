<template>
  <footer class="text-white">
    <div class="w-full bg-[#333333] footer-mubg">
      <div class="flex justify-between flex-wrap">
        <div
          class="footer-links flex justify-start items-start"
          style="align-items: center"
        >
          <div>
            <img src="@/assets/images/dibulogo.png" alt="" class="w-60" />
          </div>
          <div class="xian"></div>

          <div class="ml-[50px]">
            <img src="@/assets/images/zhuban3.png" alt="" class="h-14" />
            <p>工业和信息化部人才交流中心</p>
          </div>
          <div >
            <img src="@/assets/images/zhuban1.png" alt="" class="h-14" />
            <p>中国通信企业协会</p>
          </div>
          <div>
            <img src="@/assets/images/zhuban2.png" alt="" style="padding: 5px 0;" class="h-14" />
            <p class="zxk">中信科移动通信技术股份有限公司</p>
          </div>

          <div>
            <img src="@/assets/images/xieban.png" alt="" class="h-14" />
            <p>得时（天津）智能科技有限公司</p>
          </div>
        </div>
        <div class="qrcode-container flex justify-center items-center flex-col">
          <div class="qrcode-image">
            <img src="@/assets/images/qrcode.jpg" alt="" class="qrcode" />
          </div>
          <p class="qrcode-text">微信扫码关注我们</p>
        </div>
      </div>
    </div>
    <div class="w-full bg-[#2A2A2A]">
      <div class="container-custom">
        <div class="footer-info">
          <p class="copyright-text">
            <span class="pr-3">版权所有: 大唐移动通信设备有限公司</span>
            <span>备案号: 京ICP备2024085663号</span>
          </p>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { useRouter } from "vue-router";
// Footer组件脚本部分
</script>

<style scoped lang="scss">
/* 涉及单位的样式在style标签中定义 */
.footer-mubg {
  padding: 40px 0 40px;
}
.footer-links {
  flex: 1;
  margin-left: 5%;
  .xian {
    height: 100px;
    width: 1px;
    background: #fff;
    margin-left: 60px;
  }
  p {
    text-indent: 45px;
    margin-top: 10px;
    font-size: 16px;
  }
  .zxk{
    text-indent: 40px;
  }
  img {
    margin-left: 40px;
  }
}
.qrcode-container {
  text-align: center;
  flex-shrink: 0;
  margin-right: 5%;
  .qrcode-image {
    .qrcode {
      width: 100px;
      height: 100px;
      flex-shrink: 0;
    }
  }
  .qrcode-text {
    font-size: 16px;
    color: #ffffff;
    margin-top: 25px;
    flex-shrink: 0;
  }
}
.footer-info {
  text-align: center;
}

.copyright-text {
  font-size: 16px;
  color: #ffffff;
  padding: 32px 0;
}
</style>
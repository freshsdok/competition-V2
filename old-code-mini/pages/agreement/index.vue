<template>
  <view class="container">
    <scroll-view class="content" scroll-y>
      <rich-text :nodes="content"></rich-text>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const title = ref('')
const content = ref('')

// 用户服务协议内容
const serviceAgreement = `<div class="agreement-wrap">
<h2 class="agreement-title">得时科赛小程序用户服务协议</h2>
<p class="agreement-version">版本号：V1.0.0 | 更新日期：2026年4月8日</p>
<p class="agreement-section">一、协议的接受</p>
<p class="agreement-para">欢迎使用我们的得时科赛小程序（以下简称"本平台"）。请您在使用本平台前仔细阅读并了解本协议的全部内容。一旦您点击"同意"或开始使用本平台，即表示您已同意受本协议约束。如果您不同意本协议的任何条款，请立即停止使用本平台。</p>
<p class="agreement-section">二、用户注册与账户安全</p>
<p class="agreement-para">您在使用本平台时，需要按照要求提供真实、准确、完整的个人资料，并在资料发生变更时及时更新。</p>
<p class="agreement-para">您应当妥善保管您的账户及密码，并对使用您的账户及密码进行的所有操作承担责任。</p>
<p class="agreement-para">如发现任何未经授权使用您账户的情况，您应立即通知我们。</p>
<p class="agreement-section">三、用户行为规范</p>
<p class="agreement-para">您在使用本平台时，必须遵守中华人民共和国法律法规及相关国际公约的规定。</p>
<p class="agreement-para">您不得利用本平台从事以下行为：</p>
<p class="agreement-item">（1）发布、传播含有虚假、诈骗、侮辱、诽谤、淫秽、暴力等违法或不良信息的内容；</p>
<p class="agreement-item">（2）侵犯他人知识产权、商业秘密等合法权益；</p>
<p class="agreement-item">（3）干扰本平台的正常运营，或制作、发布、传播影响本平台正常运行的程序或代码；</p>
<p class="agreement-item">（4）其他违反法律法规、社会公德或损害他人合法权益的行为。</p>
<p class="agreement-section">四、信息收集与使用</p>
<p class="agreement-para">为了向您提供更好的服务，我们可能会收集您的一些个人信息，包括但不限于您的姓名、联系方式、简历信息等。</p>
<p class="agreement-para">我们将采取严格的安全措施保护您的个人信息，不会未经您的同意向第三方披露您的个人信息，但法律法规另有规定的除外。</p>
<p class="agreement-para">您可以随时查看、修改或删除您的个人信息，但法律法规另有规定的除外。</p>
<p class="agreement-section">五、服务内容与变更</p>
<p class="agreement-para">本平台提供的服务包括但不限于学科竞赛搜索、工作岗位搜索、学习资料搜索、职业能力认证证书搜索、简历投递、消息通知、打卡签到、报名付款等功能。</p>
<p class="agreement-para">我们有权根据业务发展需要变更、暂停或终止部分或全部服务，且无需提前通知您。</p>
<p class="agreement-para">我们会尽最大努力保障本平台的正常运行，但不保证本平台完全没有错误或中断。</p>
<p class="agreement-section">六、知识产权</p>
<p class="agreement-para">本平台上的所有内容，包括但不限于文字、图片、音频、视频、软件、程序、数据等，均受知识产权法律法规的保护。</p>
<p class="agreement-para">未经我们或相关权利人书面许可，您不得复制、修改、传播、销售、出租本平台上的任何内容。</p>
<p class="agreement-section">七、免责声明</p>
<p class="agreement-para">您理解并同意，本平台仅作为信息发布和交流的平台，不对用户发布的信息的真实性、准确性、完整性负责。</p>
<p class="agreement-para">您在使用本平台过程中因第三方行为或不可抗力等因素遭受的损失，我们不承担责任。</p>
<p class="agreement-para">本平台上的链接仅为方便您使用而提供，我们不对链接指向的第三方网站的内容、隐私政策等负责。</p>
<p class="agreement-section">八、协议的修改</p>
<p class="agreement-para">我们有权根据业务发展和法律法规的变化随时修改本协议。</p>
<p class="agreement-para">修改后的协议将在本平台上公布，您继续使用本平台即表示您接受修改后的协议。</p>
<p class="agreement-para">如果您不接受修改后的协议，请立即停止使用本平台。</p>
<p class="agreement-section">九、法律适用与争议解决</p>
<p class="agreement-para">本协议的订立、执行、解释及争议的解决均适用中华人民共和国法律法规。</p>
<p class="agreement-para">如本协议的任何条款被认定为无效或不可执行，不影响其他条款的效力。</p>
<p class="agreement-para">本协议的标题仅为方便阅读而设，不影响协议条款的解释。</p>
</div>`

// 隐私政策内容
const privacyPolicy = `<div class="agreement-wrap">
<h2 class="agreement-title">得时科赛小程序隐私政策</h2>
<p class="agreement-version">版本号：V1.0.0 | 更新日期：2026年4月8日</p>
<p class="agreement-section">一、隐私政策的适用范围</p>
<p class="agreement-para">本隐私政策适用于我们的得时科赛小程序（以下简称"本平台"）提供的所有服务。本政策仅适用于本平台收集的信息，不适用于任何第三方提供的服务或第三方的信息收集、使用惯例。</p>
<p class="agreement-section">二、我们收集的信息</p>
<p class="agreement-para">您在注册账户或使用本平台服务时主动提供的信息，包括但不限于您的姓名、联系方式、简历信息等。</p>
<p class="agreement-para">您在使用本平台服务过程中产生的信息，包括但不限于您的浏览记录、搜索记录、投递记录等。</p>
<p class="agreement-para">设备信息，包括但不限于您使用的设备型号、操作系统版本、设备识别码等。</p>
<p class="agreement-para">位置信息，在您授权的情况下，我们可能会收集您的位置信息，以便为您提供更精准的服务。</p>
<p class="agreement-section">三、我们如何使用收集的信息</p>
<p class="agreement-para">提供、维护和改进我们的服务；</p>
<p class="agreement-para">处理您的学科竞赛搜索、工作岗位搜索、学习资料搜索、职业能力认证证书搜索、简历投递、消息通知、打卡签到、报名付款等功能；</p>
<p class="agreement-para">向您发送比赛信息、活动通知等；</p>
<p class="agreement-para">进行用户验证、安全防护、诈骗监测等；</p>
<p class="agreement-para">分析用户行为，优化我们的服务；</p>
<p class="agreement-para">遵守法律法规的要求。</p>
<p class="agreement-section">四、信息的存储与保护</p>
<p class="agreement-para">我们将采取各种安全措施保护您的个人信息，包括但不限于加密存储、访问控制等。</p>
<p class="agreement-para">我们会在法律要求的期限内保留您的个人信息，或为实现本政策所述目的所必需的时间内保留您的个人信息。</p>
<p class="agreement-para">尽管我们采取了合理可行的措施来保护您的个人信息，但互联网并非绝对安全的环境，我们无法保证您通过互联网传输的任何信息的安全性。</p>
<p class="agreement-section">五、信息的共享与披露</p>
<p class="agreement-para">我们不会出售您的个人信息给任何第三方。</p>
<p class="agreement-para">在以下情况下，我们可能会共享您的个人信息：</p>
<p class="agreement-item">（1）获得您的明确同意；</p>
<p class="agreement-item">（2）遵守法律法规的要求；</p>
<p class="agreement-item">（3）保护我们、我们的用户或公众的权利、财产或安全；</p>
<p class="agreement-item">（4）与我们的关联公司、服务提供商共享，但这些方必须按照我们的要求处理信息，并保持保密。</p>
<p class="agreement-section">六、您的权利</p>
<p class="agreement-para">访问权：您有权访问我们收集的关于您的个人信息。</p>
<p class="agreement-para">更正权：您有权要求我们更正不准确的个人信息。</p>
<p class="agreement-para">删除权：在特定情况下，您有权要求我们删除您的个人信息。</p>
<p class="agreement-para">限制处理权：在特定情况下，您有权限制我们对您个人信息的处理。</p>
<p class="agreement-para">数据可携带权：您有权以结构化、常用和机器可读的格式接收您提供给我们的个人信息。</p>
<p class="agreement-para">反对权：在特定情况下，您有权反对我们处理您的个人信息。</p>
<p class="agreement-section">七、Cookie和类似技术的使用</p>
<p class="agreement-para">我们可能会使用Cookie和类似技术来收集和存储您的信息，以便为您提供更个性化的服务。</p>
<p class="agreement-para">大多数网络浏览器默认接受Cookie，但您通常可以修改浏览器设置来拒绝Cookie。</p>
<p class="agreement-para">如果您选择拒绝Cookie，您可能无法使用本平台的某些功能。</p>
<p class="agreement-section">八、未成年人的保护</p>
<p class="agreement-para">我们非常重视对未成年人个人信息的保护。如果您是未满18周岁的未成年人，在使用本平台前，应事先取得您的父母或法定监护人的同意。</p>
<p class="agreement-para">对于经父母或法定监护人同意而收集的未成年人的个人信息，我们只会在法律允许、父母或监护人明确同意或者保护未成年人所必要的情况下使用或公开披露此信息。</p>
<p class="agreement-section">九、隐私政策的更新</p>
<p class="agreement-para">我们可能会不时更新本隐私政策。</p>
<p class="agreement-para">更新后的隐私政策将在本平台上公布，您继续使用本平台即表示您接受更新后的隐私政策。</p>
<p class="agreement-para">如果更新的内容涉及您的重大权益，我们将采取适当方式通知您。</p>
</div>`

onLoad((options) => {
  const type = options.type || 'service'
  if (type === 'service') {
    title.value = '用户服务协议'
    content.value = serviceAgreement
  } else {
    title.value = '隐私政策'
    content.value = privacyPolicy
  }
  
  // 设置页面标题
  uni.setNavigationBarTitle({
    title: title.value
  })
})
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #ffffff;
}

.content {
  padding: 20px 16px 100px;
}

/* 富文本样式 - 使用 class 代替内联样式 */
:deep(.agreement-wrap) {
  line-height: 1.8;
  color: #333;
  font-size: 14px;
}

:deep(.agreement-title) {
  text-align: center;
  margin-bottom: 8px;
  font-size: 22px;
  font-weight: bold;
  color: #000;
}

:deep(.agreement-subtitle) {
  text-align: center;
  margin-bottom: 4px;
  font-size: 20px;
  font-weight: bold;
  color: #000;
}

:deep(.agreement-version) {
  text-align: center;
  margin-bottom: 24px;
  color: #666;
  font-size: 14px;
}

:deep(.agreement-section) {
  font-weight: bold;
  color: #000;
  margin-top: 6px;
}

:deep(.agreement-para) {
  margin-bottom: 2px;
  text-align: justify;
  text-indent: 2em;
}

:deep(.agreement-item) {
  margin-bottom: 6px;
  text-align: justify;
  text-indent: 2em;
}
</style>

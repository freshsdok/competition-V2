<template>
  <view class="page">
    <view class="tip">请核对开票抬头。提交成功后不可在小程序中修改。</view>

    <view v-for="(item, index) in groups" :key="item.merId" class="form-card">
      <view class="card-head">
        <view>
          <text class="merchant">{{ item.merName || '收款单位' }}</text>
          <text class="tax-no">纳税人识别号：{{ item.taxNum || '-' }}</text>
        </view>
        <text class="amount">¥{{ money(item.invoiceAmount) }}</text>
      </view>

      <view class="selection-summary">
        <text>{{ item.commodityType === 'cert' ? '赛证互通' : '赛事报名' }}</text>
        <text>{{ (item.userIds || []).length }} 个开票项目</text>
      </view>

      <view class="field required">
        <text class="label">抬头类型</text>
        <picker :range="classOptions" range-key="label" :value="formIndex(forms[index]?.invoiceClass)" @change="event => changeClass(index, event)">
          <view class="picker-value">{{ forms[index]?.invoiceClass === '1' ? '个人' : '企业' }} <text>›</text></view>
        </picker>
      </view>
      <view class="field required">
        <text class="label">发票抬头</text>
        <picker
          :range="invoiceTitleOptions(forms[index])"
          range-key="label"
          :value="invoiceTitleIndex(forms[index])"
          @click="checkInvoiceTitles(forms[index])"
          @change="event => changeInvoiceTitle(index, event)"
        >
          <view class="picker-value" :class="{ placeholder: !invoiceTitleLabel(forms[index]) }">
            {{ invoiceTitleLabel(forms[index]) || '请选择发票抬头' }} <text>›</text>
          </view>
        </picker>
      </view>
      <view v-if="forms[index].invoiceClass === '2'" class="field required">
        <text class="label">纳税人识别号</text>
        <input v-model.trim="forms[index].buyerTaxNumber" class="input" maxlength="20" placeholder="请输入8-20位税号" />
      </view>
      <view v-else class="personal-tip">个人抬头的发票一般无法在单位报销，请确认发票抬头。</view>
      <view v-if="forms[index].buyerNametype === '个人实名'" class="real-name-row">
        <text class="label">个人实名</text>
        <text class="real-name">{{ forms[index].buyerName || '未获取到实名信息' }}</text>
      </view>
      <view class="field required">
        <text class="label">发票内容</text>
        <picker :range="goodsOptions(item)" range-key="label" :value="goodsIndex(item, forms[index]?.goodsCode)" @change="event => changeGoods(index, item, event)">
          <view class="picker-value">{{ goodsLabel(item, forms[index]?.goodsCode) }} <text>›</text></view>
        </picker>
      </view>
      <view class="field">
        <text class="label">接收邮箱</text>
        <input v-model.trim="forms[index].email" class="input" type="text" placeholder="选填，用于接收电子发票" />
      </view>
      <view class="textarea-field">
        <text class="label">发票备注（选填）</text>
        <textarea v-model="forms[index].remark" class="textarea" maxlength="115" placeholder="填写后展示在发票备注区域" @input="trimRemark(index)" />
        <text class="count">{{ forms[index].remark.length }}/115</text>
      </view>
    </view>

    <view v-if="loading" class="empty">正在计算开票金额...</view>
    <view v-else-if="!groups.length" class="empty">未获取到可开票项目</view>

    <view class="footer-space"></view>
    <view v-if="groups.length" class="footer">
      <view><text class="total-label">开票总额</text><text class="total">¥{{ totalAmount }}</text></view>
      <button class="submit-btn" :disabled="submitting" @click="submit">{{ submitting ? '提交中...' : '申请开票' }}</button>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getInvoiceAmount, listInvoiceTitles, applyInvoice } from '@/api/invoice'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()
const groups = ref([])
const forms = ref([])
const enterpriseTitles = ref([])
const personalRealName = ref('')
const loading = ref(true)
const submitting = ref(false)
const classOptions = [{ label: '企业', value: '2' }, { label: '个人', value: '1' }]
const personalTitles = computed(() => [
  { label: '个人', buyerName: '个人', buyerNametype: '个人', taxpayerIdentificationNumber: '' },
  { label: '个人实名', buyerName: personalRealName.value, buyerNametype: '个人实名', taxpayerIdentificationNumber: '' }
])
const totalAmount = computed(() => groups.value.reduce((sum, item) => sum + Number(item.invoiceAmount || 0), 0).toFixed(2))

onLoad(async () => {
  const selection = uni.getStorageSync('personal_invoice_selection')
  if (!Array.isArray(selection) || !selection.length) {
    loading.value = false
    uni.showToast({ title: '请先选择开票项目', icon: 'none' })
    return
  }
  try {
    const [res, titleRes, profile] = await Promise.all([
      getInvoiceAmount(selection),
      listInvoiceTitles().catch(() => null),
      userStore.getUserInfo().catch(() => null)
    ])
    enterpriseTitles.value = normalizeEnterpriseTitles(titleRes?.data)
    personalRealName.value = profile?.userName || ''
    groups.value = Array.isArray(res?.data) ? res.data : []
    forms.value = groups.value.map(item => {
      const goods = goodsOptions(item)
      return {
        merId: item.merId,
        invoiceAmount: item.invoiceAmount,
        userIds: item.userIds || [],
        orderIds: item.orderIds || [],
        invoiceClass: '2',
        invoiceLine: 'pc',
        buyerName: '',
        buyerNametype: '',
        buyerTaxNumber: '',
        email: '',
        remark: '',
        invoiceType: '1',
        commodityType: item.commodityType || '',
        randomId: uuid(),
        goodsCode: goods[0]?.value || ''
      }
    })
  } finally {
    loading.value = false
  }
})

function goodsOptions(item) {
  const values = Array.isArray(item?.invoiceContent) ? item.invoiceContent : []
  return values.flatMap(entry => Object.keys(entry || {}).map(key => ({ value: key, label: entry[key] })))
}
function goodsIndex(item, value) { const index = goodsOptions(item).findIndex(option => option.value === value); return Math.max(index, 0) }
function goodsLabel(item, value) { return goodsOptions(item).find(option => option.value === value)?.label || '请选择' }
function formIndex(value) { return value === '1' ? 1 : 0 }
function normalizeEnterpriseTitles(values) {
  if (!Array.isArray(values)) return []
  const seen = new Set()
  return values.reduce((result, item) => {
    const enterpriseName = String(item?.enterpriseName || '').trim()
    const taxNumber = String(item?.taxpayerIdentificationNumber || '').trim()
    const key = `${enterpriseName}|${taxNumber}`
    if (!enterpriseName || seen.has(key)) return result
    seen.add(key)
    result.push({
      label: enterpriseName,
      buyerName: enterpriseName,
      buyerNametype: '',
      taxpayerIdentificationNumber: taxNumber
    })
    return result
  }, [])
}
function invoiceTitleOptions(form) {
  return form?.invoiceClass === '1' ? personalTitles.value : enterpriseTitles.value
}
function invoiceTitleIndex(form) {
  const options = invoiceTitleOptions(form)
  const index = options.findIndex(option => option.buyerName === form?.buyerName && option.buyerNametype === (form?.buyerNametype || ''))
  return Math.max(index, 0)
}
function invoiceTitleLabel(form) {
  if (!form?.buyerName) return ''
  if (form.invoiceClass === '1') return form.buyerNametype || ''
  return form.buyerName
}
function checkInvoiceTitles(form) {
  if (form?.invoiceClass === '2' && !enterpriseTitles.value.length) {
    uni.showToast({ title: '暂无企业抬头，请先在PC端维护', icon: 'none' })
  }
}
function changeClass(index, event) {
  const value = classOptions[Number(event.detail.value)]?.value || '2'
  forms.value[index].invoiceClass = value
  forms.value[index].buyerName = value === '1' ? '个人' : ''
  forms.value[index].buyerNametype = value === '1' ? '个人' : ''
  forms.value[index].buyerTaxNumber = ''
}
function changeInvoiceTitle(index, event) {
  const form = forms.value[index]
  const option = invoiceTitleOptions(form)[Number(event.detail.value)]
  if (!option) return
  if (option.buyerNametype === '个人实名' && !option.buyerName) {
    uni.showToast({ title: '未获取到实名信息，请稍后重试', icon: 'none' })
  }
  form.buyerName = option.buyerName
  form.buyerNametype = option.buyerNametype
  form.buyerTaxNumber = option.taxpayerIdentificationNumber
}
function changeGoods(index, item, event) {
  forms.value[index].goodsCode = goodsOptions(item)[Number(event.detail.value)]?.value || ''
}
function trimRemark(index) {
  let text = forms.value[index].remark || ''
  while (byteLength(text) > 230) text = text.slice(0, -1)
  forms.value[index].remark = text
}
function byteLength(text) { return [...text].reduce((sum, char) => sum + (char.charCodeAt(0) > 127 ? 2 : 1), 0) }
function money(value) { return Number(value || 0).toFixed(2) }
function validate() {
  for (const form of forms.value) {
    if (!form.buyerName) return form.invoiceClass === '2' && !enterpriseTitles.value.length ? '请先在PC端维护企业发票抬头' : '请选择发票抬头'
    if (form.invoiceClass === '2' && !/^[A-Z0-9]{8,20}$/i.test(form.buyerTaxNumber || '')) return '请输入正确的纳税人识别号'
    if (!form.goodsCode) return '请选择发票内容'
    if (form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) return '请输入正确的邮箱地址'
  }
  return ''
}
async function submit() {
  if (submitting.value) return
  const message = validate()
  if (message) {
    uni.showToast({ title: message, icon: 'none' })
    return
  }
  const confirmed = await new Promise(resolve => {
    uni.showModal({ title: '确认申请开票', content: `本次开票总额 ¥${totalAmount.value}，提交后不可修改。`, confirmColor: '#3169F8', success: res => resolve(res.confirm) })
  })
  if (!confirmed) return
  submitting.value = true
  try {
    await applyInvoice(forms.value)
    uni.removeStorageSync('personal_invoice_selection')
    uni.showToast({ title: '开票申请已提交', icon: 'success' })
    setTimeout(() => uni.redirectTo({ url: '/pages-personal/invoice/index' }), 600)
  } finally {
    submitting.value = false
  }
}
function uuid() {
  return 'xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx'.replace(/[xy]/g, char => {
    const random = Math.floor(Math.random() * 16)
    return (char === 'x' ? random : (random & 3) | 8).toString(16)
  })
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; padding: 24rpx 28rpx 0; background: #f5f7fb; box-sizing: border-box; }
.tip { padding: 19rpx 22rpx; border-radius: 14rpx; color: #855314; background: #fff2df; font-size: 23rpx; line-height: 1.5; }
.form-card { margin-top: 22rpx; padding: 26rpx; border-radius: 22rpx; background: #fff; box-shadow: 0 6rpx 20rpx rgba(24, 52, 110, .05); }
.card-head { display: flex; justify-content: space-between; gap: 20rpx; padding-bottom: 22rpx; border-bottom: 1rpx solid #edf0f5; }
.merchant, .tax-no { display: block; }
.merchant { color: #27334a; font-size: 28rpx; font-weight: 700; }
.tax-no { margin-top: 8rpx; color: #8a94a5; font-size: 21rpx; }
.amount { color: #e05248; font-size: 32rpx; font-weight: 700; }
.selection-summary { display: flex; justify-content: space-between; margin: 20rpx 0 8rpx; padding: 16rpx 18rpx; border-radius: 12rpx; color: #536179; background: #f5f7fb; font-size: 22rpx; }
.field { display: flex; align-items: center; justify-content: space-between; gap: 24rpx; min-height: 94rpx; border-bottom: 1rpx solid #edf0f5; }
.label { color: #3d485b; font-size: 25rpx; }
.required .label::before { content: '*'; margin-right: 5rpx; color: #e05248; }
.field picker { flex: 1; }
.picker-value { color: #28344a; text-align: right; font-size: 25rpx; }
.picker-value.placeholder { color: #a8afbc; }
.picker-value text { margin-left: 12rpx; color: #9aa3b3; font-size: 34rpx; }
.input { flex: 1; height: 78rpx; color: #28344a; text-align: right; font-size: 25rpx; }
.personal-tip { margin: 18rpx 0 4rpx; padding: 16rpx 18rpx; border-radius: 12rpx; color: #3f6bb3; background: #edf4ff; font-size: 21rpx; line-height: 1.5; }
.real-name-row { display: flex; align-items: center; justify-content: space-between; min-height: 82rpx; border-bottom: 1rpx solid #edf0f5; }
.real-name { color: #28344a; font-size: 25rpx; }
.textarea-field { position: relative; padding: 26rpx 0 12rpx; }
.textarea { width: 100%; height: 150rpx; margin-top: 16rpx; padding: 18rpx; border-radius: 14rpx; background: #f5f7fb; font-size: 24rpx; box-sizing: border-box; }
.count { display: block; margin-top: 8rpx; color: #9ba4b3; text-align: right; font-size: 20rpx; }
.empty { padding: 130rpx 0; color: #99a2b2; text-align: center; font-size: 25rpx; }
.footer-space { height: 126rpx; }
.footer { position: fixed; right: 0; bottom: 0; left: 0; display: flex; align-items: center; justify-content: space-between; padding: 18rpx 30rpx calc(18rpx + env(safe-area-inset-bottom)); background: #fff; box-shadow: 0 -5rpx 20rpx rgba(25, 46, 89, .08); }
.total-label { color: #6d788a; font-size: 23rpx; }
.total { margin-left: 12rpx; color: #e05248; font-size: 32rpx; font-weight: 700; }
button::after { border: none; }
.submit-btn { width: 250rpx; height: 76rpx; line-height: 76rpx; margin: 0; border-radius: 40rpx; color: #fff; background: #3169f8; font-size: 28rpx; }
.submit-btn[disabled] { color: #fff; background: #aebfe9; }
</style>

<template>
  <el-card class="cert__item-container">
    <el-image :src="coverImg" fit="cover" class="__cover">
      <template #error>
        <img src="@/assets/images/certInterconnect_icon.png" alt="" srcset="">
      </template>
    </el-image>
    <div class="pl-[15px] pr-[15px] pt-[10px] pb-[20px]">
      <div class="__title">
        <TextEllipsisTooltip :text="title" :lines="1" />
      </div>
      <div class="__content">
        <div class="flex justify-between mt-[10px] h-[40px]">
          <span class="flex">
            <span>兑换条件：</span>
            <span class="flex-1">
              <TextEllipsisTooltip :text="certConditions" :lines="2" />
            </span>
          </span>
        </div>
        <div class="flex justify-between mt-[15px] items-center">
          <span>{{ time }}</span>
          <el-button size="small" type="primary" @click="handleClick">申 请</el-button>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup name="CertRuleItem">
  // ************ 组件 *************
  import TextEllipsisTooltip from "@/components/TextEllipsisTooltip"

  // ************ Props *************
  const props = defineProps({
    coverImg: {
      type: String,
      default: ''
    },
    title: {
      type: String,
      default: '赛证互通规则',
    },
    originCertList: {
      type: Array,
      default: () => ([]),
    },
    targetCertList: {
      type: Array,
      default: () => ([]),
    },
    certConditions: {
      type: String,
      default: '1',
    },
    viewNum: {
      type: Number,
      default: 16
    },
    time: {
      type: String,
      default: '2026-03-31'
    }
  })
  // emit
  const emit = defineEmits(['click'])
  // 兑换条件说明
  const certConditions = computed(() => `${props.originCertList.map(item => (item.certConfigName)).join(`${props.certConditions === '1' ? '且' : '或'}`)}`)
  // 点击兑换
  const handleClick = () => {
    emit('click')
  }

</script>

<style lang="scss" scoped>
  .el-card.cert__item-container {
    display: flex;
    width: 240px;
    flex-direction: column;
    border-radius: 12px !important;
    transition: transform 200ms ease, box-shadow 200ms ease, border-color 200ms ease;
    cursor: pointer;

    &:hover {
      transform: translateY(-8px) scale(1.04);
      box-shadow: 0 18px 34px rgba(102, 102, 102, 0.45);
      border-color: rgba(150, 150, 150, 0.3);
    }

    &:active {
      transform: translateY(0px) scale(1);
    }

    & .__cover {
      width: 240px;
      height: 240px;
      object-fit: cover;
    }

    & .__title {
      font-family: Source Han Sans CN, Source Han Sans CN;
      font-weight: bold;
      font-size: 18px;
      color: #333333;
      line-height: 25px;
      text-align: left;
      font-style: normal;
      text-transform: none;
      margin-bottom: 10px;
    }

    & .__content {
      font-family: Source Han Sans CN, Source Han Sans CN;
      font-weight: 400;
      font-size: 13px;
      color: #999999;
      font-style: normal;
      text-transform: none;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  :deep(.el-card__body) {
    padding: 0 !important;
  }

  :deep(.el-button) {
    font-size: 12px !important;
    background: #3169F8 !important;
  }
</style>
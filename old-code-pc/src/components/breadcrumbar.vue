<template>
  <!-- 目前只做了赛事的适配，其他的页面请自己根据需求修改 -->
<div class="breadcrumbar">
  <div class="breadcrumbar-line flex items-center justify-start">
    <span v-for="(item, index) in breadcrumbarList" :key="index" @click="goTo(item,index)" class="cursor-pointer">
      <span :class="{'nav-link-active': item.path === route.path}">{{ item?.meta.label }}</span>
      <span class="nav-link-line" v-if="getShowLine(index)">></span>

    </span>
  </div>
</div>
</template>

<script setup>
import { useRoute,useRouter  } from 'vue-router'
const props = defineProps({
  breadcrumbarArr: {
    type: Array,
    default: () => [],
  }
})


const route = useRoute()
const breadcrumbarList = $computed(() => {
  let matched = props.breadcrumbarArr && props.breadcrumbarArr.length ? props.breadcrumbarArr : route.matched; 
  return matched
})
// 是否显示分割线
const getShowLine =(index)=> {
  try {
    // 安全检查：确保breadcrumbarList是数组
    if (!Array.isArray(breadcrumbarList)) {
      return false;
    }
    
    // 安全检查：确保索引在有效范围内
    if (index < 0 || index >= breadcrumbarList.length) {
      return false;
    }
    
    // 检查是否为最后一项
    if (index === breadcrumbarList.length - 1) {
      return false;
    }

     if ( !breadcrumbarList[index+1].meta.label) {
      return false;
    }
    let nextItem = breadcrumbarList[index+1];
    // 安全检查：确保nextItem存在且有meta属性
    if (!nextItem || !nextItem.meta) {
      return false;
    }
    
    return nextItem.meta.hiddenTriangle !== 'hidden';
  } catch (error) {
    console.warn('面包屑分割线显示判断错误:', error);
    return false;
  }
}

const router = useRouter()
const goTo = (item,index) => {
  let query = {}
  if(index <= 1) {
    query = {}
  }else{
    query = route.query
  }
  router.push({
    path: item.path,
    query: query,
  })
}
</script>

<style scoped>
.breadcrumbar{
  margin: 25px 0 20px;
}
.breadcrumbar-line{
  font-size: 14px;
  color: #999999;
}
.nav-link-name{
  font-size: 16px;
  color: #333333;
}
.nav-link-active{
  font-size: 16px;
  color: #333333;
}
.nav-link-line{
  margin: 0 5px;
}
</style>
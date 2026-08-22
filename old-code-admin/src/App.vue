<template>
  <router-view />
</template>

<script setup>
import useSettingsStore from '@/store/modules/settings'
import { handleThemeStyle } from '@/utils/theme'
import { VxeUI } from 'vxe-table'
onMounted(() => {
  nextTick(() => {
    // 初始化主题样式
    handleThemeStyle(useSettingsStore().theme)
    VxeUI.setTheme(settingsStore.isDark ? 'dark' : '')
  })
})

const settingsStore = useSettingsStore()
watch(() => settingsStore.isDark, (newVal) => {
  VxeUI.setTheme(newVal ? 'dark' : '')
})
</script>

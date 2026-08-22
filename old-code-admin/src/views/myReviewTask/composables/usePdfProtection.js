import { onMounted, onBeforeUnmount } from 'vue'

/**
 * PDF 防护组合式函数
 * 禁用右键、快捷键、开发者工具检测
 */
export function usePdfProtection(options = {}) {
  const {
    disableContextMenu = true,    // 禁用右键菜单
    disableShortcuts = true,       // 禁用快捷键
    disableDevTools = true,        // 检测开发者工具
    onDevToolsOpen = null,         // 开发者工具打开回调
    targetRef = null               // 目标元素ref，只对该元素及其子元素生效
  } = options

  // 禁用快捷键列表（只在目标区域生效）
  const blockedKeys = [
    { key: 's', ctrl: true },      // Ctrl+S 保存
    { key: 'p', ctrl: true },      // Ctrl+P 打印
    { key: 'a', ctrl: true },      // Ctrl+A 全选
    { key: 'c', ctrl: true },      // Ctrl+C 复制
    { key: 'x', ctrl: true },      // Ctrl+X 剪切
    { key: 'v', ctrl: true },      // Ctrl+V 粘贴
    { key: 'u', ctrl: true },      // Ctrl+U 查看源码
  ]

  // 开发者工具快捷键（全局生效）
  const devToolsKeys = [
    { key: 'F12' },                // F12 开发者工具
    { key: 'F12', ctrl: true },    // Ctrl+F12
    { key: 'F12', shift: true },   // Shift+F12
    { key: 'i', ctrl: true, shift: true }, // Ctrl+Shift+I
    { key: 'j', ctrl: true, shift: true }, // Ctrl+Shift+J
  ]

  // 阻止右键菜单 - 全局禁用（防止通过右键检查打开开发者工具）
  function handleContextMenu(e) {
    if (!disableContextMenu) return

    // 全局禁用右键菜单
    e.preventDefault()
    return false
  }

  // 检查按键是否匹配列表
  function checkKeyMatch(e, keyList) {
    const key = e.key || e.keyCode
    const ctrl = e.ctrlKey || e.metaKey
    const shift = e.shiftKey
    const alt = e.altKey

    return keyList.some(item => {
      const keyMatch = item.key === key || item.keyCode === key
      const ctrlMatch = item.ctrl ? ctrl : !ctrl
      const shiftMatch = item.shift ? shift : !shift
      const altMatch = item.alt ? alt : !alt
      return keyMatch && ctrlMatch && shiftMatch && altMatch
    })
  }

  // 阻止快捷键
  function handleKeyDown(e) {
    if (!disableShortcuts) return

    const key = e.key || e.keyCode
    const ctrl = e.ctrlKey || e.metaKey
    const shift = e.shiftKey

    // 优先检查开发者工具快捷键（全局生效）
    if (checkKeyMatch(e, devToolsKeys)) {
      e.preventDefault()
      e.stopPropagation()
      return false
    }

    // 如果指定了目标元素，只阻止目标元素获得焦点时的其他快捷键
    if (targetRef?.value) {
      // 检查当前焦点元素是否在目标元素内
      const activeElement = document.activeElement
      const isTargetFocused = targetRef.value.contains(activeElement) || targetRef.value === activeElement
      // 检查选中的文本是否在目标元素内
      const selection = window.getSelection()
      let isSelectionInTarget = false
      if (selection && selection.rangeCount > 0) {
        const range = selection.getRangeAt(0)
        isSelectionInTarget = targetRef.value.contains(range.commonAncestorContainer)
      }

      if (!isTargetFocused && !isSelectionInTarget) {
        return
      }
    }

    // 检查其他阻止列表
    if (checkKeyMatch(e, blockedKeys)) {
      e.preventDefault()
      e.stopPropagation()
      return false
    }
  }

  // 检测开发者工具（基于窗口大小差异）
  let devToolsCheckTimer = null
  let lastOuterWidth = window.outerWidth
  let lastOuterHeight = window.outerHeight

  function checkDevTools() {
    if (!disableDevTools) return

    const threshold = 160 // 阈值
    const widthDiff = window.outerWidth - window.innerWidth
    const heightDiff = window.outerHeight - window.innerHeight

    // 检测开发者工具是否打开
    if (widthDiff > threshold || heightDiff > threshold) {
      if (onDevToolsOpen) {
        onDevToolsOpen()
      }
    }
  }

  // 启动防护
  function startProtection() {
    // 禁用右键
    if (disableContextMenu) {
      document.addEventListener('contextmenu', handleContextMenu, true)
    }

    // 禁用快捷键
    if (disableShortcuts) {
      document.addEventListener('keydown', handleKeyDown, true)
    }

    // 检测开发者工具（降低检测频率，减少CPU占用）
    if (disableDevTools) {
      devToolsCheckTimer = setInterval(checkDevTools, 15000) // 15秒检测一次
      window.addEventListener('resize', checkDevTools)
    }
  }

  // 停止防护
  function stopProtection() {
    document.removeEventListener('contextmenu', handleContextMenu, true)
    document.removeEventListener('keydown', handleKeyDown, true)
    clearInterval(devToolsCheckTimer)
    window.removeEventListener('resize', checkDevTools)
  }

  onMounted(() => {
    startProtection()
  })

  onBeforeUnmount(() => {
    stopProtection()
  })

  return {
    startProtection,
    stopProtection
  }
}

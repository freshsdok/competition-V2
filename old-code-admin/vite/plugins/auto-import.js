import autoImport from 'unplugin-auto-import/vite'

export default function createAutoImport() {
    return autoImport({
        imports: [
            'vue',
            'vue-router',
            'pinia',
            {
                'element-plus': [
                    // Message 组件
                    'ElMessage',
                    // MessageBox 组件
                    'ElMessageBox',
                    'ElMessageBox as $msgbox',
                    'ElMessageBox.alert',
                    'ElMessageBox.confirm',
                    'ElMessageBox.prompt',
                    // Notification 组件
                    'ElNotification',
                    // Loading 组件
                    'ElLoading',
                    // 表单相关方法
                    'ElForm',
                    'ElFormItem',
                    'ElInput',
                    'ElSelect',
                    'ElOption',
                    'ElCheckbox',
                    'ElCheckboxGroup',
                    'ElRadio',
                    'ElRadioGroup',
                    'ElSwitch',
                    'ElDatePicker',
                    'ElTimePicker',
                    // 容器组件
                    'ElContainer',
                    'ElHeader',
                    'ElAside',
                    'ElMain',
                    'ElFooter',
                    // 表格组件
                    'ElTable',
                    'ElTableColumn',
                    // 布局组件
                    'ElRow',
                    'ElCol',
                    // 按钮组件
                    'ElButton',
                    'ElButtonGroup',
                    // 对话框组件
                    'ElDialog',
                    'ElDrawer',
                    // 导航组件
                    'ElMenu',
                    'ElMenuItem',
                    'ElMenuItemGroup',
                    'ElSubMenu',
                    'ElBreadcrumb',
                    'ElBreadcrumbItem',
                    // 提示组件
                    'ElTooltip',
                    'ElPopover',
                    'ElPopconfirm',
                    // 数据展示组件
                    'ElCard',
                    'ElAvatar',
                    'ElBadge',
                    'ElEmpty',
                    'ElTag',
                    // 上传组件
                    'ElUpload',
                    // 树组件
                    'ElTree',
                    // 分页组件
                    'ElPagination',
                    // 进度条
                    'ElProgress',
                    // 骨架屏
                    'ElSkeleton',
                    // 步骤条
                    'ElSteps',
                    'ElStep',
                    // 分割线
                    'ElDivider',
                    // 滚动条
                    'ElScrollbar',
                    // 级联选择器
                    'ElCascader',
                    // 颜色选择器
                    'ElColorPicker',
                    // 输入框组合
                    'ElInputNumber',
                    // 滑块
                    'ElSlider',
                    // 时间线
                    'ElTimeline',
                    'ElTimelineItem',
                    // 统计数值
                    'ElStatistic',
                    // 结果
                    'ElResult',
                    // 下拉菜单
                    'ElDropdown',
                    'ElDropdownMenu',
                    'ElDropdownItem'
                ]
            }
        ],
        dts: false,
        eslintrc: {
            enabled: false
        }
    })
}
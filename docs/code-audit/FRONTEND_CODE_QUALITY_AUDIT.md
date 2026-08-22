# 前端代码质量审计

## 范围统计

| 前端工程 | 文件数 | 行数 |
| --- | --- | --- |
| old-code-admin | 469 | 107084 |
| old-code-pc | 211 | 48147 |
| old-code-mini | 44 | 7334 |

## 高风险前端文件

| 文件 | 行数 | 风险分 |
| --- | --- | --- |
| old-code-admin/src/utils/generator/html.js | 359 | 2115.4 |
| old-code-admin/src/views/course/courseInfo/index.vue | 3232 | 1146.6 |
| old-code-admin/src/views/tournament/sceneSchedule/index.vue | 3150 | 1017.0 |
| old-code-pc/src/views/personal/components/TeamDetails.vue | 2467 | 615.9 |
| old-code-admin/src/views/content/page/editContent/utils/componentRenderer.js | 1274 | 591.2 |
| old-code-admin/src/views/content/page/editContent/components/PropertyPanel.vue | 1959 | 504.9 |
| old-code-admin/src/views/review/my-review/index.vue | 1718 | 504.9 |
| old-code-admin/src/utils/generator/js.js | 370 | 476.0 |
| old-code-pc/src/views/personal/personaltabs/Competition.vue | 2073 | 445.6 |
| old-code-mini/pages/my-credential/index.vue | 1448 | 404.9 |
| old-code-mini/pages/review-secretary/index.vue | 1028 | 383.4 |
| old-code-admin/src/views/tournament/sceneCredentialCompetition/index.vue | 952 | 349.6 |
| old-code-admin/src/views/review/session/index.vue | 794 | 345.2 |
| old-code-admin/src/components/Reviewtask/index.vue | 1310 | 332.0 |
| old-code-pc/src/views/personal/personaltabs/myfile.vue | 1215 | 326.8 |
| old-code-admin/src/plugins/package/penal/task/task-components/UserTask.vue | 566 | 325.3 |
| old-code-admin/src/views/review/secretary/session.vue | 787 | 307.9 |
| old-code-mini/pages/scan/result.vue | 1090 | 300.5 |
| old-code-admin/src/views/iPayment/merchant/index.vue | 1022 | 291.1 |
| old-code-admin/src/views/course/chapterVideo/index.vue | 772 | 285.6 |
| old-code-pc/src/plugins/package/penal/task/task-components/UserTask.vue | 561 | 285.1 |
| old-code-admin/src/views/review/assignment/index.vue | 730 | 267.5 |
| old-code-admin/src/views/review/import/index.vue | 728 | 264.9 |
| old-code-admin/src/views/tournament/reviewManage/components/BatchActionBar.vue | 608 | 258.4 |
| old-code-pc/src/views/personal/personaltabs/SceneResourceReservation.vue | 471 | 249.6 |
| old-code-admin/src/views/fileTask/fileTaskList/components/index.vue | 1121 | 249.1 |
| old-code-mini/pages/mine/index.vue | 287 | 245.8 |
| old-code-admin/src/components/Reviewtask/chapterVideo.vue | 394 | 239.2 |
| old-code-admin/src/views/tournament/sceneSchedule/components/ResourceSlotDialog.vue | 696 | 236.8 |
| old-code-admin/src/views/iPayment/order/index.vue | 1112 | 229.1 |

## 直接证据

| 类型 | 位置 | 代码 |
| --- | --- | --- |
| v-html | old-code-admin/src/views/content/detailPage/index.vue:41 | <div class="detail-content" v-html="detailData.detailContent"></div> |
| v-html | old-code-admin/src/views/course/courseInfo/index.vue:147 | <div v-html="scope.row.details" style="max-height: 50px; overflow: hidden; text-overflow: ellipsis;"></div> |
| v-html | old-code-admin/src/views/tournament/promote/index.vue:148 | v-html="scope.row.promotedHint" |
| v-html | old-code-admin/src/views/tournament/team/changeLog.vue:22 | <div v-if="scope.row.changeType === 'changeTeacher'" v-html="highlightDiff(scope.row.teacherNameOld, scope.row.teacherNameNew)"></div> |
| v-html | old-code-admin/src/views/tournament/team/changeLog.vue:23 | <div v-else v-html="highlightDiff(scope.row.memberNameOld, scope.row.memberNameNew)"></div> |
| v-html | old-code-admin/src/views/tournament/team/changeLog.vue:28 | <div v-if="scope.row.changeType === 'changeTeacher'" v-html="highlightDiff(scope.row.teacherNameNew, scope.row.teacherNameOld)"></div> |
| v-html | old-code-admin/src/views/tournament/team/changeLog.vue:29 | <div v-else v-html="highlightDiff(scope.row.memberNameNew, scope.row.memberNameOld)"></div> |
| v-html | old-code-pc/src/components/DS_C/ds_list_one.vue:79 | <div class="content-desc text-ellipsis rich-content" v-html="item.competitionDesc"></div> |
| v-html | old-code-pc/src/components/DS_C/ds_support_home.vue:32 | <div class="ml-[15px]" v-html="highlightKeyword(item.questions, keyWord)"></div> |
| v-html | old-code-pc/src/components/DS_C/ds_support_home.vue:35 | <div class="text-[#666666] text-[16px]" v-html="highlightKeyword(item.answer, keyWord)"></div> |
| v-html | old-code-pc/src/components/DS_C/ds_tournament_home.vue:33 | <div class="w-full rich-content tur-content-right-desc" v-html="tourDetail?.competitionDesc \\|\\| ''"></div> |
| v-html | old-code-pc/src/views/awardPublicity/index.vue:45 | <div class="rich-content ql-editor" v-html="currentCompetition?.tipInfo \\|\\| ''"> |
| v-html | old-code-pc/src/views/certInterconnect/description.vue:23 | <div class="rich-content ql-editor" v-html="pageDetail?.applyDesc \\|\\| ''"> |
| v-html | old-code-pc/src/views/certInterconnect/details.vue:16 | <div class="rich-content ql-editor" v-html="ruleData.applyDesc"></div> |
| v-html | old-code-pc/src/views/customize/index.vue:93 | <div class="rich-content ql-editor" v-html="pageDetail?.detailList[0]?.detailContent" v-if="pageDetail?.detailList[0]?.detailContent"> |
| console.log | old-code-admin/src/components/Editor/index.vue:127 | console.log('图片上传服务端返回：', res); |
| console.log | old-code-admin/src/components/Editor/index.vue:157 | console.log('视频上传服务端返回：', res); |
| console.log | old-code-admin/src/components/Editor/index.vue:216 | console.log('focus', editor); |
| console.log | old-code-admin/src/components/Editor/index.vue:219 | console.log('blur', editor); |
| console.log | old-code-admin/src/components/Editor/index.vue:225 | console.log('ClipboardEvent 粘贴事件对象', event); |
| console.log | old-code-admin/src/components/Reviewtask/chapterVideo.vue:180 | console.log('Video Audit - props.form:', JSON.stringify(newVal, null, 2)); |
| console.log | old-code-admin/src/components/Reviewtask/chapterVideo.vue:195 | console.log('Found courseChapterVideos at top level'); |
| console.log | old-code-admin/src/components/Reviewtask/chapterVideo.vue:201 | console.log('Found courseChapterVideos in businessDetail'); |
| console.log | old-code-admin/src/components/Reviewtask/chapterVideo.vue:209 | console.log('Found chapterVideoList in businessDetail'); |
| console.log | old-code-admin/src/components/Reviewtask/chapterVideo.vue:212 | console.log('Found videoList in businessDetail'); |
| console.log | old-code-admin/src/components/Reviewtask/chapterVideo.vue:218 | console.log('Found chapterVideoList in form'); |
| console.log | old-code-admin/src/components/Reviewtask/chapterVideo.vue:222 | console.log('Found videoList in form'); |
| console.log | old-code-admin/src/components/Reviewtask/chapterVideo.vue:228 | console.log('Video Audit - detailData:', JSON.stringify(detailData, null, 2)); |
| console.log | old-code-admin/src/components/Reviewtask/chapterVideo.vue:261 | console.log('Processing video data:', videoData); |
| console.log | old-code-admin/src/components/Reviewtask/chapterVideo.vue:280 | console.log('No video data found'); |
| window | old-code-admin/src/api/tournament/sceneNotice.js:6 | return window.btoa(unescape(encodeURIComponent(text))) |
| window | old-code-admin/src/api/tournament/sceneNotice.js:14 | return window.btoa(binary) |
| window | old-code-admin/src/components/Editor/index.back.vue:237 | const clipboard = e.clipboardData \\|\\| window.clipboardData |
| window | old-code-admin/src/components/Editor/index.vue:226 | const clipboard = event.clipboardData \\|\\| window.clipboardData; |
| window | old-code-admin/src/components/HeaderSearch/index.vue:87 | window.open(path.substr(pindex, path.length), "_blank") |
| window | old-code-admin/src/components/RuoYi/Doc/index.vue:11 | window.open(url.value) |
| window | old-code-admin/src/components/RuoYi/Git/index.vue:11 | window.open(url.value) |
| window | old-code-admin/src/components/SizeSelect/index.vue:35 | setTimeout("window.location.reload()", 1000) |
| window | old-code-admin/src/components/ThreeJs/Pointwave.vue:33 | const windowHalfX = ref(window.innerWidth / 2) |
| window | old-code-admin/src/components/ThreeJs/Pointwave.vue:51 | container.style.height = `${window.innerHeight - props.top}px` |

## 结论
- 小程序端现场证件、资源预约、评审秘书页面承担较多业务判断，应拆 API、状态和展示。
- 管理端/PC 端存在富文本渲染和全局下载能力暴露，需要统一安全封装。
- 未发现前端自动化测试目录，关键流程依赖人工回归。

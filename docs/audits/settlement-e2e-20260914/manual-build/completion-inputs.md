# 最终手册收尾输入

保留现有《操作手册-实测至资料保存（待收尾验证）.docx》。只有主执行者提供实际完成相应操作的截图并确认结果后，才生成另一个文件《获奖团队银行卡资料填报操作手册.docx》。不得把待实测说明或未经执行的画面写成成功证据。

## 需要的实际画面

- provided：提供成功区域，包含“收款资料已提供”、指定单位名称及下一步提示。
- tour：同一原始画面同时看见高亮的顶部“我已完成填报”按钮和“还差最后一步”引导气泡，尽量包括收起或关闭引导控件。
- completed：完成结果区域，包含“本队已完成填报”、办理人和完成时间，以及入口关闭后的页面状态。

原始桌面整屏截图放在上级screenshots目录。若提供成功与Tour无法在一屏同时看见，请分别拍摄；不拼接虚假一屏，不重绘或修补证据。最终显示区域在看过原图后确定，用Word的srcRect属性裁切，保留原始图片字节。显示区域优先避开账号栏和完整队伍标识。

## completion-manifest.json 格式

JSON含provided、tour、completed三个必需键，每项为包含filename、box、width的对象。filename为screenshots目录中的真实PNG文件名，box为[左,上,右,下]像素坐标，width为Word显示宽度（英寸）。该JSON必须待实际图片齐备后再创建，不放占位假图。

## 排版与检查

provided显示在第3步选单位与勾选说明后；tour和completed显示在第4步。宽度通常6.4英寸，收到图片后按实际纵横比调整，避免压缩得不可读或挤出空白页。不能把图片补拍要求误当作实际操作已完成。

最终命令使用bundled Python运行build_manual.py，指定--completion和另一个--output路径，不使用--review。生成后必须用已配置的bundled LibreOffice通过render_docx.py渲染，逐页打开全部PNG检查，再校验DOCX媒体字节与各原图SHA256一致。

每次生成会记录专属的“输出文件名-image-display-records.json”，并更新兼容的image-display-records.json。旧审阅稿专属图片记录和qa-status.json另行保留；最终QA记录不得覆盖审阅稿的历史结果。

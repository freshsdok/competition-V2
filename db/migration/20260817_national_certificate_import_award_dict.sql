-- 国赛团队证书新增“优胜奖”编码4。
-- 可重复执行；若环境中已存在同标签或同编码记录，则统一为当前口径。

UPDATE `sys_dict_data`
SET `dict_sort` = 4,
    `dict_label` = '优胜奖',
    `dict_value` = '4',
    `status` = '0',
    `update_by` = 'certificate_import',
    `update_time` = NOW(),
    `remark` = '证书导入奖项编码'
WHERE `dict_type` = 'awards_name'
  AND (`dict_label` = '优胜奖' OR `dict_value` = '4');

INSERT INTO `sys_dict_data` (
    `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`,
    `is_default`, `status`, `create_by`, `create_time`, `remark`
)
SELECT 4, '优胜奖', '4', 'awards_name', NULL, 'info', 'N', '0',
       'certificate_import', NOW(), '证书导入奖项编码'
WHERE NOT EXISTS (
    SELECT 1
    FROM `sys_dict_data`
    WHERE `dict_type` = 'awards_name'
      AND (`dict_label` = '优胜奖' OR `dict_value` = '4')
);

-- 通用评审模块第四包：填报提交日志与菜单权限。
-- Target database: MySQL 5.7+/8.0+, charset utf8mb4.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `review_object_submit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `activity_id` bigint NOT NULL COMMENT '评审活动ID',
  `object_id` bigint NOT NULL COMMENT '评审对象ID',
  `action_type` varchar(50) NOT NULL COMMENT '操作类型',
  `before_status` varchar(50) DEFAULT NULL COMMENT '操作前状态',
  `after_status` varchar(50) DEFAULT NULL COMMENT '操作后状态',
  `operator_user_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(100) DEFAULT NULL COMMENT '操作人姓名',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `action_reason` varchar(500) DEFAULT NULL COMMENT '操作原因或意见',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标识: 0正常/1删除',
  PRIMARY KEY (`id`),
  KEY `idx_review_submit_log_object` (`object_id`),
  KEY `idx_review_submit_log_activity` (`activity_id`, `action_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评审对象提交状态日志表';

-- 菜单：我的评审填报。
select @review_parent_id := menu_id
from sys_menu
where menu_name = '评审管理' and parent_id = 0 and platform_type = 'admin'
limit 1;

insert into sys_menu(menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '我的评审填报', @review_parent_id, 0, 'my-submission', 'review/my-submission/index', 'ReviewMySubmission', '1', '0', 'C', '0', '0', 'competition:review:submission:list', 'edit', 'system', 'admin', now()
where @review_parent_id is not null
  and not exists (
    select 1 from sys_menu where menu_name = '我的评审填报' and parent_id = @review_parent_id and platform_type = 'admin'
  );

select @review_submission_id := menu_id
from sys_menu
where menu_name = '我的评审填报' and parent_id = @review_parent_id and platform_type = 'admin'
limit 1;

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '填报任务查询', @review_submission_id, 1, '', '', '1', '0', 'F', '0', '0', 'competition:review:submission:query', '#', 'system', 'admin', now()
where @review_submission_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:submission:query' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '填报草稿保存', @review_submission_id, 2, '', '', '1', '0', 'F', '0', '0', 'competition:review:submission:edit', '#', 'system', 'admin', now()
where @review_submission_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:submission:edit' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '填报资料提交', @review_submission_id, 3, '', '', '1', '0', 'F', '0', '0', 'competition:review:submission:submit', '#', 'system', 'admin', now()
where @review_submission_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:submission:submit' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '填报撤回申请', @review_submission_id, 4, '', '', '1', '0', 'F', '0', '0', 'competition:review:submission:withdraw', '#', 'system', 'admin', now()
where @review_submission_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:submission:withdraw' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '撤回审批', @review_submission_id, 5, '', '', '1', '0', 'F', '0', '0', 'competition:review:submission:approve', '#', 'system', 'admin', now()
where @review_submission_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:submission:approve' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '关闭填报', @review_submission_id, 6, '', '', '1', '0', 'F', '0', '0', 'competition:review:submission:close', '#', 'system', 'admin', now()
where @review_submission_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:submission:close' and platform_type = 'admin');

-- 测试库/新库初始化时为超级管理员角色补齐评审模块菜单与按钮关系。
insert into sys_role_menu(role_id, menu_id)
select r.role_id, m.menu_id
from sys_role r
join sys_menu m on m.platform_type = 'admin'
left join sys_role_menu rm on rm.role_id = r.role_id and rm.menu_id = m.menu_id
where r.role_key = 'admin'
  and rm.menu_id is null
  and (
    m.perms like 'competition:review:%'
    or m.component like 'review/%'
    or (m.menu_name = '评审管理' and m.parent_id = 0)
  );

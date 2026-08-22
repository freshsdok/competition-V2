-- 评审模块评分表配置增强包：评分表配置菜单与按钮权限。
-- Target database: MySQL 5.7+/8.0+, charset utf8mb4.

SET NAMES utf8mb4;

select @review_parent_id := menu_id
from sys_menu
where menu_name = '评审管理' and parent_id = 0 and platform_type = 'admin'
limit 1;

insert into sys_menu(menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评分表配置', @review_parent_id, 8, 'rule', 'review/rule/index', 'ReviewRule', '1', '0', 'C', '0', '0', 'competition:review:rule:list', 'edit', 'system', 'admin', now()
where @review_parent_id is not null
  and not exists (
    select 1 from sys_menu where menu_name = '评分表配置' and parent_id = @review_parent_id and platform_type = 'admin'
  );

select @review_rule_id := menu_id
from sys_menu
where menu_name = '评分表配置' and parent_id = @review_parent_id and platform_type = 'admin'
limit 1;

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评分规则查询', @review_rule_id, 1, '', '', '1', '0', 'F', '0', '0', 'competition:review:rule:query', '#', 'system', 'admin', now()
where @review_rule_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:rule:query' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评分规则新增', @review_rule_id, 2, '', '', '1', '0', 'F', '0', '0', 'competition:review:rule:add', '#', 'system', 'admin', now()
where @review_rule_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:rule:add' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评分规则编辑', @review_rule_id, 3, '', '', '1', '0', 'F', '0', '0', 'competition:review:rule:edit', '#', 'system', 'admin', now()
where @review_rule_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:rule:edit' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评分规则删除', @review_rule_id, 4, '', '', '1', '0', 'F', '0', '0', 'competition:review:rule:remove', '#', 'system', 'admin', now()
where @review_rule_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:rule:remove' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评分指标列表', @review_rule_id, 5, '', '', '1', '0', 'F', '0', '0', 'competition:review:criteria:list', '#', 'system', 'admin', now()
where @review_rule_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:criteria:list' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评分指标查询', @review_rule_id, 6, '', '', '1', '0', 'F', '0', '0', 'competition:review:criteria:query', '#', 'system', 'admin', now()
where @review_rule_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:criteria:query' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评分指标新增', @review_rule_id, 7, '', '', '1', '0', 'F', '0', '0', 'competition:review:criteria:add', '#', 'system', 'admin', now()
where @review_rule_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:criteria:add' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评分指标编辑', @review_rule_id, 8, '', '', '1', '0', 'F', '0', '0', 'competition:review:criteria:edit', '#', 'system', 'admin', now()
where @review_rule_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:criteria:edit' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评分指标删除', @review_rule_id, 9, '', '', '1', '0', 'F', '0', '0', 'competition:review:criteria:remove', '#', 'system', 'admin', now()
where @review_rule_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:criteria:remove' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审轮次编辑', @review_rule_id, 10, '', '', '1', '0', 'F', '0', '0', 'competition:review:round:edit', '#', 'system', 'admin', now()
where @review_rule_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:round:edit' and platform_type = 'admin');

insert into sys_role_menu(role_id, menu_id)
select r.role_id, m.menu_id
from sys_role r
join sys_menu m on m.platform_type = 'admin'
left join sys_role_menu rm on rm.role_id = r.role_id and rm.menu_id = m.menu_id
where r.role_key = 'admin'
  and rm.menu_id is null
  and (
    m.perms in (
      'competition:review:rule:list',
      'competition:review:rule:query',
      'competition:review:rule:add',
      'competition:review:rule:edit',
      'competition:review:rule:remove',
      'competition:review:criteria:list',
      'competition:review:criteria:query',
      'competition:review:criteria:add',
      'competition:review:criteria:edit',
      'competition:review:criteria:remove',
      'competition:review:round:edit'
    )
    or m.component = 'review/rule/index'
  );

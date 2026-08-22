-- 通用评审模块：评审任务分配管理菜单与按钮权限。
-- 说明：本脚本只初始化菜单权限，不修改业务数据。

set @review_parent_id := (
    select menu_id from sys_menu
    where menu_name = '评审管理' and parent_id = 0 and platform_type = 'admin'
    limit 1
);

insert into sys_menu(menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审任务分配', @review_parent_id, 6, 'assignment', 'review/assignment/index', 'ReviewAssignment', '1', '0', 'C', '0', '0', 'competition:review:assignment:list', 'peoples', 'system', 'admin', now()
where @review_parent_id is not null
  and not exists (
    select 1 from sys_menu
    where menu_name = '评审任务分配' and parent_id = @review_parent_id and platform_type = 'admin'
  );

set @review_assignment_id := (
    select menu_id from sys_menu
    where menu_name = '评审任务分配' and parent_id = @review_parent_id and platform_type = 'admin'
    limit 1
);

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审任务查询', @review_assignment_id, 1, '', '', '1', '0', 'F', '0', '0', 'competition:review:assignment:query', '#', 'system', 'admin', now()
where @review_assignment_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:assignment:query' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审任务新增', @review_assignment_id, 2, '', '', '1', '0', 'F', '0', '0', 'competition:review:assignment:add', '#', 'system', 'admin', now()
where @review_assignment_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:assignment:add' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审任务编辑', @review_assignment_id, 3, '', '', '1', '0', 'F', '0', '0', 'competition:review:assignment:edit', '#', 'system', 'admin', now()
where @review_assignment_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:assignment:edit' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审任务删除', @review_assignment_id, 4, '', '', '1', '0', 'F', '0', '0', 'competition:review:assignment:remove', '#', 'system', 'admin', now()
where @review_assignment_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:assignment:remove' and platform_type = 'admin');

insert into sys_role_menu(role_id, menu_id)
select r.role_id, m.menu_id
from sys_role r
join sys_menu m on m.platform_type = 'admin'
left join sys_role_menu rm on rm.role_id = r.role_id and rm.menu_id = m.menu_id
where r.role_key = 'admin'
  and rm.menu_id is null
  and (
    m.menu_id = @review_assignment_id
    or m.perms in (
      'competition:review:assignment:list',
      'competition:review:assignment:query',
      'competition:review:assignment:add',
      'competition:review:assignment:edit',
      'competition:review:assignment:remove'
    )
  );

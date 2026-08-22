-- 通用评审模块第六包：评审秘书移动端现场控制台菜单权限

set @review_parent_id := (
    select menu_id from sys_menu
    where menu_name = '评审管理' and platform_type = 'admin'
      and path = 'review'
    order by menu_id limit 1
);

set @review_session_id := (
    select menu_id from sys_menu
    where menu_name = '现场场次' and parent_id = @review_parent_id and platform_type = 'admin'
    order by menu_id limit 1
);

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '秘书控制台查询', @review_session_id, 10, '', '', '1', '0', 'F', '0', '0', 'competition:review:secretary:query', '#', 'system', 'admin', now()
where @review_session_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:secretary:query' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '秘书控制台操作', @review_session_id, 11, '', '', '1', '0', 'F', '0', '0', 'competition:review:secretary:edit', '#', 'system', 'admin', now()
where @review_session_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:secretary:edit' and platform_type = 'admin');

insert into sys_role_menu(role_id, menu_id)
select r.role_id, m.menu_id
from sys_role r
join sys_menu m on m.platform_type = 'admin'
left join sys_role_menu rm on rm.role_id = r.role_id and rm.menu_id = m.menu_id
where r.role_key = 'admin'
  and rm.menu_id is null
  and m.perms in (
      'competition:review:secretary:query',
      'competition:review:secretary:edit'
  );

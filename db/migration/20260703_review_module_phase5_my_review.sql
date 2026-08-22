-- 通用评审模块第五包：专家PC端我的评审任务菜单与按钮权限。
-- Target database: MySQL 5.7+/8.0+, charset utf8mb4.

SET NAMES utf8mb4;

select @review_parent_id := menu_id
from sys_menu
where menu_name = '评审管理' and parent_id = 0 and platform_type = 'admin'
limit 1;

insert into sys_menu(menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '我的评审任务', @review_parent_id, 5, 'my-review', 'review/my-review/index', 'ReviewMyReview', '1', '0', 'C', '0', '0', 'competition:review:my-review:list', 'education', 'system', 'admin', now()
where @review_parent_id is not null
  and not exists (
    select 1 from sys_menu where menu_name = '我的评审任务' and parent_id = @review_parent_id and platform_type = 'admin'
  );

select @review_my_review_id := menu_id
from sys_menu
where menu_name = '我的评审任务' and parent_id = @review_parent_id and platform_type = 'admin'
limit 1;

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审任务查询', @review_my_review_id, 1, '', '', '1', '0', 'F', '0', '0', 'competition:review:my-review:query', '#', 'system', 'admin', now()
where @review_my_review_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:my-review:query' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评分草稿保存', @review_my_review_id, 2, '', '', '1', '0', 'F', '0', '0', 'competition:review:my-review:edit', '#', 'system', 'admin', now()
where @review_my_review_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:my-review:edit' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评分提交', @review_my_review_id, 3, '', '', '1', '0', 'F', '0', '0', 'competition:review:my-review:submit', '#', 'system', 'admin', now()
where @review_my_review_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:my-review:submit' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '现场当前对象查询', @review_my_review_id, 4, '', '', '1', '0', 'F', '0', '0', 'competition:review:session:query', '#', 'system', 'admin', now()
where @review_my_review_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:session:query' and platform_type = 'admin');

-- 测试库/新库初始化时为超级管理员角色补齐第五包菜单与按钮关系。
insert into sys_role_menu(role_id, menu_id)
select r.role_id, m.menu_id
from sys_role r
join sys_menu m on m.platform_type = 'admin'
left join sys_role_menu rm on rm.role_id = r.role_id and rm.menu_id = m.menu_id
where r.role_key = 'admin'
  and rm.menu_id is null
  and (
    m.perms in (
      'competition:review:my-review:list',
      'competition:review:my-review:query',
      'competition:review:my-review:edit',
      'competition:review:my-review:submit',
      'competition:review:session:query'
    )
    or m.component = 'review/my-review/index'
  );

-- 通用评审模块第三包：管理端菜单初始化脚本
-- 说明：脚本按菜单名称和父级做幂等插入，不依赖固定 menu_id。

set @review_parent_id := (
    select menu_id from sys_menu
    where menu_name = '评审管理' and parent_id = 0 and platform_type = 'admin'
    limit 1
);

insert into sys_menu(menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审管理', 0, 70, 'review', 'Layout', 'Review', '1', '0', 'M', '0', '0', '', 'list', 'system', 'admin', now()
where @review_parent_id is null;

set @review_parent_id := (
    select menu_id from sys_menu
    where menu_name = '评审管理' and parent_id = 0 and platform_type = 'admin'
    limit 1
);

insert into sys_menu(menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审活动', @review_parent_id, 1, 'activity', 'review/activity/index', 'ReviewActivity', '1', '0', 'C', '0', '0', 'competition:review:activity:list', 'form', 'system', 'admin', now()
where not exists (
    select 1 from sys_menu where menu_name = '评审活动' and parent_id = @review_parent_id and platform_type = 'admin'
);

insert into sys_menu(menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审对象', @review_parent_id, 2, 'object', 'review/object/index', 'ReviewObject', '1', '0', 'C', '0', '0', 'competition:review:object:list', 'list', 'system', 'admin', now()
where not exists (
    select 1 from sys_menu where menu_name = '评审对象' and parent_id = @review_parent_id and platform_type = 'admin'
);

insert into sys_menu(menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '导入评审对象', @review_parent_id, 3, 'import', 'review/import/index', 'ReviewObjectImport', '1', '1', 'C', '0', '0', 'competition:review:object:import', 'upload', 'system', 'admin', now()
where not exists (
    select 1 from sys_menu where menu_name = '导入评审对象' and parent_id = @review_parent_id and platform_type = 'admin'
);

insert into sys_menu(menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '现场场次', @review_parent_id, 4, 'session', 'review/session/index', 'ReviewSession', '1', '1', 'C', '0', '0', 'competition:review:session:list', 'date', 'system', 'admin', now()
where not exists (
    select 1 from sys_menu where menu_name = '现场场次' and parent_id = @review_parent_id and platform_type = 'admin'
);

set @review_activity_id := (
    select menu_id from sys_menu where menu_name = '评审活动' and parent_id = @review_parent_id and platform_type = 'admin' limit 1
);
set @review_object_id := (
    select menu_id from sys_menu where menu_name = '评审对象' and parent_id = @review_parent_id and platform_type = 'admin' limit 1
);
set @review_import_id := (
    select menu_id from sys_menu where menu_name = '导入评审对象' and parent_id = @review_parent_id and platform_type = 'admin' limit 1
);

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审活动查询', @review_activity_id, 1, '', '', '1', '0', 'F', '0', '0', 'competition:review:activity:query', '#', 'system', 'admin', now()
where not exists (select 1 from sys_menu where parent_id = @review_activity_id and perms = 'competition:review:activity:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审活动新增', @review_activity_id, 2, '', '', '1', '0', 'F', '0', '0', 'competition:review:activity:add', '#', 'system', 'admin', now()
where not exists (select 1 from sys_menu where parent_id = @review_activity_id and perms = 'competition:review:activity:add');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审活动编辑', @review_activity_id, 3, '', '', '1', '0', 'F', '0', '0', 'competition:review:activity:edit', '#', 'system', 'admin', now()
where not exists (select 1 from sys_menu where parent_id = @review_activity_id and perms = 'competition:review:activity:edit');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审对象查询', @review_object_id, 1, '', '', '1', '0', 'F', '0', '0', 'competition:review:object:query', '#', 'system', 'admin', now()
where not exists (select 1 from sys_menu where parent_id = @review_object_id and perms = 'competition:review:object:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审对象导入', @review_import_id, 1, '', '', '1', '0', 'F', '0', '0', 'competition:review:object:import', '#', 'system', 'admin', now()
where not exists (select 1 from sys_menu where parent_id = @review_import_id and perms = 'competition:review:object:import');

-- 通用评审模块第七包：评审结果菜单与按钮权限。
-- Target database: MySQL 5.7+/8.0+, charset utf8mb4.

SET NAMES utf8mb4;

-- 兼容早期测试库中已有同名 review_record 表但缺少通用评审模块字段的情况。
-- 只补字段，不删除旧字段，避免影响历史样例数据。
set @column_exists := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'review_record' and column_name = 'activity_id');
set @ddl := if(@column_exists = 0, 'alter table review_record add column activity_id bigint null comment ''评审活动ID'' after id', 'select 1');
prepare stmt from @ddl; execute stmt; deallocate prepare stmt;

set @column_exists := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'review_record' and column_name = 'round_id');
set @ddl := if(@column_exists = 0, 'alter table review_record add column round_id bigint null comment ''评审轮次ID'' after activity_id', 'select 1');
prepare stmt from @ddl; execute stmt; deallocate prepare stmt;

set @column_exists := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'review_record' and column_name = 'object_id');
set @ddl := if(@column_exists = 0, 'alter table review_record add column object_id bigint null comment ''评审对象ID'' after round_id', 'select 1');
prepare stmt from @ddl; execute stmt; deallocate prepare stmt;

set @column_exists := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'review_record' and column_name = 'assignment_id');
set @ddl := if(@column_exists = 0, 'alter table review_record add column assignment_id bigint null comment ''评审任务ID'' after object_id', 'select 1');
prepare stmt from @ddl; execute stmt; deallocate prepare stmt;

set @column_exists := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'review_record' and column_name = 'reviewer_id');
set @ddl := if(@column_exists = 0, 'alter table review_record add column reviewer_id bigint null comment ''评审人ID'' after assignment_id', 'select 1');
prepare stmt from @ddl; execute stmt; deallocate prepare stmt;

set @column_exists := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'review_record' and column_name = 'reviewer_user_id');
set @ddl := if(@column_exists = 0, 'alter table review_record add column reviewer_user_id bigint null comment ''评审人用户ID'' after reviewer_id', 'select 1');
prepare stmt from @ddl; execute stmt; deallocate prepare stmt;

set @column_exists := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'review_record' and column_name = 'record_status');
set @ddl := if(@column_exists = 0, 'alter table review_record add column record_status varchar(50) null comment ''评审记录状态'' after reviewer_user_id', 'select 1');
prepare stmt from @ddl; execute stmt; deallocate prepare stmt;

set @column_exists := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'review_record' and column_name = 'total_score');
set @ddl := if(@column_exists = 0, 'alter table review_record add column total_score decimal(10,2) null comment ''总分'' after record_status', 'select 1');
prepare stmt from @ddl; execute stmt; deallocate prepare stmt;

set @column_exists := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'review_record' and column_name = 'grade');
set @ddl := if(@column_exists = 0, 'alter table review_record add column grade varchar(50) null comment ''等级'' after total_score', 'select 1');
prepare stmt from @ddl; execute stmt; deallocate prepare stmt;

set @column_exists := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'review_record' and column_name = 'recommendation');
set @ddl := if(@column_exists = 0, 'alter table review_record add column recommendation varchar(100) null comment ''推荐意见'' after grade', 'select 1');
prepare stmt from @ddl; execute stmt; deallocate prepare stmt;

set @column_exists := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'review_record' and column_name = 'comment_text');
set @ddl := if(@column_exists = 0, 'alter table review_record add column comment_text text null comment ''评审意见'' after recommendation', 'select 1');
prepare stmt from @ddl; execute stmt; deallocate prepare stmt;

set @column_exists := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'review_record' and column_name = 'submitted_time');
set @ddl := if(@column_exists = 0, 'alter table review_record add column submitted_time datetime null comment ''提交时间'' after comment_text', 'select 1');
prepare stmt from @ddl; execute stmt; deallocate prepare stmt;

set @column_exists := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'review_record' and column_name = 'returned_time');
set @ddl := if(@column_exists = 0, 'alter table review_record add column returned_time datetime null comment ''退回时间'' after submitted_time', 'select 1');
prepare stmt from @ddl; execute stmt; deallocate prepare stmt;

set @column_exists := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'review_record' and column_name = 'locked_time');
set @ddl := if(@column_exists = 0, 'alter table review_record add column locked_time datetime null comment ''锁定时间'' after returned_time', 'select 1');
prepare stmt from @ddl; execute stmt; deallocate prepare stmt;

set @column_exists := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'review_record' and column_name = 'invalid_time');
set @ddl := if(@column_exists = 0, 'alter table review_record add column invalid_time datetime null comment ''作废时间'' after locked_time', 'select 1');
prepare stmt from @ddl; execute stmt; deallocate prepare stmt;

set @column_exists := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'review_record' and column_name = 'invalid_reason');
set @ddl := if(@column_exists = 0, 'alter table review_record add column invalid_reason varchar(500) null comment ''作废原因'' after invalid_time', 'select 1');
prepare stmt from @ddl; execute stmt; deallocate prepare stmt;

set @column_exists := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'review_record' and column_name = 'remark');
set @ddl := if(@column_exists = 0, 'alter table review_record add column remark varchar(500) null comment ''备注'' after invalid_reason', 'select 1');
prepare stmt from @ddl; execute stmt; deallocate prepare stmt;

select @review_parent_id := menu_id
from sys_menu
where menu_name = '评审管理' and parent_id = 0 and platform_type = 'admin'
limit 1;

insert into sys_menu(menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审结果', @review_parent_id, 7, 'result', 'review/result/index', 'ReviewResult', '1', '0', 'C', '0', '0', 'competition:review:result:list', 'medal', 'system', 'admin', now()
where @review_parent_id is not null
  and not exists (
    select 1 from sys_menu where menu_name = '评审结果' and parent_id = @review_parent_id and platform_type = 'admin'
  );

select @review_result_id := menu_id
from sys_menu
where menu_name = '评审结果' and parent_id = @review_parent_id and platform_type = 'admin'
limit 1;

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审结果查询', @review_result_id, 1, '', '', '1', '0', 'F', '0', '0', 'competition:review:result:query', '#', 'system', 'admin', now()
where @review_result_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:result:query' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审结果生成', @review_result_id, 2, '', '', '1', '0', 'F', '0', '0', 'competition:review:result:generate', '#', 'system', 'admin', now()
where @review_result_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:result:generate' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评价结论填写', @review_result_id, 3, '', '', '1', '0', 'F', '0', '0', 'competition:review:result:edit', '#', 'system', 'admin', now()
where @review_result_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:result:edit' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审结果发布', @review_result_id, 4, '', '', '1', '0', 'F', '0', '0', 'competition:review:result:publish', '#', 'system', 'admin', now()
where @review_result_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:result:publish' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审结果撤回', @review_result_id, 5, '', '', '1', '0', 'F', '0', '0', 'competition:review:result:revoke', '#', 'system', 'admin', now()
where @review_result_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:result:revoke' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评分记录查看', @review_result_id, 6, '', '', '1', '0', 'F', '0', '0', 'competition:review:result:record', '#', 'system', 'admin', now()
where @review_result_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:result:record' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审轮次列表', @review_result_id, 7, '', '', '1', '0', 'F', '0', '0', 'competition:review:round:list', '#', 'system', 'admin', now()
where @review_result_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:round:list' and platform_type = 'admin');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, platform_type, create_time)
select '评审轮次查询', @review_result_id, 8, '', '', '1', '0', 'F', '0', '0', 'competition:review:round:query', '#', 'system', 'admin', now()
where @review_result_id is not null
  and not exists (select 1 from sys_menu where perms = 'competition:review:round:query' and platform_type = 'admin');

insert into sys_role_menu(role_id, menu_id)
select r.role_id, m.menu_id
from sys_role r
join sys_menu m on m.platform_type = 'admin'
left join sys_role_menu rm on rm.role_id = r.role_id and rm.menu_id = m.menu_id
where r.role_key = 'admin'
  and rm.menu_id is null
  and (
    m.perms in (
      'competition:review:result:list',
      'competition:review:result:query',
      'competition:review:result:generate',
      'competition:review:result:edit',
      'competition:review:result:publish',
      'competition:review:result:revoke',
      'competition:review:result:record',
      'competition:review:round:list',
      'competition:review:round:query'
    )
    or m.component = 'review/result/index'
  );

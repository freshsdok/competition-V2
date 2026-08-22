# 大赛现场设备资源管理与预约 - 第二阶段菜单 SQL 说明

SQL 文件：

- `db/migration/20260701_competition_scene_resource_phase2_menu.sql`

## 一、父菜单确认

测试库查询结果：

| menu_id | menu_name | parent_id | path | component | menu_type | perms |
| --- | --- | --- | --- | --- | --- | --- |
| 2002 | 赛事管理 | 0 | tournament | NULL | M | NULL |
| 2437 | 现场安排配置 | 2002 | sceneSchedule | tournament/sceneSchedule/index | C | competition:sceneSchedule:list |

因此资源管理菜单建议挂载到现有“赛事管理”目录下，与“现场安排配置”同级。

## 二、不写死父菜单 ID

SQL 中使用运行时查询：

```sql
SET @parent_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE menu_name = '赛事管理'
      AND path = 'tournament'
      AND menu_type = 'M'
      AND platform_type = 'admin'
    ORDER BY menu_id
    LIMIT 1
);
```

未硬编码 `2002`。

## 三、新增菜单

- 菜单名称：资源管理
- 路径：`sceneResource`
- 组件：`tournament/sceneResource/index`
- 平台：`admin`
- 权限：`competition:sceneResource:list`
- 排序：`81`

## 四、新增按钮权限

- `competition:sceneResource:list`
- `competition:sceneResource:query`
- `competition:sceneResource:add`
- `competition:sceneResource:edit`
- `competition:sceneResource:remove`
- `competition:sceneResource:changeStatus`

## 五、安全性

SQL 使用 `NOT EXISTS` 避免重复插入：

- 已存在同路径/同组件菜单时不重复新增菜单；
- 已存在相同权限码时不重复新增按钮；
- 父菜单不存在时不插入任何记录。

## 六、执行建议

第二阶段前端页面联调前，在测试数据库执行该 SQL。  
如正式库菜单结构与测试库不同，请先确认“赛事管理”目录是否满足：

- `menu_name = '赛事管理'`
- `path = 'tournament'`
- `menu_type = 'M'`
- `platform_type = 'admin'`

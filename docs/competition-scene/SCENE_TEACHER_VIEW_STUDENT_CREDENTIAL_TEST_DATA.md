# 教师查看自己指导学生参赛证测试数据记录

生成时间：2026-07-07 23:55

## 1. 数据准备结论

本轮未能选取或准备真实测试库数据。

原因：

- 测试库候选地址 `10.10.10.10:3306` TCP 连接超时；
- 本机 `127.0.0.1:3306` 未监听；
- 未提供其他可达测试库地址；
- 未提供教师 A、教师 B、普通学生测试账号；
- 未连接生产数据库。

## 2. 隐私与安全处理

本轮未读取到真实业务数据，因此报告中不包含：

- 身份证号；
- `openId`;
- `unionId`;
- `credentialToken`;
- 手机号；
- `qrContent` 明文。

## 3. 需要的测试数据清单

待测试库可达后，应选取或准备以下数据：

| 角色/对象 | 数据要求 | 当前状态 |
| --- | --- | --- |
| 教师 A | 至少指导团队 1 | 未选取 |
| 团队 1 | 至少两个学生 | 未选取 |
| 学生 1 | 已生成参赛证 | 未选取 |
| 学生 2 | 未生成参赛证 | 未选取 |
| 教师 B | 指导团队 2 | 未选取 |
| 团队 2 | 至少一个已生成参赛证学生 | 未选取 |
| 普通学生账号 | 不具备教师授权关系 | 未选取 |
| 团队级证件 | 如测试库存在则选取 | 未选取 |
| 个人级证件 | 如测试库存在则选取 | 未选取 |

## 4. 建议的数据核验 SQL 口径

测试库可达后，建议只输出脱敏统计，不在报告中记录敏感字段明文。

### 4.1 教师授权团队候选

```sql
select
  competition_series_id,
  team_code,
  team_name,
  leader_teacher_id,
  count(*) as member_count
from competition_apply_info
where del_flag = '0'
  and pay_status = 'paid'
  and (check_status = '4' or check_status is null or check_status = '')
  and team_code is not null
group by competition_series_id, team_code, team_name, leader_teacher_id
having leader_teacher_id is not null
order by member_count desc
limit 20;
```

### 4.2 指导教师报名行候选

```sql
select
  competition_series_id,
  team_code,
  team_name,
  user_id as teacher_user_id,
  competition_role_name
from competition_apply_info
where del_flag = '0'
  and competition_role_name in ('指导教师', '指导老师')
  and user_id is not null
order by create_time desc
limit 20;
```

### 4.3 有证学生候选

```sql
select
  m.competition_series_id,
  m.team_code,
  m.member_id,
  m.user_id,
  m.user_name,
  c.credential_id,
  c.credential_no,
  c.credential_status
from competition_apply_info m
join competition_scene_credential c
  on c.del_flag = '0'
 and c.competition_series_id = m.competition_series_id
 and (
   (c.member_id is not null and c.member_id = m.member_id)
   or (c.user_id is not null and c.user_id = m.user_id)
 )
where m.del_flag = '0'
  and m.pay_status = 'paid'
  and (m.check_status = '4' or m.check_status is null or m.check_status = '')
  and (m.competition_role_name is null or m.competition_role_name not in ('指导教师', '指导老师'))
limit 20;
```

### 4.4 无证学生候选

```sql
select
  m.competition_series_id,
  m.team_code,
  m.member_id,
  m.user_id,
  m.user_name
from competition_apply_info m
left join competition_scene_credential c
  on c.del_flag = '0'
 and c.competition_series_id = m.competition_series_id
 and (
   (c.member_id is not null and c.member_id = m.member_id)
   or (c.user_id is not null and c.user_id = m.user_id)
 )
where m.del_flag = '0'
  and m.pay_status = 'paid'
  and (m.check_status = '4' or m.check_status is null or m.check_status = '')
  and (m.competition_role_name is null or m.competition_role_name not in ('指导教师', '指导老师'))
  and c.credential_id is null
limit 20;
```

## 5. 后续记录要求

后续真实联调报告中只记录：

- 测试账号标识可用别名代替；
- `userId` / `teamCode` / `credentialId` 可记录；
- 证件二维码只记录“存在/不存在”，不记录 `qrContent` 明文；
- 不记录身份证号、`openId`、`unionId`、`credentialToken`；
- 手机号如确需说明，只记录脱敏值。

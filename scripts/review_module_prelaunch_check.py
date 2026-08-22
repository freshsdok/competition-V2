#!/usr/bin/env python3
"""Pre-launch data checks for the review module.

The script is read-only. Connection defaults target the local jiaoxue_test
database used during integration testing and can be overridden with:

  REVIEW_DB_HOST, REVIEW_DB_PORT, REVIEW_DB_NAME, REVIEW_DB_USER,
  REVIEW_DB_PASSWORD, REVIEW_CHECK_OUTPUT
"""

from __future__ import annotations

import datetime as _dt
import os
import sys
from typing import Any, Dict, Iterable, List, Tuple

try:
    import pymysql
except ImportError:
    print("PyMySQL is required. Install with: python -m pip install PyMySQL", file=sys.stderr)
    sys.exit(2)


REQUIRED_TABLES = [
    "review_activity",
    "review_round",
    "review_rule",
    "review_criteria",
    "review_object",
    "review_object_member",
    "review_object_material",
    "review_submission_permission",
    "review_object_external_ref",
    "review_object_certificate_ref",
    "reviewer_profile",
    "review_activity_user_role",
    "review_panel",
    "review_panel_member",
    "review_assignment",
    "review_record",
    "review_score_detail",
    "review_session",
    "review_session_object",
    "review_session_event_log",
    "review_result",
    "review_result_publish_log",
    "review_audit_log",
    "review_object_submit_log",
]

REVIEW_RECORD_REQUIRED_COLUMNS = [
    "activity_id",
    "round_id",
    "object_id",
    "assignment_id",
    "reviewer_user_id",
    "record_status",
    "total_score",
]

REQUIRED_MENU_PERMISSIONS = [
    "competition:review:activity:list",
    "competition:review:object:list",
    "competition:review:object:import",
    "competition:review:submission:list",
    "competition:review:submission:query",
    "competition:review:submission:edit",
    "competition:review:submission:submit",
    "competition:review:submission:withdraw",
    "competition:review:submission:approve",
    "competition:review:submission:close",
    "competition:review:my-review:list",
    "competition:review:my-review:query",
    "competition:review:my-review:edit",
    "competition:review:my-review:submit",
    "competition:review:session:list",
    "competition:review:session:query",
    "competition:review:secretary:query",
    "competition:review:secretary:edit",
    "competition:review:result:list",
    "competition:review:result:query",
    "competition:review:result:generate",
    "competition:review:result:edit",
    "competition:review:result:publish",
    "competition:review:result:revoke",
    "competition:review:result:record",
]


def config() -> Dict[str, Any]:
    return {
        "host": os.getenv("REVIEW_DB_HOST", "localhost"),
        "port": int(os.getenv("REVIEW_DB_PORT", "3306")),
        "database": os.getenv("REVIEW_DB_NAME", "jiaoxue_test"),
        "user": os.getenv("REVIEW_DB_USER", "dev"),
        "password": os.getenv("REVIEW_DB_PASSWORD", "dev_mysql_2026"),
        "charset": "utf8mb4",
        "cursorclass": pymysql.cursors.DictCursor,
    }


def fetch_all(cur: Any, sql: str) -> List[Dict[str, Any]]:
    cur.execute(sql)
    return list(cur.fetchall())


def count_rows(rows: Iterable[Dict[str, Any]]) -> int:
    return len(list(rows))


def format_rows(rows: List[Dict[str, Any]], limit: int = 20) -> str:
    if not rows:
        return ""
    headers = list(rows[0].keys())
    lines = [
        "| " + " | ".join(headers) + " |",
        "| " + " | ".join(["---"] * len(headers)) + " |",
    ]
    for row in rows[:limit]:
        values = []
        for key in headers:
            value = row.get(key)
            values.append("" if value is None else str(value).replace("\n", " "))
        lines.append("| " + " | ".join(values) + " |")
    if len(rows) > limit:
        lines.append(f"\n仅展示前 {limit} 条，共 {len(rows)} 条。")
    return "\n".join(lines)


def run_query_check(cur: Any, check_id: str, title: str, severity: str, sql: str) -> Dict[str, Any]:
    rows = fetch_all(cur, sql)
    return {
        "id": check_id,
        "title": title,
        "severity": severity,
        "status": "PASS" if not rows else severity,
        "rows": rows,
    }


def permission_union_sql(permissions: Iterable[str]) -> str:
    return "\nunion all\n".join(f"select '{permission}' as perm" for permission in permissions)


def main() -> int:
    cfg = config()
    output = os.getenv("REVIEW_CHECK_OUTPUT", "review_module_prelaunch_data_check_report.md")
    results: List[Dict[str, Any]] = []

    conn = pymysql.connect(**cfg)
    try:
        with conn.cursor() as cur:
            table_rows = fetch_all(
                cur,
                """
                select table_name
                from information_schema.tables
                where table_schema = database()
                """,
            )
            existing_tables = {row["TABLE_NAME"] for row in table_rows}
            missing_tables = [{"table_name": table} for table in REQUIRED_TABLES if table not in existing_tables]
            results.append({
                "id": "TABLES",
                "title": "评审模块核心表完整性",
                "severity": "FAIL",
                "status": "PASS" if not missing_tables else "FAIL",
                "rows": missing_tables,
            })

            column_rows = fetch_all(
                cur,
                """
                select column_name
                from information_schema.columns
                where table_schema = database()
                  and table_name = 'review_record'
                """,
            )
            existing_columns = {row["COLUMN_NAME"] for row in column_rows}
            missing_columns = [
                {"table_name": "review_record", "column_name": column}
                for column in REVIEW_RECORD_REQUIRED_COLUMNS
                if column not in existing_columns
            ]
            results.append({
                "id": "RECORD_COLUMNS",
                "title": "review_record 评审模块字段完整性",
                "severity": "FAIL",
                "status": "PASS" if not missing_columns else "FAIL",
                "rows": missing_columns,
            })

            permission_union = permission_union_sql(REQUIRED_MENU_PERMISSIONS)
            missing_menu_permissions = fetch_all(
                cur,
                f"""
                select p.perm
                from (
                    {permission_union}
                ) p
                left join sys_menu m on m.perms = p.perm and m.status = '0'
                where m.menu_id is null
                """,
            )
            results.append({
                "id": "MENU_PERMISSIONS",
                "title": "评审模块菜单和按钮权限完整性",
                "severity": "FAIL",
                "status": "PASS" if not missing_menu_permissions else "FAIL",
                "rows": missing_menu_permissions,
            })

            checks: List[Tuple[str, str, str, str]] = [
                (
                    "CERT_DUP",
                    "同一活动下同一有效参赛证映射多个评审对象",
                    "WARN",
                    """
                    select activity_id, certificate_code, count(distinct object_id) as object_count
                    from review_object_certificate_ref
                    where del_flag = '0' and valid_status = 'VALID'
                    group by activity_id, certificate_code
                    having count(distinct object_id) > 1
                    """,
                ),
                (
                    "PERMISSION_USER",
                    "有效填报权限缺少 user_id",
                    "FAIL",
                    """
                    select id, activity_id, object_id, permission_type, status
                    from review_submission_permission
                    where del_flag = '0' and status = 'ACTIVE' and user_id is null
                    """,
                ),
                (
                    "ASSIGN_OBJECT_STATUS",
                    "未锁定或已作废对象仍存在有效评审任务",
                    "WARN",
                    """
                    select a.id as assignment_id, a.activity_id, a.round_id, a.object_id,
                           a.status as assignment_status, o.submit_status
                    from review_assignment a
                    join review_object o on o.id = a.object_id and o.del_flag = '0'
                    where a.del_flag = '0'
                      and coalesce(a.status, '') <> 'CANCELLED'
                      and coalesce(o.submit_status, '') not in ('LOCKED', 'REVIEWING', 'REVIEWED')
                    """,
                ),
                (
                    "SUBMITTED_ASSIGNMENT_RECORD",
                    "已提交任务缺少已提交评分记录",
                    "FAIL",
                    """
                    select a.id as assignment_id, a.activity_id, a.round_id, a.object_id, a.reviewer_user_id
                    from review_assignment a
                    where a.del_flag = '0' and a.status = 'SUBMITTED'
                      and not exists (
                          select 1
                          from review_record r
                          where r.del_flag = '0'
                            and r.assignment_id = a.id
                            and r.record_status = 'SUBMITTED'
                      )
                    """,
                ),
                (
                    "SUBMITTED_RECORD_ASSIGNMENT",
                    "已提交评分记录对应任务未提交",
                    "WARN",
                    """
                    select r.id as record_id, r.assignment_id, r.activity_id, r.round_id,
                           r.object_id, r.reviewer_user_id, a.status as assignment_status
                    from review_record r
                    left join review_assignment a on a.id = r.assignment_id and a.del_flag = '0'
                    where r.del_flag = '0' and r.record_status = 'SUBMITTED'
                      and coalesce(a.status, '') <> 'SUBMITTED'
                    """,
                ),
                (
                    "SCORE_SNAPSHOT",
                    "已提交评分明细缺少指标快照字段",
                    "FAIL",
                    """
                    select d.id as detail_id, d.record_id, d.criteria_id, d.criteria_name, d.score_type
                    from review_score_detail d
                    join review_record r on r.id = d.record_id and r.del_flag = '0'
                    where d.del_flag = '0'
                      and r.record_status = 'SUBMITTED'
                      and (d.criteria_name is null or d.criteria_name = '' or d.score_type is null or d.score_type = '')
                    """,
                ),
                (
                    "PUBLISHED_LOG",
                    "已发布结果缺少发布日志",
                    "FAIL",
                    """
                    select res.id as result_id, res.activity_id, res.round_id, res.object_id
                    from review_result res
                    where res.del_flag = '0' and res.result_status = 'PUBLISHED'
                      and not exists (
                          select 1
                          from review_result_publish_log log
                          where log.del_flag = '0'
                            and log.activity_id = res.activity_id
                            and (log.round_id <=> res.round_id)
                            and (log.object_id <=> res.object_id)
                            and log.status = 'PUBLISHED'
                      )
                    """,
                ),
                (
                    "RESULT_SCORE",
                    "已生成或已发布结果缺少系统计算分",
                    "FAIL",
                    """
                    select id as result_id, activity_id, round_id, object_id, result_status
                    from review_result
                    where del_flag = '0'
                      and result_status in ('GENERATED', 'PUBLISHED')
                      and calculated_score is null
                    """,
                ),
                (
                    "SESSION_CURRENT_IN_LIST",
                    "场次当前对象不在场次对象列表中",
                    "FAIL",
                    """
                    select s.id as session_id, s.activity_id, s.round_id, s.current_object_id
                    from review_session s
                    where s.del_flag = '0' and s.current_object_id is not null
                      and not exists (
                          select 1
                          from review_session_object so
                          where so.del_flag = '0'
                            and so.session_id = s.id
                            and so.object_id = s.current_object_id
                      )
                    """,
                ),
                (
                    "SESSION_CURRENT_OBJECT_STATUS",
                    "场次当前对象不是 LOCKED 状态",
                    "FAIL",
                    """
                    select s.id as session_id, s.current_object_id, o.submit_status
                    from review_session s
                    join review_object o on o.id = s.current_object_id and o.del_flag = '0'
                    where s.del_flag = '0' and s.current_object_id is not null
                      and o.submit_status <> 'LOCKED'
                    """,
                ),
                (
                    "REVIEWER_MENU_ROLE",
                    "已分配专家账号缺少专家端菜单权限",
                    "FAIL",
                    f"""
                    select distinct a.reviewer_user_id, u.user_name, p.perm as missing_perm
                    from review_assignment a
                    join sys_user u on u.user_id = a.reviewer_user_id
                    join (
                        select 'competition:review:my-review:list' as perm
                        union all select 'competition:review:my-review:query' as perm
                        union all select 'competition:review:my-review:edit' as perm
                        union all select 'competition:review:my-review:submit' as perm
                    ) p
                    where a.del_flag = '0'
                      and coalesce(a.status, '') <> 'CANCELLED'
                      and u.user_id <> 1
                      and not exists (
                          select 1
                          from sys_user_role ur
                          join sys_role_menu rm on rm.role_id = ur.role_id
                          join sys_menu m on m.menu_id = rm.menu_id and m.status = '0'
                          where ur.user_id = a.reviewer_user_id
                            and m.perms = p.perm
                      )
                    """,
                ),
                (
                    "SECRETARY_MENU_ROLE",
                    "场次秘书账号缺少秘书控制台权限",
                    "FAIL",
                    f"""
                    select distinct s.secretary_user_id, u.user_name, p.perm as missing_perm
                    from review_session s
                    join sys_user u on u.user_id = s.secretary_user_id
                    join (
                        select 'competition:review:secretary:query' as perm
                        union all select 'competition:review:secretary:edit' as perm
                    ) p
                    where s.del_flag = '0'
                      and s.secretary_user_id is not null
                      and u.user_id <> 1
                      and not exists (
                          select 1
                          from sys_user_role ur
                          join sys_role_menu rm on rm.role_id = ur.role_id
                          join sys_menu m on m.menu_id = rm.menu_id and m.status = '0'
                          where ur.user_id = s.secretary_user_id
                            and m.perms = p.perm
                      )
                    """,
                ),
            ]

            for check in checks:
                results.append(run_query_check(cur, *check))
    finally:
        conn.close()

    fail_count = sum(1 for item in results if item["status"] == "FAIL")
    warn_count = sum(1 for item in results if item["status"] == "WARN")
    pass_count = sum(1 for item in results if item["status"] == "PASS")

    lines = [
        "# 评审模块上线前数据检查报告",
        "",
        f"- 生成时间：{_dt.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
        f"- 数据库：{cfg['host']}:{cfg['port']}/{cfg['database']}",
        f"- 汇总：PASS {pass_count}，WARN {warn_count}，FAIL {fail_count}",
        "",
    ]
    for item in results:
        lines.append(f"## [{item['status']}] {item['id']} - {item['title']}")
        rows = item["rows"]
        if not rows:
            lines.append("")
            lines.append("未发现异常。")
            lines.append("")
            continue
        lines.append("")
        lines.append(format_rows(rows))
        lines.append("")

    with open(output, "w", encoding="utf-8") as fh:
        fh.write("\n".join(lines))

    print(f"Wrote {output}")
    print(f"PASS={pass_count} WARN={warn_count} FAIL={fail_count}")
    return 1 if fail_count else 0


if __name__ == "__main__":
    sys.exit(main())

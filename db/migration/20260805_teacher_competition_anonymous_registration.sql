-- 教师赛取消选手账号、实名认证和教师身份前置条件后，保存 Excel 中的组织信息快照。
ALTER TABLE competition_apply_info
    ADD COLUMN company_name varchar(200) NULL COMMENT '单位名称（报名快照）' AFTER org_id,
    ADD COLUMN org_name_snapshot varchar(200) NULL COMMENT '机构名称（报名快照）' AFTER company_name;

-- 重复报名按赛事、赛道、身份证号查询；cancelled 仍由查询条件排除。
CREATE INDEX idx_apply_teacher_duplicate
    ON competition_apply_info (competition_series_id, competition_track_id, id_card, del_flag, pay_status);

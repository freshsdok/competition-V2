package com.teaching.system.api.domain;

import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

public class CompetitionApplyInfoExcel extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 赛事系列id(个人参赛) */
    private Long competitionSeriesId;

    /** 用户id */
    private Long userId;

    /** 赛事赛道name */
    @Excel(name = "赛道名称")
    private String competitionTrackName;

    /** 赛事赛道二级name */
    @Excel(name = "*参赛组别\n" +
            "（下拉选择）")
    private String secondLevelName;

    /** 参赛姓名 */
    @Excel(name = "*选手1姓名")
    private String userName;

    /** 身份证号 */
    @Excel(name = "*选手1证件号")
    private String idCard;

    /** 联系电话 */
    @Excel(name = "*选手1手机号")
    private String phone;

    /** 邮箱 */
    @Excel(name = "*选手1邮箱")
    private String email;

    /** 性别 */
    @Excel(name = "*选手1性别\n" +
            "（下拉选择）")
    private String sex;

    /** 年级 */
    @Excel(name = "*选手1学级")
    private String classInfo;

    /** 专业 /部门 */
    @Excel(name = "*选手1\n" +
            "专业名称")
    private String profession;
}

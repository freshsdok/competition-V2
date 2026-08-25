package com.teaching.competition.review.enums;

/**
 * 评审对象填报状态操作类型。
 */
public enum ReviewSubmitActionType {
    SAVE_DRAFT("SAVE_DRAFT", "保存草稿"),
    SUBMIT("SUBMIT", "提交"),
    WITHDRAW_REQUEST("WITHDRAW_REQUEST", "申请撤回"),
    WITHDRAW_APPROVE("WITHDRAW_APPROVE", "撤回通过"),
    WITHDRAW_REJECT("WITHDRAW_REJECT", "撤回驳回"),
    LOCK("LOCK", "锁定"),
    INVALID("INVALID", "作废"),
    MATERIAL_ADD("MATERIAL_ADD", "上传材料"),
    MATERIAL_DELETE("MATERIAL_DELETE", "删除材料");

    private final String code;
    private final String desc;

    ReviewSubmitActionType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}

package com.teaching.competition.review.enums;

    /**
     * ReviewPermissionType枚举。
     */
    public enum ReviewPermissionType {
CREATE("CREATE", "创建"),
EDIT("EDIT", "编辑"),
SUBMIT("SUBMIT", "提交"),
EDIT_SUBMIT("EDIT_SUBMIT", "编辑并提交");

        private final String code;
        private final String desc;

        ReviewPermissionType(String code, String desc) {
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

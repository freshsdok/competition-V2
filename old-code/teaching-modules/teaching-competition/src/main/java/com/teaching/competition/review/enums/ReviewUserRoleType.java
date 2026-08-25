package com.teaching.competition.review.enums;

    /**
     * ReviewUserRoleType枚举。
     */
    public enum ReviewUserRoleType {
ADMIN("ADMIN", "管理员"),
OPERATOR("OPERATOR", "操作员"),
REVIEWER("REVIEWER", "评审专家"),
SECRETARY("SECRETARY", "评审秘书"),
OBJECT_OWNER("OBJECT_OWNER", "对象负责人"),
AUDITOR("AUDITOR", "审计员");

        private final String code;
        private final String desc;

        ReviewUserRoleType(String code, String desc) {
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

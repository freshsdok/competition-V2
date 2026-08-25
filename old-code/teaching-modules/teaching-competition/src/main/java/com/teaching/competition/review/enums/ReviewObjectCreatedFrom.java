package com.teaching.competition.review.enums;

    /**
     * ReviewObjectCreatedFrom枚举。
     */
    public enum ReviewObjectCreatedFrom {
OPEN("OPEN", "开放填报"),
ASSIGNED_USER("ASSIGNED_USER", "指定用户"),
BUSINESS_IMPORTED("BUSINESS_IMPORTED", "业务导入"),
ADMIN_CREATED("ADMIN_CREATED", "管理员创建");

        private final String code;
        private final String desc;

        ReviewObjectCreatedFrom(String code, String desc) {
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

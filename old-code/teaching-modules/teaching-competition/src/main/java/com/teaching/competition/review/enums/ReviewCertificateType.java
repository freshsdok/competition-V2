package com.teaching.competition.review.enums;

    /**
     * ReviewCertificateType枚举。
     */
    public enum ReviewCertificateType {
CONTESTANT("CONTESTANT", "参赛证"),
TEACHER("TEACHER", "教师证"),
EXPERT("EXPERT", "专家证"),
STAFF("STAFF", "工作人员证");

        private final String code;
        private final String desc;

        ReviewCertificateType(String code, String desc) {
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

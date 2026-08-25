package com.teaching.competition.review.enums;

    /**
     * ReviewCertificateValidStatus枚举。
     */
    public enum ReviewCertificateValidStatus {
VALID("VALID", "有效"),
INVALID("INVALID", "无效"),
REVOKED("REVOKED", "已撤销"),
REPLACED("REPLACED", "已替换");

        private final String code;
        private final String desc;

        ReviewCertificateValidStatus(String code, String desc) {
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

package com.teaching.competition.review.enums;

    /**
     * ReviewCertificateResolveSourceType枚举。
     */
    public enum ReviewCertificateResolveSourceType {
SCAN("SCAN", "扫码"),
NEXT("NEXT", "下一位"),
MANUAL("MANUAL", "手动");

        private final String code;
        private final String desc;

        ReviewCertificateResolveSourceType(String code, String desc) {
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

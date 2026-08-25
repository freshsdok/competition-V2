package com.teaching.competition.review.enums;

    /**
     * ReviewRoundType枚举。
     */
    public enum ReviewRoundType {
MATERIAL_REVIEW("MATERIAL_REVIEW", "材料评审"),
ONSITE_DEFENSE("ONSITE_DEFENSE", "现场答辩"),
QUALIFICATION_CHECK("QUALIFICATION_CHECK", "资格审核"),
GROUP_REVIEW("GROUP_REVIEW", "专家组评审"),
FINAL_CONFIRM("FINAL_CONFIRM", "终评确认");

        private final String code;
        private final String desc;

        ReviewRoundType(String code, String desc) {
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

package com.teaching.competition.review.enums;

    /**
     * ReviewEventType枚举。
     */
    public enum ReviewEventType {
SCAN_CERT("SCAN_CERT", "扫码证件"),
SET_CURRENT("SET_CURRENT", "设置当前对象"),
NEXT_OBJECT("NEXT_OBJECT", "下一位"),
SKIP("SKIP", "跳过"),
ABSENT("ABSENT", "缺席"),
PRESENT("PRESENT", "到场"),
DELAY("DELAY", "延后"),
PAUSE("PAUSE", "暂停"),
RESUME("RESUME", "恢复"),
END("END", "结束");

        private final String code;
        private final String desc;

        ReviewEventType(String code, String desc) {
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

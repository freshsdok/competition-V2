package com.teaching.competition.review.enums;

    /**
     * ReviewMaterialType枚举。
     */
    public enum ReviewMaterialType {
DECLARATION("DECLARATION", "申报书"),
PPT("PPT", "展示文档"),
VIDEO("VIDEO", "视频"),
IMAGE("IMAGE", "图片"),
PDF("PDF", "PDF"),
DOC("DOC", "Word文档"),
ZIP("ZIP", "压缩包"),
OTHER("OTHER", "其他");

        private final String code;
        private final String desc;

        ReviewMaterialType(String code, String desc) {
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

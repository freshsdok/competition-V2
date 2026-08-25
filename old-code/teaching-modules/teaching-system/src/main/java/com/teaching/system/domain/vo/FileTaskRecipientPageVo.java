package com.teaching.system.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传任务应交用户分页及不受检索条件影响的汇总。
 */
@Data
public class FileTaskRecipientPageVo {
    private List<FileTaskRecipientVo> rows = new ArrayList<>();
    private long total;
    private long totalCount;
    private long uploadedCount;
    private long notUploadedCount;
}

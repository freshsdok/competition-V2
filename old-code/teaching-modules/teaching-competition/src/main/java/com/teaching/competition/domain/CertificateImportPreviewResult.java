package com.teaching.competition.domain;

import java.util.Collections;
import java.util.List;

/**
 * 证书导入预览结果，不包含完整 SQL。
 */
public class CertificateImportPreviewResult {
    private final int historyRowCount;
    private final int originRowCount;
    private final int warningCount;
    private final List<String> warnings;

    public CertificateImportPreviewResult(int historyRowCount, int originRowCount,
                                          int warningCount, List<String> warnings) {
        this.historyRowCount = historyRowCount;
        this.originRowCount = originRowCount;
        this.warningCount = warningCount;
        this.warnings = warnings == null ? Collections.emptyList() : List.copyOf(warnings);
    }

    public int getHistoryRowCount() {
        return historyRowCount;
    }

    public int getOriginRowCount() {
        return originRowCount;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public List<String> getWarnings() {
        return warnings;
    }
}

package com.teaching.competition.domain;

import lombok.Data;

import java.util.List;

@Data
public class CertificateExportRequest {
    /** ALL 或 SELECTED。 */
    private String scope;
    private List<String> certCodes;
}

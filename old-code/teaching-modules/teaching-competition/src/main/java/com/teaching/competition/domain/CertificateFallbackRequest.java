package com.teaching.competition.domain;

import lombok.Data;

import java.util.List;

@Data
public class CertificateFallbackRequest {
    private List<String> certCodes;
}

package com.teaching.competition.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CertificateFilterOptions {
    private List<String> contestNames = new ArrayList<>();
    private List<String> sessions = new ArrayList<>();
    private List<String> contestAreas = new ArrayList<>();
    private List<Integer> runingNumYears = new ArrayList<>();
}

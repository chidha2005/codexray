package com.codexray.model;

public record RiskFinding(
        String severity,
        String category,
        String title,
        String description,
        String recommendation,
        int line
) {}

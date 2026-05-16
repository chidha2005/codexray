package com.codexray.model;

public record LoopModel(
        String loopType,
        int beginLine,
        int endLine,
        String riskLevel,
        String reason
) {}

package com.codexray.model;

public record ObjectCreationModel(
        String type,
        String expression,
        String allocationCategory,
        int line,
        String memoryImpact
) {}

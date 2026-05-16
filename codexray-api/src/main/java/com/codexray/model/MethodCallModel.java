package com.codexray.model;

public record MethodCallModel(
        String methodName,
        String scope,
        int line,
        String category
) {}

package com.codexray.model;

public record ConditionModel(
        String conditionType,
        String expression,
        int line
) {}

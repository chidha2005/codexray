package com.codexray.model;

public record AnalysisSummary(
        int classCount,
        int methodCount,
        int fieldCount,
        int localVariableCount,
        int parameterCount,
        int objectCreationCount,
        int loopCount,
        int conditionCount,
        int methodCallCount,
        int riskFindingCount
) {}

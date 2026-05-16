package com.codexray.dto;

import com.codexray.model.*;
import java.util.List;
import java.util.UUID;

public record JavaAnalyzeResponse(
        UUID analysisId,
        String language,
        AnalysisSummary summary,
        List<ClassModel> classes,
        List<MethodModel> methods,
        List<FieldModel> fields,
        List<VariableModel> variables,
        List<ObjectCreationModel> objectCreations,
        List<LoopModel> loops,
        List<ConditionModel> conditions,
        List<MethodCallModel> methodCalls,
        List<RiskFinding> riskFindings
) {}

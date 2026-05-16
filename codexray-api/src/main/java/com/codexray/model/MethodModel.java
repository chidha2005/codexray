package com.codexray.model;

import java.util.List;

public record MethodModel(
        String name,
        String returnType,
        List<String> modifiers,
        int parameterCount,
        int beginLine,
        int endLine,
        boolean recursive,
        boolean staticMethod
) {}

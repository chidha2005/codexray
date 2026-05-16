package com.codexray.model;

import java.util.List;

public record ClassModel(
        String name,
        String type,
        String packageName,
        List<String> modifiers,
        List<String> extendedTypes,
        List<String> implementedTypes,
        int beginLine,
        int endLine
) {}

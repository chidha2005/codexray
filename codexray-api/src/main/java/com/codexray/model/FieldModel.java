package com.codexray.model;

import java.util.List;

public record FieldModel(
        String name,
        String type,
        List<String> modifiers,
        boolean primitive,
        boolean collectionLike,
        boolean staticField,
        int line
) {}

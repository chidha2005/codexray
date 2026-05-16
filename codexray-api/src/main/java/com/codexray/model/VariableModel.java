package com.codexray.model;

public record VariableModel(
        String name,
        String type,
        String scope,
        boolean primitive,
        boolean collectionLike,
        boolean objectReference,
        int line
) {}

package com.codexray.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JavaAnalyzeRequest(
        @NotBlank(message = "sourceCode is required")
        @Size(max = 200_000, message = "sourceCode exceeds maximum allowed size")
        String sourceCode
) {}

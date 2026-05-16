package com.codexray.exception;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String errorCode,
        String message,
        String path,
        List<String> details
) {}

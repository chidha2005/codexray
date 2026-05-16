package com.codexray.service;

import com.codexray.analyzer.JavaStaticAnalyzer;
import com.codexray.dto.JavaAnalyzeRequest;
import com.codexray.dto.JavaAnalyzeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JavaAnalysisService {
    private final JavaStaticAnalyzer javaStaticAnalyzer;

    public JavaAnalyzeResponse analyze(JavaAnalyzeRequest request) {
        return javaStaticAnalyzer.analyze(request.sourceCode());
    }
}

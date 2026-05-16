package com.codexray.api;

import com.codexray.dto.JavaAnalyzeRequest;
import com.codexray.dto.JavaAnalyzeResponse;
import com.codexray.service.JavaAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analyze")
@RequiredArgsConstructor
public class JavaAnalysisController {
    private final JavaAnalysisService javaAnalysisService;

    @PostMapping("/java")
    public ResponseEntity<JavaAnalyzeResponse> analyzeJava(@Valid @RequestBody JavaAnalyzeRequest request) {
        return ResponseEntity.ok(javaAnalysisService.analyze(request));
    }
}

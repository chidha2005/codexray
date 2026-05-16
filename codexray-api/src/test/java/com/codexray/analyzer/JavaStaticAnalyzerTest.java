package com.codexray.analyzer;

import com.codexray.dto.JavaAnalyzeResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JavaStaticAnalyzerTest {
    private final JavaStaticAnalyzer analyzer = new JavaStaticAnalyzer();

    @Test
    void shouldAnalyzeJavaClassSuccessfully() {
        String code = """
                import java.util.*;

                public class Demo {
                    private static Map<String, Object> cache = new HashMap<>();

                    public List<String> process(List<String> input) {
                        List<String> result = new ArrayList<>();

                        for (String value : input) {
                            String normalized = value.trim().toLowerCase();
                            result.add(normalized);
                        }

                        return result;
                    }
                }
                """;

        JavaAnalyzeResponse response = analyzer.analyze(code);

        assertThat(response.language()).isEqualTo("JAVA");
        assertThat(response.summary().classCount()).isEqualTo(1);
        assertThat(response.summary().methodCount()).isEqualTo(1);
        assertThat(response.summary().fieldCount()).isEqualTo(1);
        assertThat(response.summary().objectCreationCount()).isEqualTo(2);
        assertThat(response.summary().loopCount()).isEqualTo(1);
        assertThat(response.riskFindings()).isNotEmpty();
    }
}

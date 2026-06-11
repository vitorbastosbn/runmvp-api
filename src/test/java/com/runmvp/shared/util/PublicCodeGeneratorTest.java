package com.runmvp.shared.util;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class PublicCodeGeneratorTest {

    @Test
    void generate_returns8CharCode() {
        String code = PublicCodeGenerator.generate();
        assertThat(code).hasSize(8);
    }

    @Test
    void generate_onlyAllowedChars() {
        for (int i = 0; i < 500; i++) {
            String code = PublicCodeGenerator.generate();
            assertThat(code).matches("[A-Z2-9]{8}");
        }
    }

    @Test
    void generate_noAmbiguousChars() {
        for (int i = 0; i < 500; i++) {
            String code = PublicCodeGenerator.generate();
            assertThat(code).doesNotContain("0","O","1","I","L");
        }
    }

    @Test
    void generate_producesDifferentValues() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            codes.add(PublicCodeGenerator.generate());
        }
        assertThat(codes.size()).isGreaterThan(990);
    }
}

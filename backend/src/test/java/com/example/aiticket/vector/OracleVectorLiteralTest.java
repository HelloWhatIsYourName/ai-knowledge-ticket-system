package com.example.aiticket.vector;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OracleVectorLiteralTest {
    @Test
    void convertsFloatListToOracleVectorLiteral() {
        String literal = OracleVectorLiteral.from(List.of(0.1f, -0.2f, 0.3f));

        assertThat(literal).isEqualTo("[0.1,-0.2,0.3]");
    }

    @Test
    void rejectsEmptyVector() {
        assertThatThrownBy(() -> OracleVectorLiteral.from(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("vector must not be empty");
    }
}

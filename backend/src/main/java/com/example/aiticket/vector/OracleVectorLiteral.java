package com.example.aiticket.vector;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public final class OracleVectorLiteral {
    private OracleVectorLiteral() {
    }

    public static String from(List<Float> vector) {
        if (vector == null || vector.isEmpty()) {
            throw new IllegalArgumentException("vector must not be empty");
        }
        return vector.stream()
                .map(OracleVectorLiteral::format)
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static String format(Float value) {
        if (value == null) {
            throw new IllegalArgumentException("vector value must not be null");
        }
        return new BigDecimal(value.toString()).stripTrailingZeros().toPlainString();
    }
}

package org.emerald.butler.util;

import java.util.Objects;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NumericCheck {
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static final Pattern LONG_PATTERN = Pattern.compile("-?\\d+(\\d+)?");
    private final String value;

    public boolean isNumeric() {
        if (Objects.isNull(value)) {
            return false;
        }

        return NUMERIC_PATTERN.matcher(value).matches();
    }

    public boolean isInteger() {
        if (Objects.isNull(value)) {
            return false;
        }

        return LONG_PATTERN.matcher(value).matches();
    }

    public boolean isLong() {
        if (Objects.isNull(value)) {
            return false;
        }

        return LONG_PATTERN.matcher(value).matches();
    }
}

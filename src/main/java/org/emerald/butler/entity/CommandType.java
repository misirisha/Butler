package org.emerald.butler.entity;

import java.util.Objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommandType {
    PRIVATE("private"), GROUP("group"), SUPERGROUP("supergroup"), CHANNEL("channel");

    private final String code;

    public static CommandType getByCode(String code) {
        for (CommandType type : CommandType.values()) {
            if (Objects.equals(code, type.getCode())) {
                return type;
            }
        }

        throw new IllegalArgumentException();
    }
}

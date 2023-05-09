package org.emerald.butler.entity;

import io.jmix.core.metamodel.datatype.impl.EnumClass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatRole implements EnumClass<String> {
    DWELLER("dweller"),
    CHIEF("chief");

    private final String id;

    public static ChatRole fromId(String id) {
        for (ChatRole at : ChatRole.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }

        throw new IllegalArgumentException("No ChatRole for given id is present. Given: " + id);
    }
}

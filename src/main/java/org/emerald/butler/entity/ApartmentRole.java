package org.emerald.butler.entity;

import io.jmix.core.metamodel.datatype.impl.EnumClass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApartmentRole implements EnumClass<String> {
    DWELLER("dweller"),
    OWNER("owner");

    private final String id;

    public static ApartmentRole fromId(String id) {
        for (ApartmentRole at : ApartmentRole.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }

        throw new IllegalArgumentException("No ApartmentRole for given id is present. Given: " + id);
    }
}

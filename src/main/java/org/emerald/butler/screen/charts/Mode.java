package org.emerald.butler.screen.charts;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum Mode implements EnumClass<Integer> {
    BY_DWELLERS(10),
    BY_APARTMENTS(20);

    private final Integer id;

    Mode(Integer id) {
        this.id = id;
    }

    @Nonnull
    public Integer getId() {
        return id;
    }

    @Nullable
    public static Mode fromId(Integer id) {
        for (Mode at : Mode.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
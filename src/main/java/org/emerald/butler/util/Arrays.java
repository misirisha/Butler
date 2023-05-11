package org.emerald.butler.util;

import java.util.Objects;

public final class Arrays {
    private Arrays() {}

    public static <T> boolean contains(T[] array, T element) {
        for (T item : array) {
            if (Objects.equals(item, element)) {
                return true;
            }
        }

        return false;
    }
}
package org.emerald.butler.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class RandomList<T> extends ArrayList<T> {
    private final Random random = new Random();

    public RandomList(int initialCapacity) {
        super(initialCapacity);
    }

    public RandomList() {
    }

    public RandomList(T... ts) {
        this(List.of(ts));
    }

    public RandomList(Collection<? extends T> c) {
        super(c);
    }

    public Optional<T> findRandom() {
        if (isEmpty()) {
            return Optional.empty();
        }

        int index = random.nextInt(size());
        return Optional.ofNullable(get(index));
    }

    public T getRandom() {
        if (isEmpty()) {
            throw new IllegalStateException("Empty");
        }

        int index = random.nextInt(size());
        return get(index);
    }
}
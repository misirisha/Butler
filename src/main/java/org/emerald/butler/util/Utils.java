package org.emerald.butler.util;

import java.util.concurrent.Callable;


public final class Utils {
    private Utils() {}

    public static void silently(Action action) {
        try {
            action.execute();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public static <R> R silently(Callable<R> callable) {
        try {
            return callable.call();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}

package org.emerald.butler.util;

public class Format {
    private static final String FORMAT_KEYWORD = "{}";
    private final String formatted;
    private final Object[] args;

    public Format(String format, Object... args) {
        this.formatted = format;
        this.args = args;
    }

    public String get() {
        int index = formatted.indexOf(FORMAT_KEYWORD);
        if (index == -1) {
            return formatted;
        }

        int argNumber = 0;
        final StringBuilder result = new StringBuilder()
                .append(formatted, 0, index)
                .append(args[argNumber].toString());

        int endIndex = index;
        argNumber += 1;

        while (true) {
            index = formatted.indexOf(FORMAT_KEYWORD, endIndex + 1);
            if (index == -1) {
                break;
            }

            result.append(formatted, endIndex + FORMAT_KEYWORD.length(), index)
                    .append(args[argNumber].toString());

            endIndex = index;
            argNumber += 1;
        }

        return result.toString();
    }

    @Override
    public String toString() {
        return get();
    }
}

package org.emerald.butler.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NumericCheckTest {

    @Test
    void checkIsLong() {
        Assertions.assertTrue(new NumericCheck("12").isLong());
        Assertions.assertTrue(new NumericCheck("1").isLong());
        Assertions.assertTrue(new NumericCheck("-500").isLong());
        Assertions.assertFalse(new NumericCheck("10.2").isLong());
        Assertions.assertFalse(new NumericCheck("0.0").isLong());
        Assertions.assertFalse(new NumericCheck("-12.3").isLong());
        Assertions.assertFalse(new NumericCheck("1L").isLong());
        Assertions.assertFalse(new NumericCheck("Hello world").isLong());
        Assertions.assertFalse(new NumericCheck("").isLong());
        Assertions.assertFalse(new NumericCheck(null).isLong());
        Assertions.assertFalse(new NumericCheck("    ").isLong());
    }
}

package org.heigit.ors.config.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NonEmptyMapFilter {

    @Override
    public boolean equals(Object other) {
        if (other == null) return true;
        try {
            Method isEmptyMethod = other.getClass().getDeclaredMethod("isEmpty");
            if (isEmptyMethod.invoke(other).equals(true)) {
                return true;
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // Ignore
        }
        return false;
    }
}

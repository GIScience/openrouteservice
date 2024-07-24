package org.heigit.ors.config.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NonEmptyObjectFilter {

    @Override
    public boolean equals(Object other) {
        if (other == null) return true;
        for (Method m : other.getClass().getDeclaredMethods()) {
            if (!m.getReturnType().equals(Void.TYPE) && m.getParameterTypes().length == 0) {
                try {
                    if (m.invoke(other) != null) {
                        return false;
                    }
                } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException  e) {
                    // Ignore
                }
            }
        }
        return true;
    }
}

package org.heigit.ors.config.utils;

import org.heigit.ors.config.profile.EncoderOptionsProperties;
import org.heigit.ors.config.profile.ExecutionProperties;
import org.heigit.ors.config.profile.PreparationProperties;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NonEmptyObjectFilter {

    @Override
    public boolean equals(Object other) {
        if (other == null) return true;
        for (Method m : other.getClass().getDeclaredMethods()) {
            if (!m.getReturnType().equals(Void.TYPE) && m.getParameterTypes().length == 0) {
                try {
                    Object o = m.invoke(other);
                    if (o != null) {
                        if (o instanceof EncoderOptionsProperties
                                || o instanceof PreparationProperties.MethodsProperties
                                || o instanceof PreparationProperties.MethodsProperties.CHProperties
                                || o instanceof PreparationProperties.MethodsProperties.LMProperties
                                || o instanceof PreparationProperties.MethodsProperties.CoreProperties
                                || o instanceof PreparationProperties.MethodsProperties.FastIsochroneProperties
                                || o instanceof ExecutionProperties.MethodsProperties
                                || o instanceof ExecutionProperties.MethodsProperties.AStarProperties
                                || o instanceof ExecutionProperties.MethodsProperties.LMProperties
                                || o instanceof ExecutionProperties.MethodsProperties.CoreProperties
                        ) {
                            if (!equals(o)) {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                } catch (InvocationTargetException | IllegalAccessException e) {
                    // Ignore
                }
            }
        }
        return true;
    }
}

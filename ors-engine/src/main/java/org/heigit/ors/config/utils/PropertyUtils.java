package org.heigit.ors.config.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class PropertyUtils {
    public static Object copyObjectProperties(Object source, Object target, boolean overwrite) {
        Logger logger = LoggerFactory.getLogger(Object.class);
        if (source == null || target == null) {
            throw new IllegalArgumentException("Source and target objects must not be null");
        }

        Class<?> clazz = target.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (!field.trySetAccessible()) {
                continue;
            }
            Object value = null;
            try {
                value = field.get(source);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                logger.warn("Could not access field: {}", field.getName());
            }
            if (value != null) {
                try {
                    Object currentValue = field.get(target);
                    if (overwrite || currentValue == null || (currentValue instanceof String && ((String) currentValue).isEmpty())) {
                        field.set(target, value);
                    }
                } catch (IllegalAccessException e) {
                    logger.warn("Could not set field: {}", field.getName());
                }
            }
        }
        return target;
    }
}

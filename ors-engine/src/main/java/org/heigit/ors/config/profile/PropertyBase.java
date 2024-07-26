package org.heigit.ors.config.profile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public abstract class PropertyBase {
    Logger logger = LoggerFactory.getLogger(PropertyBase.class);

    public void updateObject(Object source) {
        if (source == null) {
            throw new IllegalArgumentException("Source object must not be null");
        }

        if (!source.getClass().isAssignableFrom(this.getClass())) {
            throw new IllegalArgumentException("Current object must be an instance of the source object's class");
        }

        Class<?> clazz = source.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (!field.trySetAccessible()) {
                continue;
            }
            Object value = null;
            try {
                value = field.get(source);
            } catch (IllegalAccessException e) {
                logger.warn("Could not access field: {}", field.getName());
            }
            if (value != null) {
                try {
                    field.set(this, value);
                } catch (IllegalAccessException e) {
                    logger.warn("Could not set field: {}", field.getName());
                }
            }
        }
    }
}

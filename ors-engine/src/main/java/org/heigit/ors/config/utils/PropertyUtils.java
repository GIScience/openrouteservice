package org.heigit.ors.config.utils;

import org.heigit.ors.config.profile.storages.ExtendedStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class PropertyUtils {
    public static Object deepCopyObjectsProperties(Object source, Object target, boolean overwrite) {
        // If a field is a class, it will be copied recursively
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
            if (value == null) {
                continue;
            }
            try {
                Object currentValue = field.get(target);
                if (field.getType().isMemberClass()) {
                    field.set(target, deepCopyObjectsProperties(value, currentValue, overwrite));
                } else if (overwrite || currentValue == null || (currentValue instanceof String && ((String) currentValue).isEmpty())) {
                    field.set(target, value);
                }
            } catch (IllegalAccessException e) {
                logger.warn("Could not set field: {}", field.getName());
            }
        }
        return target;
    }

    public static Map<String, ExtendedStorage> deepCopyMapsProperties(Map<String, ExtendedStorage> source, Map<String, ExtendedStorage> target, boolean overwrite) {
        if (target == null) {
            return source;
        } else if (source == null) {
            return target;
        }

        // Create a new map to avoid modifying the original target
        HashMap<String, ExtendedStorage> targetUpdate = new HashMap<>(target);

        for (Map.Entry<String, ExtendedStorage> entry : source.entrySet()) {
            String key = entry.getKey();
            ExtendedStorage sourceValue = entry.getValue();
            Object targetValue = targetUpdate.get(key);

            if (sourceValue == null) {
                continue;
            }

            if (targetValue == null) {
                targetUpdate.put(key, sourceValue);
            } else {
                // Recursively copy nested maps
                targetUpdate.put(key, (ExtendedStorage) deepCopyObjectsProperties(sourceValue, targetValue, overwrite));
            }
        }
        return targetUpdate;
    }
}

package org.heigit.ors.config.utils;

import org.heigit.ors.config.profile.storages.ExtendedStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

public class PropertyUtils {
    public static Object deepCopyObjectsProperties(Object source, Object target, boolean overwriteNonEmptyFields, boolean copyEmptyMemberClasses) {
        Logger logger = LoggerFactory.getLogger(Object.class);
        if (source == null || target == null) {
            return target;
        }


        Class<?> clazz = target.getClass();
        List<Field> fields = new ArrayList<>();
        getAllFields(fields, clazz);
        for (Field field : fields) {
            Class<?> fieldType = field.getType();
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
                boolean shouldOverwrite = overwriteNonEmptyFields || currentValue == null;
                if (shouldOverwrite) {
                    field.set(target, value);
                } else if (!fieldType.isPrimitive() &&
                        !Number.class.isAssignableFrom(fieldType) &&
                        !Boolean.class.equals(fieldType) &&
                        !Character.class.equals(fieldType) &&
                        !String.class.equals(fieldType) &&
                        !Enum.class.isAssignableFrom(fieldType) &&
                        !Collection.class.isAssignableFrom(fieldType) &&
                        !fieldType.isArray()) {
                    field.set(target, deepCopyObjectsProperties(value, currentValue, overwriteNonEmptyFields, copyEmptyMemberClasses));
                }
            } catch (IllegalAccessException e) {
                logger.warn("Could not set field: {}", field.getName());
            }
        }
        return target;
    }

    public static Map<String, ExtendedStorage> deepCopyMapsProperties(Map<String, ExtendedStorage> source, Map<String, ExtendedStorage> target, boolean overwriteNonEmptyFields, boolean copyEmptyMemberClasses, boolean copyEmptyStorages) {
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
                if (copyEmptyStorages) targetUpdate.put(key, sourceValue);
            } else {
                // Recursively copy nested maps
                targetUpdate.put(key, (ExtendedStorage) deepCopyObjectsProperties(sourceValue, targetValue, overwriteNonEmptyFields, copyEmptyMemberClasses));
            }
        }
        return targetUpdate;
    }

    public static Boolean assertAllNull(Object o, ArrayList<String> ignoreList) throws IllegalAccessException {
        return assertAllNull(o, ignoreList, false);
    }

    public static Boolean assertAllNull(Object o, ArrayList<String> ignoreList, Boolean searchMemberClasses) throws IllegalAccessException {
        for (Field field : o.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (ignoreList.contains(field.getName())) {
                continue;
            }
            Object value = field.get(o);
            if (value == null) {
                continue;
            }
            if (searchMemberClasses) {
                if (!assertAllNull(value, ignoreList, true)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }


    public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }
}

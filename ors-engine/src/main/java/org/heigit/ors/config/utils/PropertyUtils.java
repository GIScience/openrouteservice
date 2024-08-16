package org.heigit.ors.config.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.nio.file.Path;
import java.util.*;

public class PropertyUtils {
    public static Boolean assertAllNull(Object o) throws IllegalAccessException {
        return assertAllNull(o, new HashSet<>());
    }

    public static Boolean assertAllNull(Object o, Set<String> excludeFields) throws IllegalAccessException {
        return assertAllNull(o, excludeFields, "");
    }


    private static Boolean assertAllNull(Object o, Set<String> excludeFields, String path) throws IllegalAccessException {
        for (Field field : o.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            String fullPath = path.isEmpty() ? field.getName() : path + "." + field.getName();
            if (shouldExclude(fullPath, excludeFields)) {
                continue;
            }
            Object value = field.get(o);
            if (value == null) {
                continue;
            }
            if (value.getClass().isPrimitive()) {
                return false;
            } else if (value instanceof Collection) {
                if (!((Collection<?>) value).isEmpty()) {
                    return false;
                }
            } else if (value instanceof Map) {
                if (!((Map<?, ?>) value).isEmpty()) {
                    return false;
                }
            } else if (value instanceof Object[]) {
                if (((Object[]) value).length > 0) {
                    return false;
                }
            } else if (value instanceof Path) {
                return false;
            } else if (value instanceof String) {
                if (!value.equals("")) {
                    return false;
                }
            } else if (value instanceof Number) {
                if (((Number) value).doubleValue() != 0) {
                    return false;
                }
            } else if (value instanceof Boolean) {
                if ((Boolean) value) {
                    return false;
                }
            } else if (value instanceof Enum) {
                return false;
            } else {
                if (!assertAllNull(value, excludeFields, fullPath)) {
                    return false;
                }
            }
        }
        return true;
    }


    public static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            fields.addAll(getAllFields(type.getSuperclass()));
        }
        return fields;
    }

    public static boolean deepEqualityCheckIsUnequal(Object obj1, Object obj2, Set<String> excludeFields) {
        return deepEqualityCheckIsUnequal(obj1, obj2, excludeFields, "");
    }

    private static boolean deepEqualityCheckIsUnequal(Object obj1, Object obj2, Set<String> excludeFields, String path) {
        if (obj1 == obj2) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }

        if (!obj1.getClass().isAssignableFrom(obj2.getClass()) && !obj2.getClass().isAssignableFrom(obj1.getClass())) {
            return false;
        }


        Class<?> inputClass = obj1.getClass();
        List<Field> allFields = getAllFields(inputClass);
        for (Field field : allFields) {
            String fullPath = path.isEmpty() ? field.getName() : path + "." + field.getName();
            if (shouldExclude(fullPath, excludeFields)) {
                continue;
            }

            try {
                field.setAccessible(true);
                Object value1 = field.get(obj1);
                Object value2 = field.get(obj2);

                if (value1 == value2) {
                    continue;
                }
                if (value1 == null || value2 == null) {
                    return false;
                }

                if (deepCompareFieldsAreUnequal(value1, value2, excludeFields, fullPath)) {
                    return false;
                }

            } catch (InaccessibleObjectException | IllegalAccessException e) {
                throw new RuntimeException("Could not access field: " + field.getName());
            }
        }
        return true;
    }

    protected static boolean deepCompareFieldsAreUnequal(Object value1, Object value2, Set<String> excludeFields, String path) {
        // Check on null
        if (value1 == null) {
            return !deepEqualityCheckIsUnequal(null, value2, excludeFields, path);
        }
        if (value1 instanceof Collection && value2 instanceof Collection) {
            return deepCompareCollections((Collection<?>) value1, (Collection<?>) value2, excludeFields, path);
        } else if (value1 instanceof Map && value2 instanceof Map) {
            // If their sizes are different, they are not equal
            if (((Map<?, ?>) value1).size() != ((Map<?, ?>) value2).size()) {
                return false;
            }
            return !deepEqualityCheckIsUnequal(value1, value2, excludeFields);
        } else if (value1 instanceof Object[] && value2 instanceof Object[]) {
            return !deepCompareArrays((Object[]) value1, (Object[]) value2, excludeFields, path);
        } else if (isPrimitiveOrWrapper(value1.getClass())) {
            return !value1.equals(value2);
        } else if (value1 instanceof Path && value2 instanceof Path) {
            return !value1.equals(value2);
        } else {
            return !deepEqualityCheckIsUnequal(value1, value2, excludeFields, path);
        }
    }

    private static boolean deepCompareCollections(Collection<?> col1, Collection<?> col2, Set<String> excludeFields, String path) {
        if (col1.size() != col2.size()) {
            return false;
        }
        Iterator<?> it1 = col1.iterator();
        Iterator<?> it2 = col2.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            if (deepCompareFieldsAreUnequal(it1.next(), it2.next(), excludeFields, path)) {
                return false;
            }
        }
        return true;
    }

    protected static boolean deepCompareArrays(Object[] arr1, Object[] arr2, Set<String> excludeFields, String path) {
        // Check on null
        if (arr1 == null || arr2 == null) {
            return false;
        }

        if (arr1.length != arr2.length) {
            return false;
        }
        for (int i = 0; i < arr1.length; i++) {
            if (deepCompareFieldsAreUnequal(arr1[i], arr2[i], excludeFields, path)) {
                return false;
            }
        }
        return true;
    }

    protected static boolean isPrimitiveOrWrapper(Class<?> type) {
        // Check for null
        if (type == null) {
            return false;
        }
        return type.isPrimitive() || type.isEnum() || type == Boolean.class || type == Integer.class || type == Character.class ||
                type == Byte.class || type == Short.class || type == Double.class || type == Long.class || type == Float.class || type == String.class;
    }

    protected static boolean shouldExclude(String path, Set<String> excludeFields) {
        for (String excludeField : excludeFields) {
            if (excludeField.equals(path)) {
                return true;
            }
        }
        return false;
    }

}

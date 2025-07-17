package io.github.mangjoo;

import io.github.mangjoo.annotations.ExcelColumn;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Annotation-based Excel data mapping class
 */
public class ExcelMapper {

    /**
     * Convert Excel data to object list of specified class
     *
     * @param rows Excel row data (Map format)
     * @param clazz Class type to convert to
     * @return Converted object list
     */
    public static <T> List<T> mapToObjects(List<Map<String, String>> rows, Class<T> clazz) {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        // Collect field-column mapping information for the class
        Map<String, FieldMapping> fieldMappings = analyzeClass(clazz);

        List<T> result = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            try {
                T instance = createObject(row, clazz, fieldMappings);
                result.add(instance);
            } catch (Exception e) {
                throw new ExcelMappingException(
                    "Failed to map row " + (i + 1) + " to " + clazz.getSimpleName() + ": " + e.getMessage(),
                    e
                );
            }
        }

        return result;
    }

    /**
     * Convert single row to object
     */
    public static <T> T mapToObject(Map<String, String> row, Class<T> clazz) {
        Map<String, FieldMapping> fieldMappings = analyzeClass(clazz);
        try {
            return createObject(row, clazz, fieldMappings);
        } catch (Exception e) {
            throw new ExcelMappingException(
                "Failed to map row to " + clazz.getSimpleName() + ": " + e.getMessage(),
                e
            );
        }
    }

    private static <T> Map<String, FieldMapping> analyzeClass(Class<T> clazz) {
        Map<String, FieldMapping> mappings = new HashMap<>();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
            if (annotation != null) {
                field.setAccessible(true);
                String columnName = annotation.value();
                mappings.put(columnName, new FieldMapping(field, annotation));
            }
        }

        return mappings;
    }

    private static <T> T createObject(Map<String, String> row, Class<T> clazz, Map<String, FieldMapping> fieldMappings)
            throws Exception {

        T instance = clazz.getDeclaredConstructor().newInstance();

        for (Map.Entry<String, FieldMapping> entry : fieldMappings.entrySet()) {
            String columnName = entry.getKey();
            FieldMapping mapping = entry.getValue();
            Field field = mapping.field;
            ExcelColumn annotation = mapping.annotation;

            String value = row.get(columnName);

            // Handle case when value is missing
            if (value == null || value.trim().isEmpty()) {
                if (!annotation.defaultValue().isEmpty()) {
                    value = annotation.defaultValue();
                } else if (annotation.required()) {
                    throw new ExcelMappingException(
                        "Required column '" + columnName + "' is missing or empty"
                    );
                } else {
                    continue; // Leave optional fields as null
                }
            }

            // Type conversion and field setting
            Object convertedValue = convertValue(value.trim(), field.getType(), columnName);
            field.set(instance, convertedValue);
        }

        return instance;
    }

    private static Object convertValue(String value, Class<?> targetType, String columnName) {
        try {
            if (targetType == String.class) {
                return value;
            } else if (targetType == int.class || targetType == Integer.class) {
                return Integer.parseInt(value);
            } else if (targetType == long.class || targetType == Long.class) {
                return Long.parseLong(value);
            } else if (targetType == double.class || targetType == Double.class) {
                return Double.parseDouble(value);
            } else if (targetType == float.class || targetType == Float.class) {
                return Float.parseFloat(value);
            } else if (targetType == boolean.class || targetType == Boolean.class) {
                return Boolean.parseBoolean(value) ||
                       "1".equals(value) ||
                       "Y".equalsIgnoreCase(value) ||
                       "YES".equalsIgnoreCase(value) ||
                       "TRUE".equalsIgnoreCase(value);
            } else if (targetType == BigDecimal.class) {
                return new BigDecimal(value);
            } else {
                throw new ExcelMappingException(
                    "Unsupported field type: " + targetType.getSimpleName() + " for column '" + columnName + "'"
                );
            }
        } catch (NumberFormatException e) {
            throw new ExcelMappingException(
                "Cannot convert value '" + value + "' to " + targetType.getSimpleName() + " for column '" + columnName + "'"
            );
        }
    }

    private static class FieldMapping {
        final Field field;
        final ExcelColumn annotation;

        FieldMapping(Field field, ExcelColumn annotation) {
            this.field = field;
            this.annotation = annotation;
        }
    }

    /**
     * Excel mapping related exception
     */
    public static class ExcelMappingException extends RuntimeException {
        public ExcelMappingException(String message) {
            super(message);
        }

        public ExcelMappingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
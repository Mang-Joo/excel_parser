package io.github.mangjoo;

import io.github.mangjoo.annotations.ExcelColumn;
import io.github.mangjoo.annotations.ExcelReadColumn;
import io.github.mangjoo.annotations.ExcelReadWrite;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        // Collect field mappings information for the class
        List<FieldMappingInfo> fieldMappings = analyzeClassFields(clazz);

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
        List<FieldMappingInfo> fieldMappings = analyzeClassFields(clazz);
        try {
            return createObject(row, clazz, fieldMappings);
        } catch (Exception e) {
            throw new ExcelMappingException(
                "Failed to map row to " + clazz.getSimpleName() + ": " + e.getMessage(),
                e
            );
        }
    }

    private static <T> List<FieldMappingInfo> analyzeClassFields(Class<T> clazz) {
        List<FieldMappingInfo> mappings = new ArrayList<>();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            
            // Check for ExcelColumn (legacy support)
            ExcelColumn columnAnnotation = field.getAnnotation(ExcelColumn.class);
            if (columnAnnotation != null) {
                FieldMappingInfo info = new FieldMappingInfo(field);
                info.addColumnName(columnAnnotation.value());
                info.setRequired(columnAnnotation.required());
                info.setDefaultValue(columnAnnotation.defaultValue());
                mappings.add(info);
                continue;
            }
            
            // Check for ExcelReadColumn
            ExcelReadColumn readAnnotation = field.getAnnotation(ExcelReadColumn.class);
            if (readAnnotation != null) {
                FieldMappingInfo info = new FieldMappingInfo(field);
                info.addColumnName(readAnnotation.value());
                // Add aliases
                for (String alias : readAnnotation.aliases()) {
                    info.addColumnName(alias);
                }
                info.setRequired(readAnnotation.required());
                info.setDefaultValue(readAnnotation.defaultValue());
                mappings.add(info);
                continue;
            }
            
            // Check for ExcelReadWrite
            ExcelReadWrite readWriteAnnotation = field.getAnnotation(ExcelReadWrite.class);
            if (readWriteAnnotation != null && !readWriteAnnotation.skip()) {
                FieldMappingInfo info = new FieldMappingInfo(field);
                info.addColumnName(readWriteAnnotation.value());
                // Add read aliases
                for (String alias : readWriteAnnotation.readAliases()) {
                    info.addColumnName(alias);
                }
                info.setRequired(readWriteAnnotation.required());
                info.setDefaultValue(readWriteAnnotation.defaultValue());
                mappings.add(info);
            }
        }

        return mappings;
    }

    private static <T> T createObject(Map<String, String> row, Class<T> clazz, List<FieldMappingInfo> fieldMappings)
            throws Exception {

        T instance = clazz.getDeclaredConstructor().newInstance();

        // Process each field
        for (FieldMappingInfo fieldInfo : fieldMappings) {
            String value = null;
            String foundColumnName = null;
            
            // Try to find a value using any of the possible column names
            for (String columnName : fieldInfo.getColumnNames()) {
                if (row.containsKey(columnName)) {
                    value = row.get(columnName);
                    foundColumnName = columnName;
                    break;
                }
            }
            
            // Handle case when value is missing or empty
            if (value == null || value.trim().isEmpty()) {
                if (!fieldInfo.getDefaultValue().isEmpty()) {
                    value = fieldInfo.getDefaultValue();
                } else if (fieldInfo.isRequired()) {
                    // Create a helpful error message listing all possible column names
                    String columnNames = String.join("' or '", fieldInfo.getColumnNames());
                    throw new ExcelMappingException(
                        "Required column '" + columnNames + "' is missing or empty for field " + 
                        fieldInfo.getField().getName()
                    );
                } else {
                    continue; // Leave optional fields as null
                }
            }

            // Type conversion and field setting
            Object convertedValue = convertValue(value.trim(), fieldInfo.getField().getType(), 
                foundColumnName != null ? foundColumnName : fieldInfo.getColumnNames().iterator().next());
            fieldInfo.getField().set(instance, convertedValue);
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
    
    /**
     * Helper class to store field mapping information
     */
    private static class FieldMappingInfo {
        private final Field field;
        private final Set<String> columnNames = new HashSet<>();
        private boolean required = true;
        private String defaultValue = "";
        
        FieldMappingInfo(Field field) {
            this.field = field;
        }
        
        void addColumnName(String name) {
            columnNames.add(name);
        }
        
        Set<String> getColumnNames() {
            return columnNames;
        }
        
        Field getField() {
            return field;
        }
        
        boolean isRequired() {
            return required;
        }
        
        void setRequired(boolean required) {
            this.required = required;
        }
        
        String getDefaultValue() {
            return defaultValue;
        }
        
        void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
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
package io.github.mangjoo;

import io.github.mangjoo.annotations.ExcelReadWrite;
import io.github.mangjoo.annotations.ExcelWriteColumn;
import io.github.mangjoo.writer.HeaderConfig;
import io.github.mangjoo.writer.WriteConfig;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for writing Java objects to Excel files.
 * Supports annotation-based mapping for automatic Excel generation.
 */
public class ExcelWriter {
    
    /**
     * Convert a list of objects to Excel using annotations.
     * 
     * @param data List of objects to write
     * @param clazz Class type of the objects
     * @param sheetName Name of the Excel sheet
     * @return WriteConfig ready for Excel generation
     */
    public static <T> WriteConfig createWriteConfig(List<T> data, Class<T> clazz, String sheetName) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data list cannot be null or empty");
        }
        
        // Extract field mappings from annotations
        List<FieldMapping> mappings = extractWriteMappings(clazz);
        
        // Sort by order
        mappings.sort(Comparator.comparingInt(m -> m.order));
        
        // Create headers
        List<HeaderConfig> headers = mappings.stream()
                .map(mapping -> HeaderConfig.builder()
                        .name(mapping.columnName)
                        .width(mapping.width > 0 ? (double) mapping.width : null)
                        .format(mapping.format.isEmpty() ? null : mapping.format)
                        .build())
                .collect(Collectors.toList());
        
        // Convert objects to data rows
        List<List<String>> rows = new ArrayList<>();
        for (T obj : data) {
            List<String> row = new ArrayList<>();
            for (FieldMapping mapping : mappings) {
                String value = extractFieldValue(obj, mapping.field);
                row.add(value);
            }
            rows.add(row);
        }
        
        return new WriteConfig(sheetName, headers, rows);
    }
    
    /**
     * Create write config with default sheet name "Sheet1".
     */
    public static <T> WriteConfig createWriteConfig(List<T> data, Class<T> clazz) {
        return createWriteConfig(data, clazz, "Sheet1");
    }
    
    /**
     * Extract write mappings from class annotations.
     */
    private static List<FieldMapping> extractWriteMappings(Class<?> clazz) {
        List<FieldMapping> mappings = new ArrayList<>();
        
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            
            // Check for ExcelWriteColumn annotation
            ExcelWriteColumn writeAnnotation = field.getAnnotation(ExcelWriteColumn.class);
            if (writeAnnotation != null && !writeAnnotation.skip()) {
                mappings.add(new FieldMapping(
                    field,
                    writeAnnotation.value(),
                    writeAnnotation.order(),
                    writeAnnotation.format(),
                    writeAnnotation.width()
                ));
                continue;
            }
            
            // Check for ExcelReadWrite annotation
            ExcelReadWrite readWriteAnnotation = field.getAnnotation(ExcelReadWrite.class);
            if (readWriteAnnotation != null && !readWriteAnnotation.skip()) {
                mappings.add(new FieldMapping(
                    field,
                    readWriteAnnotation.value(),
                    readWriteAnnotation.writeOrder(),
                    readWriteAnnotation.writeFormat(),
                    readWriteAnnotation.columnWidth()
                ));
            }
        }
        
        if (mappings.isEmpty()) {
            throw new IllegalArgumentException("No Excel write annotations found in class: " + clazz.getName());
        }
        
        return mappings;
    }
    
    /**
     * Extract field value as string.
     */
    private static String extractFieldValue(Object obj, Field field) {
        try {
            Object value = field.get(obj);
            return value != null ? value.toString() : "";
        } catch (IllegalAccessException e) {
            throw new ExcelWriteException("Failed to access field: " + field.getName(), e);
        }
    }
    
    /**
     * Internal class to hold field mapping information.
     */
    private static class FieldMapping {
        final Field field;
        final String columnName;
        final int order;
        final String format;
        final int width;
        
        FieldMapping(Field field, String columnName, int order, String format, int width) {
            this.field = field;
            this.columnName = columnName;
            this.order = order;
            this.format = format;
            this.width = width;
        }
    }
    
    /**
     * Exception for Excel write operations.
     */
    public static class ExcelWriteException extends RuntimeException {
        public ExcelWriteException(String message) {
            super(message);
        }
        
        public ExcelWriteException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
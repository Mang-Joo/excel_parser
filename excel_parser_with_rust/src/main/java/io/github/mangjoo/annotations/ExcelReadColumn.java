package io.github.mangjoo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for mapping Excel columns to Java fields during read operations.
 * This annotation is used when reading data from Excel files.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelReadColumn {
    /**
     * The column name in the Excel file to map to this field.
     * This should match the header row value.
     */
    String value();
    
    /**
     * Whether this column is required during reading.
     * If true and the column is missing, an exception will be thrown.
     */
    boolean required() default true;
    
    /**
     * Default value to use if the cell is empty or missing.
     */
    String defaultValue() default "";
    
    /**
     * Alternative column names to try if the primary name is not found.
     * Useful for handling different Excel formats with varying headers.
     */
    String[] aliases() default {};
}
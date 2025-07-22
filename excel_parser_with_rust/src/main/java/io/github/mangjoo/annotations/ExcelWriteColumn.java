package io.github.mangjoo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for mapping Java fields to Excel columns during write operations.
 * This annotation is used when writing data to Excel files.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelWriteColumn {
    /**
     * The column header name to use in the Excel file.
     */
    String value();
    
    /**
     * The order/position of this column in the Excel file.
     * Lower values appear first (leftmost).
     */
    int order() default Integer.MAX_VALUE;
    
    /**
     * Format pattern for the cell (e.g., date format, number format).
     * Examples: "yyyy-MM-dd", "#,##0.00", "@" (text)
     */
    String format() default "";
    
    /**
     * Whether to skip this field during writing.
     * Useful for conditional exclusion.
     */
    boolean skip() default false;
    
    /**
     * Column width in characters.
     * -1 means auto-size.
     */
    int width() default -1;
}
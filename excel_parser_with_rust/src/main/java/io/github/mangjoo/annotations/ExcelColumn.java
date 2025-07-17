package io.github.mangjoo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for mapping Excel columns to Java fields
 * 
 * Usage example:
 * <pre>
 * public class Person {
 *     @ExcelColumn("ID")
 *     private int id;
 *     
 *     @ExcelColumn("Name")
 *     private String name;
 *     
 *     @ExcelColumn(value = "Age", required = false)
 *     private Integer age;
 * }
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelColumn {
    
    /**
     * Excel column name (case-sensitive)
     */
    String value();
    
    /**
     * Whether field is required (default: true)
     * If false, no error occurs even if column is missing
     */
    boolean required() default true;
    
    /**
     * Default value (used when column is missing or empty)
     */
    String defaultValue() default "";
}
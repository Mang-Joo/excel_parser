package io.github.mangjoo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Combined annotation for both read and write Excel operations.
 * Use this when the same field configuration works for both operations.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelReadWrite {
    /**
     * Column name for both read and write operations.
     */
    String value();
    
    // Read-specific properties
    boolean required() default true;
    String defaultValue() default "";
    String[] readAliases() default {};
    
    // Write-specific properties
    int writeOrder() default Integer.MAX_VALUE;
    String writeFormat() default "";
    int columnWidth() default -1;
    
    // Common properties
    boolean skip() default false;
}
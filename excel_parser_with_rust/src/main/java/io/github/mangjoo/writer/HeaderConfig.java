package io.github.mangjoo.writer;

/**
 * Configuration for Excel column headers.
 */
public class HeaderConfig {
    private String name;
    private Double width;  // Optional column width
    private String format; // Optional cell format
    
    public HeaderConfig() {}
    
    public HeaderConfig(String name) {
        this.name = name;
    }
    
    public HeaderConfig(String name, Double width, String format) {
        this.name = name;
        this.width = width;
        this.format = format;
    }
    
    // Builder pattern for easier configuration
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String name;
        private Double width;
        private String format;
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder width(Double width) {
            this.width = width;
            return this;
        }
        
        public Builder format(String format) {
            this.format = format;
            return this;
        }
        
        public HeaderConfig build() {
            return new HeaderConfig(name, width, format);
        }
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Double getWidth() {
        return width;
    }
    
    public void setWidth(Double width) {
        this.width = width;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
}
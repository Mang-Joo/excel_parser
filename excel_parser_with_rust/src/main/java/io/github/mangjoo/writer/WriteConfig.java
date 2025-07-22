package io.github.mangjoo.writer;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Configuration for writing data to Excel.
 * This class represents the structure needed to write an Excel sheet.
 */
public class WriteConfig {
    @JsonProperty("sheet_name")
    private String sheetName;
    
    @JsonProperty("headers")
    private List<HeaderConfig> headers;
    
    @JsonProperty("data")
    private List<List<String>> data;
    
    public WriteConfig() {
        this.sheetName = "Sheet1";
    }
    
    public WriteConfig(String sheetName, List<HeaderConfig> headers, List<List<String>> data) {
        this.sheetName = sheetName;
        this.headers = headers;
        this.data = data;
    }
    
    // Getters and setters
    public String getSheetName() {
        return sheetName;
    }
    
    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }
    
    public List<HeaderConfig> getHeaders() {
        return headers;
    }
    
    public void setHeaders(List<HeaderConfig> headers) {
        this.headers = headers;
    }
    
    public List<List<String>> getData() {
        return data;
    }
    
    public void setData(List<List<String>> data) {
        this.data = data;
    }
}
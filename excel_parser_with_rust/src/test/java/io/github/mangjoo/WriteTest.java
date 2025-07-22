package io.github.mangjoo;

import io.github.mangjoo.annotations.ExcelWriteColumn;
import io.github.mangjoo.writer.WriteConfig;
import io.github.mangjoo.writer.HeaderConfig;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class WriteTest {
    
    public static class TestData {
        @ExcelWriteColumn(value = "ID", order = 1)
        private String id;
        
        @ExcelWriteColumn(value = "Name", order = 2)
        private String name;
        
        @ExcelWriteColumn(value = "Value", order = 3)
        private Integer value;
        
        public TestData(String id, String name, Integer value) {
            this.id = id;
            this.name = name;
            this.value = value;
        }
        
        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public Integer getValue() { return value; }
    }
    
    @Test
    public void testWriteExcel() {
        try {
            ExcelParser parser = new ExcelParser();
            
            List<TestData> data = Arrays.asList(
                new TestData("1", "Test1", 100),
                new TestData("2", "Test2", 200)
            );
            
            System.out.println("Writing Excel file...");
            
            // Debug: Check the WriteConfig
            WriteConfig config = parser.prepareWriteConfig(data, TestData.class, "TestSheet");
            System.out.println("Headers: " + config.getHeaders().size());
            for (HeaderConfig h : config.getHeaders()) {
                System.out.println("  - " + h.getName() + " (width: " + h.getWidth() + ")");
            }
            System.out.println("Data rows: " + config.getData().size());
            for (List<String> row : config.getData()) {
                System.out.println("  Row: " + row);
            }
            
            boolean result = parser.writeExcelWithConfig("test_output.xlsx", config);
            System.out.println("Write result: " + result);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
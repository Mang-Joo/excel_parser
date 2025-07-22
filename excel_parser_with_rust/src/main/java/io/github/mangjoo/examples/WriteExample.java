package io.github.mangjoo.examples;

import io.github.mangjoo.ExcelParser;
import io.github.mangjoo.annotations.ExcelWriteColumn;
import io.github.mangjoo.annotations.ExcelReadWrite;
import io.github.mangjoo.writer.WriteConfig;
import io.github.mangjoo.writer.HeaderConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Example demonstrating Excel write operations.
 */
public class WriteExample {
    
    /**
     * Example class with write-only annotations
     */
    public static class Product {
        @ExcelWriteColumn(value = "Product ID", order = 1, width = 15)
        private String id;
        
        @ExcelWriteColumn(value = "Product Name", order = 2, width = 30)
        private String name;
        
        @ExcelWriteColumn(value = "Price", order = 3, format = "#,##0.00", width = 15)
        private Double price;
        
        @ExcelWriteColumn(value = "In Stock", order = 4, width = 10)
        private Boolean inStock;
        
        @ExcelWriteColumn(value = "Category", order = 5, width = 20)
        private String category;
        
        // Constructor
        public Product(String id, String name, Double price, Boolean inStock, String category) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.inStock = inStock;
            this.category = category;
        }
        
        // Getters and setters (omitted for brevity)
    }
    
    /**
     * Example class with read/write annotations
     */
    public static class Customer {
        @ExcelReadWrite(value = "Customer ID", writeOrder = 1, columnWidth = 15)
        private String customerId;
        
        @ExcelReadWrite(value = "Name", writeOrder = 2, columnWidth = 25)
        private String name;
        
        @ExcelReadWrite(value = "Email", writeOrder = 3, columnWidth = 30)
        private String email;
        
        @ExcelReadWrite(value = "Join Date", writeOrder = 4, writeFormat = "yyyy-MM-dd", columnWidth = 15)
        private String joinDate;
        
        @ExcelReadWrite(value = "Total Purchases", writeOrder = 5, writeFormat = "#,##0.00", columnWidth = 20)
        private Double totalPurchases;
        
        // Constructor
        public Customer(String customerId, String name, String email, String joinDate, Double totalPurchases) {
            this.customerId = customerId;
            this.name = name;
            this.email = email;
            this.joinDate = joinDate;
            this.totalPurchases = totalPurchases;
        }
    }
    
    public static void main(String[] args) {
        try {
            ExcelParser parser = new ExcelParser();
            
            // Example 1: Write products to Excel
            writeProductsExample(parser);
            
            // Example 2: Write customers to Excel
            writeCustomersExample(parser);
            
            // Example 3: Write multiple sheets
            writeMultipleSheetsExample(parser);
            
            // Example 4: Manual configuration
            manualConfigurationExample(parser);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void writeProductsExample(ExcelParser parser) throws IOException {
        System.out.println("Writing products to Excel...");
        
        List<Product> products = Arrays.asList(
            new Product("P001", "Laptop", 999.99, true, "Electronics"),
            new Product("P002", "Mouse", 29.99, true, "Electronics"),
            new Product("P003", "Keyboard", 79.99, false, "Electronics"),
            new Product("P004", "Monitor", 299.99, true, "Electronics"),
            new Product("P005", "Desk Chair", 199.99, true, "Furniture")
        );
        
        boolean success = parser.writeExcel("products.xlsx", products, Product.class, "Products");
        System.out.println("Products written successfully: " + success);
    }
    
    private static void writeCustomersExample(ExcelParser parser) throws IOException {
        System.out.println("Writing customers to Excel...");
        
        List<Customer> customers = Arrays.asList(
            new Customer("C001", "John Doe", "john@example.com", "2023-01-15", 1500.50),
            new Customer("C002", "Jane Smith", "jane@example.com", "2023-02-20", 2300.75),
            new Customer("C003", "Bob Johnson", "bob@example.com", "2023-03-10", 890.00),
            new Customer("C004", "Alice Brown", "alice@example.com", "2023-04-05", 3200.25)
        );
        
        boolean success = parser.writeExcel("customers.xlsx", customers, Customer.class);
        System.out.println("Customers written successfully: " + success);
    }
    
    private static void writeMultipleSheetsExample(ExcelParser parser) throws IOException {
        System.out.println("Writing multiple sheets to Excel...");
        
        // Prepare products data
        List<Product> products = Arrays.asList(
            new Product("P001", "Laptop", 999.99, true, "Electronics"),
            new Product("P002", "Mouse", 29.99, true, "Electronics")
        );
        WriteConfig productsConfig = parser.prepareWriteConfig(products, Product.class, "Products");
        
        // Prepare customers data
        List<Customer> customers = Arrays.asList(
            new Customer("C001", "John Doe", "john@example.com", "2023-01-15", 1500.50),
            new Customer("C002", "Jane Smith", "jane@example.com", "2023-02-20", 2300.75)
        );
        WriteConfig customersConfig = parser.prepareWriteConfig(customers, Customer.class, "Customers");
        
        // Write both sheets to one file
        List<WriteConfig> configs = Arrays.asList(productsConfig, customersConfig);
        boolean success = parser.writeMultipleSheetsWithConfig("multi_sheet.xlsx", configs);
        System.out.println("Multiple sheets written successfully: " + success);
    }
    
    private static void manualConfigurationExample(ExcelParser parser) throws IOException {
        System.out.println("Manual configuration example...");
        
        // Create headers manually
        List<HeaderConfig> headers = Arrays.asList(
            HeaderConfig.builder().name("ID").width(10.0).build(),
            HeaderConfig.builder().name("Name").width(25.0).build(),
            HeaderConfig.builder().name("Amount").width(15.0).format("#,##0.00").build(),
            HeaderConfig.builder().name("Date").width(15.0).format("yyyy-MM-dd").build()
        );
        
        // Create data manually
        List<List<String>> data = Arrays.asList(
            Arrays.asList("1", "Transaction A", "1000.50", "2024-01-15"),
            Arrays.asList("2", "Transaction B", "2500.75", "2024-01-16"),
            Arrays.asList("3", "Transaction C", "750.00", "2024-01-17")
        );
        
        // Create write config
        WriteConfig config = new WriteConfig("Transactions", headers, data);
        
        // Write to Excel
        boolean success = parser.writeExcelWithConfig("manual_config.xlsx", config);
        System.out.println("Manual configuration written successfully: " + success);
    }
}
# Excel Parser with Rust (Java Library)

🚀 **High-performance Excel reading and writing library for Java, powered by Rust**

This Java library provides a simple and efficient way to read and write Excel files using a high-performance Rust backend. It supports both raw data access and annotation-based object mapping with separate annotations for read and write operations.

## ✨ Features

- ⚡ **High Performance**: Rust native backend for fast Excel operations
- 📖 **Excel Reading**: Parse Excel files with dynamic column recognition
- ✏️ **Excel Writing**: Create Excel files from Java objects
- 🎯 **Flexible Annotations**: Separate annotations for read, write, or both
- 🔧 **Two Usage Modes**: Raw data access and annotation-based mapping
- 📦 **Easy Integration**: Standard Java library with automatic native library loading
- 🌍 **Cross-Platform**: Works on Windows, macOS, and Linux
- 💾 **Memory Efficient**: Optimized with MessagePack serialization

## 📥 Installation

### Maven
```xml
<dependency>
    <groupId>io.github.mangjoo</groupId>
    <artifactId>excel_parser_with_rust</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle
```gradle
implementation 'io.github.mangjoo:excel_parser_with_rust:0.1.0'
```

## 🚀 Quick Start

### Basic Usage (Raw Data)

```java
import io.github.mangjoo.ExcelParser;
import io.github.mangjoo.ExcelData;
import io.github.mangjoo.SheetData;

// Create parser
ExcelParser parser = new ExcelParser();

// Parse Excel file
ExcelData data = parser.parseExcel("example.xlsx");

// Get first sheet
SheetData sheet = data.getFirstSheet();

// Convert to Map format (most convenient)
List<Map<String, String>> rows = sheet.getAllRowsAsMap();

for (Map<String, String> row : rows) {
    System.out.println("ID: " + row.get("ID"));
    System.out.println("Name: " + row.get("Name"));
    System.out.println("Age: " + row.get("Age"));
}
```

### Reading Excel Files with Annotations

#### 1. Define your POJO with read annotations

```java
import io.github.mangjoo.annotations.ExcelReadColumn;
import io.github.mangjoo.annotations.ExcelColumn; // Legacy, still supported

public class Person {
    @ExcelReadColumn(value = "ID", aliases = {"PersonID", "EmployeeID"})
    private int id;
    
    @ExcelReadColumn("Name")
    private String name;
    
    @ExcelReadColumn(value = "Age", required = false, defaultValue = "0")
    private Integer age;
    
    // Default constructor required
    public Person() {}
    
    // Getters and setters...
}
```

#### 2. Parse directly to objects

```java
ExcelParser parser = new ExcelParser();

// Parse Excel file directly to List<Person>
List<Person> people = parser.parseToList("example.xlsx", Person.class);

for (Person person : people) {
    System.out.println(person.getName() + " is " + person.getAge() + " years old");
}
```

### Writing Excel Files with Annotations

#### 1. Define your POJO with write annotations

```java
import io.github.mangjoo.annotations.ExcelWriteColumn;

public class Product {
    @ExcelWriteColumn(value = "Product ID", order = 1, width = 15)
    private String productId;
    
    @ExcelWriteColumn(value = "Product Name", order = 2, width = 30)
    private String name;
    
    @ExcelWriteColumn(value = "Price", order = 3, format = "$#,##0.00", width = 15)
    private Double price;
    
    @ExcelWriteColumn(value = "In Stock", order = 4, skip = false)
    private Boolean inStock;
    
    // Constructor, getters, setters...
}
```

#### 2. Write objects to Excel

```java
ExcelParser parser = new ExcelParser();

List<Product> products = // ... your product list

// Write to Excel file
boolean success = parser.writeExcel("products.xlsx", products, Product.class);

// Write with custom sheet name
boolean success = parser.writeExcel("products.xlsx", products, Product.class, "Products");
```

### Combined Read/Write Operations

```java
import io.github.mangjoo.annotations.ExcelReadColumn;
import io.github.mangjoo.annotations.ExcelWriteColumn;
import io.github.mangjoo.annotations.ExcelReadWrite;

public class Customer {
    // Different column names for read and write
    @ExcelReadColumn(value = "CustomerID", aliases = {"CustID", "ID"})
    @ExcelWriteColumn(value = "Customer ID", order = 1, width = 15)
    private String customerId;
    
    // Same configuration for both read and write
    @ExcelReadWrite(value = "Name", writeOrder = 2, columnWidth = 30,
                    readAliases = {"CustomerName", "FullName"})
    private String name;
    
    // Read as one format, write as another
    @ExcelReadColumn(value = "Balance", required = false, defaultValue = "0")
    @ExcelWriteColumn(value = "Account Balance", order = 3, format = "$#,##0.00")
    private Double balance;
    
    // Constructor, getters, setters...
}

// Read from Excel
List<Customer> customers = parser.parseToList("customers.xlsx", Customer.class);

// Modify data...

// Write back to Excel
parser.writeExcel("customers_updated.xlsx", customers, Customer.class);
```

## 📋 Advanced Usage

### Reading Headers Only
```java
List<String> headers = parser.getColumnHeaders("example.xlsx");
System.out.println("Columns: " + headers);
```

### Parsing Specific Sheet
```java
SheetData sheet = parser.parseSheet("example.xlsx", "Sheet2");
```

### Complex Data Types
```java
public class Employee {
    @ExcelColumn("EmployeeID")
    private Long employeeId;
    
    @ExcelColumn("Salary")
    private BigDecimal salary;
    
    @ExcelColumn("IsActive")
    private Boolean isActive;
    
    @ExcelColumn(value = "Department", required = false, defaultValue = "General")
    private String department;
}
```

## 🎯 Annotation Reference

### @ExcelReadColumn
For reading data from Excel:
| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `value` | String | - | Primary column name to read from |
| `required` | boolean | true | Whether the field is required |
| `defaultValue` | String | "" | Default value when column is missing or empty |
| `aliases` | String[] | {} | Alternative column names to try |

### @ExcelWriteColumn
For writing data to Excel:
| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `value` | String | - | Column header name in the output Excel |
| `order` | int | Integer.MAX_VALUE | Column position (lower values appear first) |
| `format` | String | "" | Cell format pattern (e.g., "#,##0.00", "yyyy-MM-dd") |
| `skip` | boolean | false | Whether to skip this field during writing |
| `width` | int | -1 | Column width in characters (-1 for auto-size) |

### @ExcelReadWrite
For both read and write operations:
| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `value` | String | - | Column name for both operations |
| `required` | boolean | true | Whether required for reading |
| `defaultValue` | String | "" | Default value for reading |
| `readAliases` | String[] | {} | Alternative names for reading |
| `writeOrder` | int | Integer.MAX_VALUE | Column position for writing |
| `writeFormat` | String | "" | Cell format for writing |
| `columnWidth` | int | -1 | Column width for writing |
| `skip` | boolean | false | Skip this field entirely |

### @ExcelColumn (Legacy)
Still supported for backward compatibility (read-only):
| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `value` | String | - | Excel column name (case-sensitive) |
| `required` | boolean | true | Whether the field is required |
| `defaultValue` | String | "" | Default value when column is missing or empty |

## 📊 Supported Data Types

- `String`
- `int`, `Integer`
- `long`, `Long`
- `double`, `Double`
- `float`, `Float`
- `boolean`, `Boolean`
- `BigDecimal`

Boolean values are parsed flexibly: "true", "1", "Y", "YES" (case-insensitive) are treated as `true`.

## ✏️ Writing Features

### Multiple Sheets
```java
// Create configurations for multiple sheets
WriteConfig sheet1 = parser.prepareWriteConfig(products, Product.class, "Products");
WriteConfig sheet2 = parser.prepareWriteConfig(customers, Customer.class, "Customers");

// Write all sheets to one file
List<WriteConfig> configs = Arrays.asList(sheet1, sheet2);
parser.writeMultipleSheetsWithConfig("multi_sheet.xlsx", configs);
```

### Manual Configuration
```java
// Create headers manually
List<HeaderConfig> headers = Arrays.asList(
    HeaderConfig.builder().name("ID").width(10).build(),
    HeaderConfig.builder().name("Amount").width(15).format("#,##0.00").build()
);

// Create data
List<List<String>> data = Arrays.asList(
    Arrays.asList("1", "1000.50"),
    Arrays.asList("2", "2500.00")
);

// Write to Excel
WriteConfig config = new WriteConfig("Transactions", headers, data);
parser.writeExcelWithConfig("manual.xlsx", config);
```

## 🔧 Requirements

- **Java 8+**
- **Supported Platforms**: 
  - Windows (x64)
  - macOS (Intel x64 / Apple Silicon ARM64)
  - Linux (x64 / ARM64)

## 📁 Examples

Complete examples are available in the `examples` package:

- `RawDataExample.java` - Basic raw data parsing
- `AnnotationExample.java` - Annotation-based object mapping for reading
- `WriteExample.java` - Excel writing demonstrations
- `ReadWriteExample.java` - Combined read/write operations with different annotations
- `Person.java` - Simple POJO example
- `Employee.java` - Complex data types example

## 🐛 Error Handling

```java
try {
    List<Person> people = parser.parseToList("example.xlsx", Person.class);
} catch (IOException e) {
    System.err.println("Failed to read Excel file: " + e.getMessage());
} catch (ExcelMapper.ExcelMappingException e) {
    System.err.println("Failed to map data: " + e.getMessage());
}
```

Common errors:
- `ExcelMappingException`: Column mapping issues (missing required columns, type conversion failures)
- `IOException`: File access issues

## 🔍 Performance

This library uses a Rust backend with MessagePack serialization for optimal performance:

- **4.4x smaller** data size compared to JSON
- **Native Rust speed** for Excel parsing and writing
- **Minimal memory usage** with streaming approach
- **Efficient JNI bridge** with minimal data copying

## 🏗️ Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Java API      │────▶│   JNI Bridge    │────▶│   Rust Core     │
│  (Annotations)  │◀────│  (MessagePack)  │◀────│   (Calamine)    │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

## 📄 License

MIT License

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 🐞 Bug Reports

Please report bugs at: https://github.com/mangjoo/excel-parser-rust/issues

## 📚 Version History

- **0.1.0** (2025-01-22)
  - Initial release
  - Excel reading with annotation mapping
  - Excel writing with separate write annotations
  - Cross-platform support
  - MessagePack serialization
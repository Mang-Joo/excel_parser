# Excel Parser with Rust (Java Library)

🚀 **High-performance Excel parsing library for Java, powered by Rust**

This Java library provides a simple and efficient way to parse Excel files using a high-performance Rust backend. It supports both raw data access and annotation-based object mapping.

## ✨ Features

- ⚡ **High Performance**: Rust native backend for fast Excel parsing
- 🎯 **Dynamic Columns**: Automatic header recognition from first row
- 🔧 **Two Usage Modes**: Raw data access and annotation-based mapping
- 📦 **Easy Integration**: Standard Java library with automatic native library loading
- 🌍 **Cross-Platform**: Works on Windows, macOS, and Linux
- 💾 **Memory Efficient**: Optimized with MessagePack serialization

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

### Annotation-Based Mapping

#### 1. Define your POJO with annotations

```java
import io.github.mangjoo.annotations.ExcelColumn;

public class Person {
    @ExcelColumn("ID")
    private int id;
    
    @ExcelColumn("Name")
    private String name;
    
    @ExcelColumn(value = "Age", required = false)
    private Integer age;
    
    // Default constructor required
    public Person() {}
    
    // Getters and setters...
}
```

#### 2. Parse directly to objects

```java
import io.github.mangjoo.ExcelParser;

ExcelParser parser = new ExcelParser();

// Parse Excel file directly to List<Person>
List<Person> people = parser.parseToList("example.xlsx", Person.class);

for (Person person : people) {
    System.out.println(person.getName() + " is " + person.getAge() + " years old");
}
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

## 🎯 Annotation Options

The `@ExcelColumn` annotation supports:

- **value**: Excel column name (case-sensitive)
- **required**: Whether the field is required (default: true)
- **defaultValue**: Default value when column is missing or empty

## 📊 Supported Data Types

- `String`
- `int`, `Integer`
- `long`, `Long`
- `double`, `Double`
- `float`, `Float`
- `boolean`, `Boolean`
- `BigDecimal`

## 🔧 Requirements

- **Java 8+**
- **Supported Platforms**: Windows (x64), macOS (Intel/ARM), Linux (x64/ARM)

## 📁 Examples

Complete examples are available in the `examples` package:

- `RawDataExample.java` - Basic raw data parsing
- `AnnotationExample.java` - Annotation-based mapping
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

## 🔍 Performance

This library uses a Rust backend with MessagePack serialization for optimal performance:

- **4.4x smaller** data size compared to JSON
- **Native Rust speed** for Excel parsing
- **Minimal memory usage** with streaming approach

## 📄 License

MIT License

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

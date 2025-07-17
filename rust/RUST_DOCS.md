# Rust Excel Parser - Developer Documentation

🦀 **Detailed Documentation for Rust Developers**

## 📖 Table of Contents

1. [Architecture Overview](#-architecture-overview)
2. [Module Structure](#-module-structure)
3. [Core Components](#-core-components)
4. [JNI Interface](#-jni-interface)
5. [Performance Optimization](#-performance-optimization)
6. [Error Handling](#-error-handling)
7. [Testing](#-testing)
8. [Extension Guide](#-extension-guide)

## 🏗️ Architecture Overview

### Overall Structure
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Java Layer    │    │   JNI Bridge    │    │   Rust Core     │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ ExcelParser     │────│ jni_wrapper.rs  │────│ parser.rs       │
│ ExcelData       │    │                 │    │ messagepack_    │
│ SheetData       │    │ Native Methods  │    │ converter.rs    │
│ AnnotationMapper│    │                 │    │ calamine        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Data Flow
```
Excel File → Calamine → Rust Structs → MessagePack → JNI ByteArray → Java Objects
```

## 📁 Module Structure

### `src/lib.rs`
Library entry point and module declarations
```rust
//! # Rust Excel Parser
//! 
//! High-performance Excel parsing library
//! Available for use in Java via JNI

pub mod parser;
pub mod messagepack_converter;
pub mod jni_wrapper;

// Re-export for common usage
pub use parser::{ExcelParser, ExcelData, SheetData, RowData, CellData};
pub use messagepack_converter::MessagePackConverter;
```

### `src/parser.rs`
Core Excel parsing logic

**Main Responsibilities:**
- Reading Excel files (using Calamine)
- Recognizing first row as headers
- Dynamic column mapping
- Data structure conversion

**Core Functions:**
```rust
impl ExcelParser {
    /// Create new parser instance
    pub fn new(file_path: String) -> Self
    
    /// Read complete Excel data
    pub fn read_data(&self) -> Result<ExcelData, Box<dyn Error>>
    
    /// Quickly read headers only
    pub fn read_headers(&self) -> Result<Vec<String>, Box<dyn Error>>
}
```

### `src/messagepack_converter.rs`
MessagePack serialization/deserialization handler

**Reason for Use:**
- 4.4x smaller size compared to JSON
- Type information preservation
- Fast serialization/deserialization

**Core Functions:**
```rust
impl MessagePackConverter {
    /// Convert ExcelData to MessagePack bytes
    pub fn to_bytes(data: &ExcelData) -> Result<Vec<u8>, rmp_serde::encode::Error>
    
    /// Convert header array to MessagePack bytes
    pub fn headers_to_bytes(headers: &[String]) -> Result<Vec<u8>, rmp_serde::encode::Error>
    
    /// Convert single sheet to MessagePack bytes
    pub fn sheet_to_bytes(sheet: &SheetData) -> Result<Vec<u8>, rmp_serde::encode::Error>
}
```

### `src/jni_wrapper.rs`
JNI bridge between Java and Rust

**Core Functions:**
```rust
// Java method: getHeaders()
#[no_mangle]
pub extern "system" fn Java_io_github_mangjoo_ExcelParser_getHeaders<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    file_path: JString<'local>,
) -> jbyteArray

// Java method: readExcel()  
#[no_mangle]
pub extern "system" fn Java_io_github_mangjoo_ExcelParser_readExcel<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    file_path: JString<'local>,
) -> jbyteArray

// Java method: readSheet()
#[no_mangle]
pub extern "system" fn Java_io_github_mangjoo_ExcelParser_readSheet<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    file_path: JString<'local>,
    sheet_name: JString<'local>,
) -> jbyteArray
```

## 🔧 Core Components

### Data Structures

#### ExcelData
```rust
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ExcelData {
    /// All sheet data
    pub sheets: Vec<SheetData>,
}

impl ExcelData {
    /// Return first sheet
    pub fn first_sheet(&self) -> Option<&SheetData> {
        self.sheets.first()
    }
    
    /// Find sheet by name
    pub fn find_sheet(&self, name: &str) -> Option<&SheetData> {
        self.sheets.iter().find(|s| s.name == name)
    }
}
```

#### SheetData
```rust
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SheetData {
    /// Sheet name
    pub name: String,
    /// Column name array (first row)
    pub column_names: Vec<String>,
    /// Actual data rows
    pub rows: Vec<RowData>,
    /// Total row count (including header)
    pub total_rows: usize,
    /// Total column count
    pub total_columns: usize,
}
```

#### RowData & CellData
```rust
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct RowData {
    /// All cell data in this row
    pub cells: Vec<CellData>,
    /// Row index in original Excel (1-based)
    pub row_index: usize,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CellData {
    /// Column name (from header)
    pub column_name: String,
    /// Cell value (converted to string)
    pub value: String,
}
```

### Excel Parsing Logic

#### Core Algorithm
```rust
fn parse_worksheet(&self, worksheet: &Range<DataType>) -> Result<SheetData, Box<dyn Error>> {
    // 1. Extract first row as headers
    let headers = self.extract_headers(worksheet)?;
    
    // 2. Process data rows (from second row)
    let mut rows = Vec::new();
    for (row_idx, row) in worksheet.rows().skip(1).enumerate() {
        let mut cells = Vec::new();
        
        // 3. Map each cell to column name
        for (col_idx, cell) in row.iter().enumerate() {
            if col_idx < headers.len() {
                let cell_data = CellData {
                    column_name: headers[col_idx].clone(),
                    value: self.cell_to_string(cell),
                };
                cells.push(cell_data);
            }
        }
        
        rows.push(RowData {
            cells,
            row_index: row_idx + 2, // 1-based + skip header
        });
    }
    
    Ok(SheetData {
        name: "Sheet".to_string(),
        column_names: headers,
        rows,
        total_rows: worksheet.height(),
        total_columns: worksheet.width(),
    })
}
```

## 🌉 JNI Interface

### Function Naming Convention
```
Java_<package_name>_<class_name>_<method_name>
├─ Java_: Required JNI prefix
├─ io_github_mangjoo: Package name (dots to underscores)
├─ ExcelParser: Class name
└─ readExcel: Method name
```

### Data Conversion Process

#### Rust → Java
```rust
// 1. Create Rust struct
let excel_data = ExcelData { sheets: vec![...] };

// 2. Serialize to MessagePack
let bytes = MessagePackConverter::to_bytes(&excel_data)?;

// 3. Convert to Java byte[] array
let java_array = bytes_to_java_array(&mut env, bytes);

// 4. Return to Java
java_array
```

#### Java → Rust
```java
// 1. Call JNI from Java
byte[] result = readExcel(filePath);

// 2. Deserialize MessagePack
Object rawData = msgpackMapper.readValue(result, Object.class);

// 3. Convert to Java objects
ExcelData excelData = parseRawExcelData(rawData);
```

### Error Handling Pattern
```rust
pub extern "system" fn Java_io_github_mangjoo_ExcelParser_readExcel<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    file_path: JString<'local>,
) -> jbyteArray {
    // 1. Validate input parameters
    let path = match get_string_from_java(&mut env, &file_path) {
        Some(p) => p,
        None => return std::ptr::null_mut(), // Return null for error
    };

    // 2. Execute core logic
    let parser = ExcelParser::new(path);
    match parser.read_data() {
        Ok(data) => {
            // 3. Serialize and return on success
            match MessagePackConverter::to_bytes(&data) {
                Ok(bytes) => bytes_to_java_array(&mut env, bytes),
                Err(_) => std::ptr::null_mut(),
            }
        }
        Err(_) => std::ptr::null_mut(), // Return null on error
    }
}
```

## ⚡ Performance Optimization

### Memory Management
```rust
// Minimize string copying
pub fn cell_to_string(&self, cell: &DataType) -> String {
    match cell {
        DataType::String(s) => s.clone(),           // Copy only when necessary
        DataType::Float(f) => f.to_string(),        // Convert numbers to string
        DataType::Int(i) => i.to_string(),
        DataType::Bool(b) => b.to_string(),
        DataType::DateTime(dt) => format!("{}", dt),
        DataType::Empty => String::new(),           // Empty string
        _ => "".to_string(),
    }
}
```

### MessagePack Optimization
```rust
// Optimize struct field order (frequently used fields first)
#[derive(Serialize, Deserialize)]
pub struct SheetData {
    pub name: String,        // Most frequently accessed
    pub column_names: Vec<String>,
    pub rows: Vec<RowData>,  // Largest data
    pub total_rows: usize,   // Metadata
    pub total_columns: usize,
}
```

### Streaming Processing
```rust
// Calamine uses streaming internally
// Doesn't load entire file into memory, reads only necessary parts
let mut workbook: Xlsx<BufReader<File>> = open_workbook(&self.file_path)?;
```

## 🛡️ Error Handling

### Error Type Hierarchy
```rust
use std::error::Error;

// Unify all errors with Box<dyn Error>
pub type ExcelError = Box<dyn Error>;

// Main error cases
impl ExcelParser {
    pub fn read_data(&self) -> Result<ExcelData, ExcelError> {
        // 1. File access error
        let mut workbook = open_workbook(&self.file_path)
            .map_err(|e| format!("Cannot open file: {}", e))?;
        
        // 2. Sheet access error  
        let worksheet = workbook.worksheet_range_at(0)
            .ok_or("Cannot find sheet")?
            .map_err(|e| format!("Cannot read sheet: {}", e))?;
        
        // 3. Data parsing error
        let sheet_data = self.parse_worksheet(&worksheet)
            .map_err(|e| format!("Data parsing failed: {}", e))?;
        
        Ok(ExcelData {
            sheets: vec![sheet_data],
        })
    }
}
```

### JNI Error Handling
```rust
// Signal error by returning null
pub extern "system" fn Java_io_github_mangjoo_ExcelParser_readExcel<'local>(
    // ...
) -> jbyteArray {
    // Return null on error
    // Converted to IOException on Java side
    match parser.read_data() {
        Ok(data) => { /* Normal processing */ },
        Err(e) => {
            eprintln!("Rust error: {}", e); // Debug log
            return std::ptr::null_mut();
        }
    }
}
```

## 🧪 Testing

### Unit Tests
```rust
#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_excel_parsing() {
        let parser = ExcelParser::new("test_data/sample.xlsx".to_string());
        let result = parser.read_data();
        
        assert!(result.is_ok());
        let data = result.unwrap();
        assert!(!data.sheets.is_empty());
        
        let first_sheet = &data.sheets[0];
        assert_eq!(first_sheet.column_names, vec!["ID", "Name", "Age"]);
    }

    #[test]
    fn test_messagepack_conversion() {
        let test_data = ExcelData {
            sheets: vec![SheetData {
                name: "Test".to_string(),
                column_names: vec!["A".to_string(), "B".to_string()],
                rows: vec![],
                total_rows: 1,
                total_columns: 2,
            }],
        };

        let bytes = MessagePackConverter::to_bytes(&test_data);
        assert!(bytes.is_ok());
        assert!(!bytes.unwrap().is_empty());
    }
}
```

### Benchmark Tests
```rust
#[cfg(test)]
mod benches {
    use super::*;
    use std::time::Instant;

    #[test]
    fn bench_large_file_parsing() {
        let start = Instant::now();
        
        let parser = ExcelParser::new("test_data/large_file.xlsx".to_string());
        let _result = parser.read_data().unwrap();
        
        let duration = start.elapsed();
        println!("Large file parsing took: {:?}", duration);
        
        // Must complete within 5 seconds
        assert!(duration.as_secs() < 5);
    }
}
```

## 🚀 Extension Guide

### Adding New Excel Features

#### 1. Extend Data Structures
```rust
// Add fields for new features
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CellData {
    pub column_name: String,
    pub value: String,
    // New feature: cell style information
    pub style: Option<CellStyle>,
    // New feature: data type information
    pub data_type: CellDataType,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum CellDataType {
    Text,
    Number,
    Date,
    Boolean,
    Formula,
}
```

#### 2. Extend Parsing Logic
```rust
impl ExcelParser {
    // New feature: include formula calculation results
    pub fn read_data_with_formulas(&self) -> Result<ExcelData, ExcelError> {
        // Existing logic + formula processing
    }
    
    // New feature: read specific range only
    pub fn read_range(&self, range: &str) -> Result<SheetData, ExcelError> {
        // Parse range specification like A1:C10
    }
}
```

#### 3. Extend JNI Interface
```rust
// JNI function for new Java method
#[no_mangle]
pub extern "system" fn Java_io_github_mangjoo_ExcelParser_readRange<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    file_path: JString<'local>,
    range: JString<'local>,
) -> jbyteArray {
    // Implementation...
}
```

### Supporting New Platforms

#### 1. Add Target
```bash
# Install new platform target
rustup target add riscv64gc-unknown-linux-gnu

# Install cross-compilation tools
# (varies by platform)
```

#### 2. Update Build Scripts
```bash
# Add new target to build_all.sh
TARGETS=(
    "x86_64-apple-darwin"
    "aarch64-apple-darwin"
    "riscv64gc-unknown-linux-gnu"  # Newly added
)
```

#### 3. Update Java Library Loader
```java
// Add new platform to NativeLibraryLoader.java
private static String getLibraryName(String osName, String osArch) {
    // ...
    if (osArch.equals("riscv64")) {
        return "librust_excel_parser-riscv64gc-unknown-linux-gnu.so";
    }
    // ...
}
```

## 📚 References

### Core Dependencies
- **[Calamine](https://docs.rs/calamine/)**: Excel file parsing
- **[serde](https://docs.rs/serde/)**: Serialization/deserialization framework  
- **[rmp-serde](https://docs.rs/rmp-serde/)**: MessagePack implementation
- **[jni](https://docs.rs/jni/)**: Java Native Interface bindings

### Development Tools
```bash
# Generate documentation
cargo doc --open

# Dependency analysis
cargo tree

# Security vulnerability check
cargo audit

# Code formatting
cargo fmt

# Static analysis
cargo clippy
```

---

📝 This document explains the internal structure and extension methods of the Rust Excel Parser.  
Please refer to the source code comments in each module for more details.
# Rust Excel Parser - API Reference

🔧 **Rust API Detailed Reference Documentation**

## 📋 Table of Contents

1. [Module Overview](#-module-overview)
2. [Parser API](#-parser-api)
3. [Data Structures](#-data-structures)
4. [MessagePack Converter](#-messagepack-converter)
5. [JNI Interface](#-jni-interface)
6. [Error Types](#-error-types)
7. [Usage Examples](#-usage-examples)

## 📦 Module Overview

### Public Modules
```rust
pub mod parser;           // Core Excel parsing logic
pub mod messagepack_converter;  // MessagePack serialization
pub mod jni_wrapper;      // Java Native Interface
```

### Re-exports
```rust
pub use parser::{ExcelParser, ExcelData, SheetData, RowData, CellData};
pub use messagepack_converter::MessagePackConverter;
```

## 🔍 Parser API

### `ExcelParser`

#### Struct Definition
```rust
pub struct ExcelParser {
    file_path: String,
}
```

#### Constructor
```rust
impl ExcelParser {
    /// Creates a new Excel parser instance.
    ///
    /// # Arguments
    /// * `file_path` - Path to the Excel file to parse
    ///
    /// # Example
    /// ```rust
    /// use rust_excel_parser::ExcelParser;
    /// 
    /// let parser = ExcelParser::new("data.xlsx".to_string());
    /// ```
    pub fn new(file_path: String) -> Self
}
```

#### Core Methods

##### `read_data()`
```rust
/// Parses the entire Excel file and returns all sheet data.
///
/// # Returns
/// * `Ok(ExcelData)` - Complete Excel data on success
/// * `Err(Box<dyn Error>)` - Error information on failure
///
/// # Errors
/// * When unable to open the file
/// * When Excel file format is invalid
/// * When out of memory
///
/// # Example
/// ```rust
/// use rust_excel_parser::ExcelParser;
/// 
/// let parser = ExcelParser::new("example.xlsx".to_string());
/// match parser.read_data() {
///     Ok(data) => {
///         println!("Number of sheets: {}", data.sheets.len());
///         for sheet in &data.sheets {
///             println!("Sheet name: {}", sheet.name);
///         }
///     }
///     Err(e) => eprintln!("Error: {}", e),
/// }
/// ```
pub fn read_data(&self) -> Result<ExcelData, Box<dyn Error>>
```

##### `read_headers()`
```rust
/// Quickly reads only the headers (first row) from the first sheet of the Excel file.
///
/// # Returns
/// * `Ok(Vec<String>)` - Array of column headers on success
/// * `Err(Box<dyn Error>)` - Error information on failure
///
/// # Example
/// ```rust
/// use rust_excel_parser::ExcelParser;
/// 
/// let parser = ExcelParser::new("example.xlsx".to_string());
/// match parser.read_headers() {
///     Ok(headers) => {
///         println!("Columns: {:?}", headers);
///         // Output: ["ID", "Name", "Age", "Email"]
///     }
///     Err(e) => eprintln!("Failed to read headers: {}", e),
/// }
/// ```
pub fn read_headers(&self) -> Result<Vec<String>, Box<dyn Error>>
```

#### Internal Methods

##### `parse_worksheet()`
```rust
/// Parses a single worksheet and converts it to SheetData.
///
/// # Arguments
/// * `worksheet` - Calamine's Range<DataType>
///
/// # Returns
/// * `Ok(SheetData)` - Converted sheet data
/// * `Err(Box<dyn Error>)` - Parsing error
fn parse_worksheet(&self, worksheet: &Range<DataType>) -> Result<SheetData, Box<dyn Error>>
```

##### `extract_headers()`
```rust
/// Extracts headers from the first row of the worksheet.
///
/// # Arguments
/// * `worksheet` - Calamine's Range<DataType>
///
/// # Returns
/// * `Ok(Vec<String>)` - Array of header strings
/// * `Err(Box<dyn Error>)` - Extraction failure
fn extract_headers(&self, worksheet: &Range<DataType>) -> Result<Vec<String>, Box<dyn Error>>
```

##### `cell_to_string()`
```rust
/// Converts Calamine's DataType to string.
///
/// # Arguments
/// * `cell` - Cell data to convert
///
/// # Returns
/// * `String` - Converted string
///
/// # Type Conversion
/// * `DataType::String(s)` → `s`
/// * `DataType::Float(f)` → `f.to_string()`
/// * `DataType::Int(i)` → `i.to_string()`
/// * `DataType::Bool(b)` → `"true"` or `"false"`
/// * `DataType::DateTime(dt)` → ISO 8601 format
/// * `DataType::Empty` → `""`
fn cell_to_string(&self, cell: &DataType) -> String
```

## 📊 Data Structures

### `ExcelData`

#### Definition
```rust
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ExcelData {
    /// Vector containing all sheet data
    pub sheets: Vec<SheetData>,
}
```

#### Methods
```rust
impl ExcelData {
    /// Returns the first sheet.
    ///
    /// # Returns
    /// * `Some(&SheetData)` - If sheet exists
    /// * `None` - If no sheets exist
    pub fn first_sheet(&self) -> Option<&SheetData> {
        self.sheets.first()
    }

    /// Finds a sheet by name.
    ///
    /// # Arguments
    /// * `name` - Name of the sheet to find
    ///
    /// # Returns
    /// * `Some(&SheetData)` - If sheet with that name exists
    /// * `None` - If no sheet with that name exists
    pub fn find_sheet(&self, name: &str) -> Option<&SheetData> {
        self.sheets.iter().find(|s| s.name == name)
    }

    /// Returns all sheet names.
    ///
    /// # Returns
    /// * `Vec<&str>` - Vector of sheet names
    pub fn sheet_names(&self) -> Vec<&str> {
        self.sheets.iter().map(|s| s.name.as_str()).collect()
    }
}
```

### `SheetData`

#### Definition
```rust
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SheetData {
    /// Name of the sheet
    pub name: String,
    /// Column headers (first row)
    pub column_names: Vec<String>,
    /// Data rows (from second row onwards)
    pub rows: Vec<RowData>,
    /// Total number of rows (including header)
    pub total_rows: usize,
    /// Total number of columns
    pub total_columns: usize,
}
```

#### Methods
```rust
impl SheetData {
    /// Checks if the sheet is empty.
    ///
    /// # Returns
    /// * `true` - If there are no data rows
    /// * `false` - If there are data rows
    pub fn is_empty(&self) -> bool {
        self.rows.is_empty()
    }

    /// Returns the number of data rows (excluding header).
    ///
    /// # Returns
    /// * `usize` - Number of data rows
    pub fn data_row_count(&self) -> usize {
        self.rows.len()
    }

    /// Gets a specific row by index.
    ///
    /// # Arguments
    /// * `index` - 0-based row index
    ///
    /// # Returns
    /// * `Some(&RowData)` - If row at that index exists
    /// * `None` - If index is out of bounds
    pub fn get_row(&self, index: usize) -> Option<&RowData> {
        self.rows.get(index)
    }

    /// Converts all rows to Map<String, String> format.
    ///
    /// # Returns
    /// * `Vec<HashMap<String, String>>` - Vector where each row is a column→value mapping
    pub fn to_maps(&self) -> Vec<std::collections::HashMap<String, String>> {
        self.rows.iter().map(|row| row.to_map()).collect()
    }
}
```

### `RowData`

#### Definition
```rust
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct RowData {
    /// All cell data in the row
    pub cells: Vec<CellData>,
    /// Row number in original Excel (1-based)
    pub row_index: usize,
}
```

#### Methods
```rust
impl RowData {
    /// Converts the row to Map<String, String>.
    ///
    /// # Returns
    /// * `HashMap<String, String>` - Map with column names as keys
    pub fn to_map(&self) -> std::collections::HashMap<String, String> {
        self.cells.iter()
            .map(|cell| (cell.column_name.clone(), cell.value.clone()))
            .collect()
    }

    /// Gets the value of a specific column.
    ///
    /// # Arguments
    /// * `column_name` - Name of the column to find
    ///
    /// # Returns
    /// * `Some(&str)` - If that column exists
    /// * `None` - If that column doesn't exist
    pub fn get_value(&self, column_name: &str) -> Option<&str> {
        self.cells.iter()
            .find(|cell| cell.column_name == column_name)
            .map(|cell| cell.value.as_str())
    }

    /// Returns the number of cells.
    ///
    /// # Returns
    /// * `usize` - Number of cells
    pub fn cell_count(&self) -> usize {
        self.cells.len()
    }
}
```

### `CellData`

#### Definition
```rust
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CellData {
    /// Name of the column this cell belongs to
    pub column_name: String,
    /// Cell value (converted to string)
    pub value: String,
}
```

#### Methods
```rust
impl CellData {
    /// Creates new cell data.
    ///
    /// # Arguments
    /// * `column_name` - Column name
    /// * `value` - Cell value
    ///
    /// # Returns
    /// * `CellData` - New cell data instance
    pub fn new(column_name: String, value: String) -> Self {
        Self { column_name, value }
    }

    /// Checks if the cell value is empty.
    ///
    /// # Returns
    /// * `true` - If value is empty string
    /// * `false` - If value exists
    pub fn is_empty(&self) -> bool {
        self.value.trim().is_empty()
    }

    /// Attempts to parse the cell value to a specific type.
    ///
    /// # Returns
    /// * `Ok(T)` - Parsing success
    /// * `Err(T::Err)` - Parsing failure
    pub fn parse<T>(&self) -> Result<T, T::Err>
    where
        T: std::str::FromStr,
    {
        self.value.parse()
    }
}
```

## 🔄 MessagePack Converter

### `MessagePackConverter`

#### Definition
```rust
pub struct MessagePackConverter;
```

#### Methods
```rust
impl MessagePackConverter {
    /// Converts ExcelData to MessagePack byte array.
    ///
    /// # Arguments
    /// * `data` - Excel data to convert
    ///
    /// # Returns
    /// * `Ok(Vec<u8>)` - MessagePack byte array on success
    /// * `Err(rmp_serde::encode::Error)` - Serialization failure
    ///
    /// # Example
    /// ```rust
    /// use rust_excel_parser::{ExcelParser, MessagePackConverter};
    /// 
    /// let parser = ExcelParser::new("data.xlsx".to_string());
    /// let data = parser.read_data().unwrap();
    /// let bytes = MessagePackConverter::to_bytes(&data).unwrap();
    /// println!("Serialized size: {} bytes", bytes.len());
    /// ```
    pub fn to_bytes(data: &ExcelData) -> Result<Vec<u8>, rmp_serde::encode::Error> {
        rmp_serde::to_vec(data)
    }

    /// Converts header array to MessagePack byte array.
    ///
    /// # Arguments
    /// * `headers` - Header string array to convert
    ///
    /// # Returns
    /// * `Ok(Vec<u8>)` - MessagePack byte array on success
    /// * `Err(rmp_serde::encode::Error)` - Serialization failure
    pub fn headers_to_bytes(headers: &[String]) -> Result<Vec<u8>, rmp_serde::encode::Error> {
        rmp_serde::to_vec(headers)
    }

    /// Converts a single sheet to MessagePack byte array.
    ///
    /// # Arguments
    /// * `sheet` - Sheet data to convert
    ///
    /// # Returns
    /// * `Ok(Vec<u8>)` - MessagePack byte array on success
    /// * `Err(rmp_serde::encode::Error)` - Serialization failure
    pub fn sheet_to_bytes(sheet: &SheetData) -> Result<Vec<u8>, rmp_serde::encode::Error> {
        rmp_serde::to_vec(sheet)
    }

    /// Deserializes MessagePack byte array to ExcelData.
    ///
    /// # Arguments
    /// * `bytes` - MessagePack byte array
    ///
    /// # Returns
    /// * `Ok(ExcelData)` - Excel data on success
    /// * `Err(rmp_serde::decode::Error)` - Deserialization failure
    pub fn from_bytes(bytes: &[u8]) -> Result<ExcelData, rmp_serde::decode::Error> {
        rmp_serde::from_slice(bytes)
    }
}
```

## 🌉 JNI Interface

### JNI Functions

#### `Java_io_github_mangjoo_ExcelParser_getHeaders`
```rust
/// JNI function corresponding to Java's getHeaders() method
///
/// # Arguments
/// * `env` - JNI environment
/// * `_class` - Java class (unused)
/// * `file_path` - Excel file path (Java String)
///
/// # Returns
/// * `jbyteArray` - MessagePack serialized header array, null on failure
///
/// # Java Signature
/// ```java
/// private static native byte[] getHeaders(String filePath);
/// ```
#[no_mangle]
pub extern "system" fn Java_io_github_mangjoo_ExcelParser_getHeaders<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    file_path: JString<'local>,
) -> jbyteArray
```

#### `Java_io_github_mangjoo_ExcelParser_readExcel`
```rust
/// JNI function corresponding to Java's readExcel() method
///
/// # Arguments
/// * `env` - JNI environment
/// * `_class` - Java class (unused)
/// * `file_path` - Excel file path (Java String)
///
/// # Returns
/// * `jbyteArray` - MessagePack serialized complete Excel data, null on failure
///
/// # Java Signature
/// ```java
/// private static native byte[] readExcel(String filePath);
/// ```
#[no_mangle]
pub extern "system" fn Java_io_github_mangjoo_ExcelParser_readExcel<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    file_path: JString<'local>,
) -> jbyteArray
```

#### `Java_io_github_mangjoo_ExcelParser_readSheet`
```rust
/// JNI function corresponding to Java's readSheet() method
///
/// # Arguments
/// * `env` - JNI environment
/// * `_class` - Java class (unused)
/// * `file_path` - Excel file path (Java String)
/// * `sheet_name` - Sheet name (Java String)
///
/// # Returns
/// * `jbyteArray` - MessagePack serialized sheet data, null on failure
///
/// # Java Signature
/// ```java
/// private static native byte[] readSheet(String filePath, String sheetName);
/// ```
#[no_mangle]
pub extern "system" fn Java_io_github_mangjoo_ExcelParser_readSheet<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    file_path: JString<'local>,
    sheet_name: JString<'local>,
) -> jbyteArray
```

### Utility Functions

#### `bytes_to_java_array`
```rust
/// Converts Rust Vec<u8> to Java byte[].
///
/// # Arguments
/// * `env` - JNI environment
/// * `bytes` - Byte vector to convert
///
/// # Returns
/// * `jbyteArray` - Java byte array, null on failure
fn bytes_to_java_array(env: &mut JNIEnv, bytes: Vec<u8>) -> jbyteArray
```

#### `get_string_from_java`
```rust
/// Converts Java String to Rust String.
///
/// # Arguments
/// * `env` - JNI environment
/// * `jstring` - Java String object
///
/// # Returns
/// * `Some(String)` - Rust string on success
/// * `None` - On conversion failure
fn get_string_from_java(env: &mut JNIEnv, jstring: &JString) -> Option<String>
```

## ❌ Error Types

### Common Error Types
```rust
/// Type alias for all Excel parsing related errors
pub type ExcelError = Box<dyn std::error::Error>;
```

### Error Categories

#### File Access Errors
- File does not exist
- No read permission for file
- Corrupted file

#### Excel Format Errors
- Unsupported Excel format
- Corrupted Excel file
- Empty file

#### Memory Errors
- Out of memory
- File too large

#### Serialization Errors
- MessagePack serialization failure
- Unsupported data type

## 💻 Usage Examples

### Basic Usage
```rust
use rust_excel_parser::{ExcelParser, MessagePackConverter};

fn main() -> Result<(), Box<dyn std::error::Error>> {
    // 1. Create parser
    let parser = ExcelParser::new("example.xlsx".to_string());
    
    // 2. Read headers only
    let headers = parser.read_headers()?;
    println!("Columns: {:?}", headers);
    
    // 3. Read complete data
    let data = parser.read_data()?;
    
    // 4. Process first sheet
    if let Some(sheet) = data.first_sheet() {
        println!("Sheet: {}", sheet.name);
        println!("Row count: {}", sheet.data_row_count());
        
        // 5. Process each row
        for (i, row) in sheet.rows.iter().enumerate() {
            println!("Row {}: {:?}", i + 1, row.to_map());
        }
    }
    
    Ok(())
}
```

### Advanced Usage
```rust
use rust_excel_parser::{ExcelParser, ExcelData, SheetData};
use std::collections::HashMap;

fn process_excel_file(file_path: &str) -> Result<(), Box<dyn std::error::Error>> {
    let parser = ExcelParser::new(file_path.to_string());
    let data = parser.read_data()?;
    
    // Process all sheets
    for sheet in &data.sheets {
        println!("=== Sheet: {} ===", sheet.name);
        
        // Statistics
        println!("Total rows: {}", sheet.total_rows);
        println!("Total columns: {}", sheet.total_columns);
        println!("Data rows: {}", sheet.data_row_count());
        
        // Column analysis
        analyze_columns(sheet);
        
        // Data validation
        validate_data(sheet)?;
    }
    
    Ok(())
}

fn analyze_columns(sheet: &SheetData) {
    let mut column_stats: HashMap<String, (usize, usize)> = HashMap::new();
    
    for row in &sheet.rows {
        for cell in &row.cells {
            let entry = column_stats.entry(cell.column_name.clone()).or_insert((0, 0));
            entry.0 += 1; // Total count
            if !cell.is_empty() {
                entry.1 += 1; // Non-empty count
            }
        }
    }
    
    for (column, (total, non_empty)) in column_stats {
        let fill_rate = (non_empty as f64 / total as f64) * 100.0;
        println!("Column '{}': {:.1}% filled ({}/{} cells)", column, fill_rate, non_empty, total);
    }
}

fn validate_data(sheet: &SheetData) -> Result<(), Box<dyn std::error::Error>> {
    for (row_idx, row) in sheet.rows.iter().enumerate() {
        // Check required columns
        if row.get_value("ID").is_none() {
            return Err(format!("Row {} missing ID column", row_idx + 1).into());
        }
        
        // Check data types
        if let Some(age_str) = row.get_value("Age") {
            if !age_str.is_empty() {
                if age_str.parse::<i32>().is_err() {
                    return Err(format!("Row {} Age value is not a number: '{}'", row_idx + 1, age_str).into());
                }
            }
        }
    }
    
    println!("✅ Data validation passed");
    Ok(())
}
```

### Direct MessagePack Usage
```rust
use rust_excel_parser::{ExcelParser, MessagePackConverter};
use std::fs::File;
use std::io::Write;

fn save_to_messagepack(excel_path: &str, output_path: &str) -> Result<(), Box<dyn std::error::Error>> {
    // Parse Excel file
    let parser = ExcelParser::new(excel_path.to_string());
    let data = parser.read_data()?;
    
    // Serialize to MessagePack
    let bytes = MessagePackConverter::to_bytes(&data)?;
    
    // Save to file
    let mut file = File::create(output_path)?;
    file.write_all(&bytes)?;
    
    println!("MessagePack file saved: {} bytes", bytes.len());
    Ok(())
}

fn load_from_messagepack(input_path: &str) -> Result<(), Box<dyn std::error::Error>> {
    // Read MessagePack file
    let bytes = std::fs::read(input_path)?;
    
    // Deserialize
    let data = MessagePackConverter::from_bytes(&bytes)?;
    
    // Use data
    println!("Loaded sheet count: {}", data.sheets.len());
    for sheet in &data.sheets {
        println!("Sheet '{}': {} rows", sheet.name, sheet.data_row_count());
    }
    
    Ok(())
}
```

---

📚 This API reference documentation details all public APIs of the Rust Excel Parser.  
Please refer to the source code comments in the `src/` directory for actual implementation examples.
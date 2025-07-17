# Rust Excel Parser Core

🦀 **High-performance Excel parsing library written in Rust**

This is the core Rust library that provides fast Excel file parsing with JNI bindings for Java integration. It uses the Calamine library for Excel parsing and MessagePack for efficient serialization.

## ✨ Features

- ⚡ **High Performance**: Native Rust performance with Calamine
- 📦 **Efficient Serialization**: MessagePack (4.4x smaller than JSON)
- 🔧 **JNI Ready**: Pre-built JNI bindings for Java integration
- 🎯 **Dynamic Columns**: Automatic header recognition from first row
- 🛡️ **Type Safety**: Guaranteed by Rust's type system
- 💾 **Memory Efficient**: Streaming approach for large files

## 🏗️ Architecture

```rust
// Core modules
pub mod parser;               // Excel parsing logic
pub mod messagepack_converter; // MessagePack serialization
pub mod jni_wrapper;          // Java Native Interface
```

## 📦 Dependencies

```toml
[dependencies]
calamine = "0.25"           # Excel file parsing
jni = "0.21"               # Java Native Interface
serde = { version = "1.0", features = ["derive"] }
rmp-serde = "1.1"          # MessagePack serialization
```

## 🚀 Usage

### Basic Rust Usage

```rust
use rust_excel_parser::{ExcelParser, MessagePackConverter};

// Create parser
let parser = ExcelParser::new("example.xlsx".to_string());

// Read headers only
let headers = parser.read_headers()?;
println!("Columns: {:?}", headers);

// Read complete data
let data = parser.read_data()?;

// Convert to MessagePack
let bytes = MessagePackConverter::to_bytes(&data)?;
```

### Data Structures

```rust
pub struct ExcelData {
    pub sheets: Vec<SheetData>,
}

pub struct SheetData {
    pub name: String,
    pub column_names: Vec<String>,
    pub rows: Vec<RowData>,
    pub total_rows: usize,
    pub total_columns: usize,
}

pub struct RowData {
    pub cells: Vec<CellData>,
    pub row_index: usize,
}

pub struct CellData {
    pub column_name: String,
    pub value: String,
}
```

## 🛠️ Building

### Local Build
```bash
# Basic build
cargo build --release

# Build all macOS targets
./build_all.sh
```

### Cross-Platform Build
```bash
# Install targets
rustup target add x86_64-apple-darwin aarch64-apple-darwin
rustup target add x86_64-unknown-linux-gnu aarch64-unknown-linux-gnu
rustup target add x86_64-pc-windows-gnu

# Cross-compile ALL platforms with Docker (including macOS)
./build_cross.sh

# Universal build script (recommended)
./build_universal.sh --all
```

### Output Files
```
target/
├── dist/           # Native builds (macOS only)
│   ├── librust_excel_parser-x86_64-apple-darwin.dylib
│   └── librust_excel_parser-aarch64-apple-darwin.dylib
├── cross-dist/     # Docker cross-compiled (ALL platforms)
│   ├── librust_excel_parser-x86_64-apple-darwin.dylib
│   ├── librust_excel_parser-aarch64-apple-darwin.dylib
│   ├── librust_excel_parser-x86_64-unknown-linux-gnu.so
│   ├── librust_excel_parser-aarch64-unknown-linux-gnu.so
│   └── rust_excel_parser-x86_64-pc-windows-gnu.dll
└── universal/      # All binaries combined
```

## 🔍 Performance

### MessagePack vs JSON
- **MessagePack**: 84 bytes
- **JSON**: 373 bytes
- **Compression ratio**: 4.4x more efficient

### Memory Usage
- Streaming approach minimizes memory footprint
- No need to load entire file into memory
- Efficient cell-by-cell processing

## 🧪 Testing

```bash
# Run tests
cargo test

# Run with output
cargo test -- --nocapture

# Test specific functionality
cargo test test_excel_parsing
```

## 📚 API Documentation

### Core Types
- **[ExcelParser](API.md#excelparser)** - Main parsing interface
- **[ExcelData](API.md#exceldata)** - Complete Excel document
- **[SheetData](API.md#sheetdata)** - Individual sheet data
- **[MessagePackConverter](API.md#messagepackconverter)** - Serialization utilities

### JNI Interface
- **[getHeaders](API.md#getheaders)** - Extract column headers
- **[readExcel](API.md#readexcel)** - Parse complete file
- **[readSheet](API.md#readsheet)** - Parse specific sheet

For detailed API documentation, see **[API.md](API.md)**

## 🔧 Integration with Java

This Rust library is designed to work seamlessly with the Java wrapper:

```java
// Java side
ExcelParser parser = new ExcelParser();
ExcelData data = parser.parseExcel("example.xlsx");
```

The Java library automatically loads the appropriate native library for the current platform.

## 📄 Documentation

- **[API Reference](API.md)** - Complete API documentation
- **[Developer Guide](RUST_DOCS.md)** - Architecture and extension guide

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Write tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## 📄 License

MIT License
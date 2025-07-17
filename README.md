# Rust Excel Parser

🦀 **High-Performance Cross-Platform Excel Parser** - Built with Rust, Java JNI Bindings Included

[![Rust](https://img.shields.io/badge/rust-1.80+-orange.svg)](https://www.rust-lang.org)
[![Java](https://img.shields.io/badge/java-8+-blue.svg)](https://openjdk.org)
[![Cross Platform](https://img.shields.io/badge/platform-macOS%20%7C%20Linux%20%7C%20Windows-lightgrey.svg)](https://github.com)
[![Performance](https://img.shields.io/badge/serialization-MessagePack%204.4x%20faster-green.svg)](https://msgpack.org)

## 🏗️ Architecture

This project demonstrates a high-performance Excel parsing solution that combines the best of both worlds:

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

**Data Flow:**
```
Excel File → Calamine → Rust Structs → MessagePack → JNI ByteArray → Java Objects
```

## ✨ Key Features

- ⚡ **High Performance**: Rust native performance + Calamine library
- 🌍 **Cross-Platform**: macOS (Intel/ARM64) + Linux (x64/ARM64) + Windows (x64)
- 📦 **Efficient Serialization**: MessagePack (4.4x smaller than JSON)
- 🔧 **Java Integration**: Seamless Java binding through JNI
- 🎯 **Dynamic Columns**: Automatic header recognition from first row
- 💾 **Memory Efficient**: Streaming approach for large file processing
- 🛡️ **Type Safety**: Guaranteed by Rust's type system

## 📁 Project Structure

```
rust_excel_parser/
├── rust/                           # 🦀 Rust Core Library
│   ├── src/
│   │   ├── parser.rs              # Excel parsing logic
│   │   ├── messagepack_converter.rs # Serialization
│   │   ├── jni_wrapper.rs         # JNI interface
│   │   └── lib.rs                 # Library entry point
│   ├── Cargo.toml                 # Rust dependencies
│   ├── API.md                     # Rust API documentation
│   └── RUST_DOCS.md              # Developer documentation
│
└── excel_parser_with_rust/         # ☕ Java Library
    ├── src/main/java/io/github/mangjoo/
    │   ├── ExcelParser.java        # Main API
    │   ├── ExcelData.java          # Data structures
    │   ├── ExcelMapper.java        # Annotation mapping
    │   ├── annotations/ExcelColumn.java # Mapping annotation
    │   └── examples/               # Usage examples
    ├── src/main/resources/native/  # Native libraries
    ├── build.gradle               # Java build config
    └── README.md                  # Java usage guide
```

## 🚀 Quick Start

### For Java Developers

```java
// 1. Add dependency to your project
// 2. Use the library
import io.github.mangjoo.ExcelParser;

ExcelParser parser = new ExcelParser();
ExcelData data = parser.parseExcel("example.xlsx");

// Convert to objects with annotations
List<Person> people = parser.parseToList("example.xlsx", Person.class);
```

👉 **[See detailed Java usage guide](excel_parser_with_rust/README.md)**

### For Rust Developers

```rust
use rust_excel_parser::{ExcelParser, MessagePackConverter};

let parser = ExcelParser::new("example.xlsx".to_string());
let data = parser.read_data()?;
let bytes = MessagePackConverter::to_bytes(&data)?;
```

👉 **[See detailed Rust API documentation](rust/API.md)**

## 📦 Supported Platforms

| Platform | Architecture | Library File |
|----------|--------------|--------------|
| macOS | x86_64 | `librust_excel_parser-x86_64-apple-darwin.dylib` |
| macOS | ARM64 | `librust_excel_parser-aarch64-apple-darwin.dylib` |
| Linux | x86_64 | `librust_excel_parser-x86_64-unknown-linux-gnu.so` |
| Linux | ARM64 | `librust_excel_parser-aarch64-unknown-linux-gnu.so` |
| Windows | x86_64 | `rust_excel_parser-x86_64-pc-windows-gnu.dll` |

## 🛠️ Building from Source

### Prerequisites
```bash
# Install Rust (1.80+)
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh

# Add cross-compilation targets
rustup target add x86_64-apple-darwin aarch64-apple-darwin
rustup target add x86_64-unknown-linux-gnu aarch64-unknown-linux-gnu
rustup target add x86_64-pc-windows-gnu
```

### Build Rust Core
```bash
cd rust/

# Quick build for current platform
cargo build --release

# Build macOS platforms (native)
./build_all.sh

# Cross-compile ALL platforms including macOS (Docker)
./build_cross.sh

# Universal build (recommended)
./build_universal.sh --all
```

### Build Java Library
```bash
cd excel_parser_with_rust/
./gradlew build
```

## 📊 Performance Comparison

| Format | Size | Compression |
|--------|------|-------------|
| JSON | 373 bytes | 1.0x |
| **MessagePack** | **84 bytes** | **4.4x** |

## 🎯 Use Cases

- **ETL Pipelines**: High-performance data extraction from Excel files
- **Microservices**: Rust service with Java client integration
- **Enterprise Applications**: Large-scale Excel processing
- **Data Analytics**: Fast Excel data ingestion
- **Cross-Platform Tools**: Applications requiring both Rust performance and Java ecosystem

## 📚 Documentation

- **[Java Library Guide](excel_parser_with_rust/README.md)** - Usage, examples, and API
- **[Rust API Reference](rust/API.md)** - Detailed API documentation
- **[Rust Developer Docs](rust/RUST_DOCS.md)** - Architecture and extension guide

## 🤝 Contributing

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- [Calamine](https://github.com/tafia/calamine) - Rust Excel parsing library
- [MessagePack](https://msgpack.org/) - Efficient binary serialization
- [JNI](https://docs.rs/jni/) - Java Native Interface for Rust

---

⭐ **Star this repository if you find it useful!**
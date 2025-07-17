# Cross-Platform Build Guide

## Overview
This project supports building native libraries for all major platforms and architectures.

## Quick Start (macOS Only)
```bash
./build_all.sh
```
Builds for:
- macOS Intel (x86_64)
- macOS Apple Silicon (ARM64)

## Full Cross-Platform Build

### Prerequisites
- Docker Desktop
- Rust toolchain

### Build All Platforms
```bash
# Build macOS binaries natively
./build_all.sh

# Build Linux and Windows binaries using Docker
./build_cross.sh
```

## Supported Platforms

### Native Compilation (macOS)
- ✅ `x86_64-apple-darwin` - macOS Intel
- ✅ `aarch64-apple-darwin` - macOS Apple Silicon

### Cross-Compilation (Docker)
- ✅ `x86_64-unknown-linux-gnu` - Linux x86_64
- ✅ `aarch64-unknown-linux-gnu` - Linux ARM64
- ✅ `x86_64-pc-windows-gnu` - Windows x86_64
- ✅ `aarch64-pc-windows-gnu` - Windows ARM64

## Manual Cross-Compilation Setup

### Linux Targets
```bash
# Install musl cross-compilation tools
brew install FiloSottile/musl-cross/musl-cross

# Add targets
rustup target add x86_64-unknown-linux-musl aarch64-unknown-linux-musl

# Build
cargo build --release --target x86_64-unknown-linux-musl
```

### Windows Targets (MSVC)
Requires Windows SDK or Visual Studio Build Tools:
```bash
rustup target add x86_64-pc-windows-msvc aarch64-pc-windows-msvc
cargo build --release --target x86_64-pc-windows-msvc
```

## Library Naming Convention

The build system generates platform-specific libraries:

### macOS
- `librust_excel_parser-x86_64-apple-darwin.dylib`
- `librust_excel_parser-aarch64-apple-darwin.dylib`

### Linux
- `librust_excel_parser-x86_64-unknown-linux-gnu.so`
- `librust_excel_parser-aarch64-unknown-linux-gnu.so`

### Windows
- `rust_excel_parser-x86_64-pc-windows-msvc.dll` (preferred)
- `rust_excel_parser-x86_64-pc-windows-gnu.dll` (fallback)
- `rust_excel_parser-aarch64-pc-windows-msvc.dll`
- `rust_excel_parser-aarch64-pc-windows-gnu.dll`

## Java Integration

The `NativeLibraryLoader` automatically detects the platform and architecture to load the correct library:

```java
// Automatically loads the correct library for current platform
ExcelParser parser = new ExcelParser();
```

### Architecture Detection
- `x86_64`, `amd64`, `x64` → `x86_64`
- `aarch64`, `arm64` → `aarch64`
- `arm` → `arm` (if supported)

### Fallback Strategy
For Windows, the loader tries:
1. MSVC version first (`-pc-windows-msvc`)
2. GNU version as fallback (`-pc-windows-gnu`)

## Output Directories

```
target/
├── dist/           # Native macOS builds
│   ├── librust_excel_parser-x86_64-apple-darwin.dylib
│   └── librust_excel_parser-aarch64-apple-darwin.dylib
└── cross-dist/     # Docker cross-compiled builds
    ├── librust_excel_parser-x86_64-unknown-linux-gnu.so
    ├── librust_excel_parser-aarch64-unknown-linux-gnu.so
    ├── rust_excel_parser-x86_64-pc-windows-gnu.dll
    └── rust_excel_parser-aarch64-pc-windows-gnu.dll
```

## CI/CD Integration

For automated builds, use the Docker approach:

```yaml
# GitHub Actions example
- name: Build cross-platform binaries
  run: |
    ./build_all.sh           # macOS binaries
    ./build_cross.sh         # Linux/Windows binaries
    
- name: Copy to Java resources
  run: |
    cp target/dist/* java-project/src/main/resources/native/
    cp target/cross-dist/* java-project/src/main/resources/native/
```

## Troubleshooting

### Common Issues

1. **Linker errors on cross-compilation**
   - Use Docker approach for reliable cross-compilation
   - Install platform-specific linkers manually

2. **Architecture mismatch**
   - Ensure JVM architecture matches library architecture
   - Java 8 on Apple Silicon may run in x86_64 mode

3. **Missing dependencies**
   - Linux builds may require additional system libraries
   - Consider static linking for better portability

### Verification

Test library loading:
```bash
# macOS
otool -L target/dist/librust_excel_parser-*.dylib

# Linux  
ldd target/cross-dist/librust_excel_parser-*.so

# Windows
# Use dependency walker or similar tools
```
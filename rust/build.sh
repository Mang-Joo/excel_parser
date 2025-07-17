#!/bin/bash

# Rust Excel Parser - Universal Build Script
# Supports native builds and Docker cross-compilation for all platforms
set -e

echo "🦀 Rust Excel Parser Build Script"
echo "================================="

# Function to show usage
show_usage() {
    echo "Usage: $0 [mode]"
    echo ""
    echo "Build Modes:"
    echo "  fast          Build only for current platform (fastest)"
    echo "  cross         Cross-compile all platforms using Docker"
    echo "  all           Build everything - native + cross-compiled (default)"
    echo ""
    echo "Examples:"
    echo "  ./build.sh           # Build everything (recommended)"
    echo "  ./build.sh fast      # Quick local build"
    echo "  ./build.sh cross     # All platforms via Docker"
}

# Function to detect current platform
detect_platform() {
    OS=$(uname -s)
    ARCH=$(uname -m)
    
    case "$OS" in
        "Darwin") echo "macOS ($ARCH)" ;;
        "Linux") echo "Linux ($ARCH)" ;;
        "MINGW"*|"MSYS"*|"CYGWIN"*) echo "Windows ($ARCH)" ;;
        *) echo "Unknown ($OS $ARCH)" ;;
    esac
}

# Function to build native
build_native() {
    echo "🏠 Building for current platform: $(detect_platform)"
    
    OS=$(uname -s)
    ARCH=$(uname -m)
    
    # Create output directory
    mkdir -p target/dist
    
    case "$OS" in
        "Darwin")
            echo "📱 Building for macOS..."
            # Add required targets
            rustup target add x86_64-apple-darwin aarch64-apple-darwin 2>/dev/null || true
            
            # Build both architectures
            cargo build --release --target x86_64-apple-darwin
            cargo build --release --target aarch64-apple-darwin
            
            # Copy to dist
            cp target/x86_64-apple-darwin/release/librust_excel_parser.dylib target/dist/librust_excel_parser-x86_64-apple-darwin.dylib
            cp target/aarch64-apple-darwin/release/librust_excel_parser.dylib target/dist/librust_excel_parser-aarch64-apple-darwin.dylib
            ;;
        "Linux")
            echo "🐧 Building for Linux..."
            cargo build --release
            if [ "$ARCH" = "x86_64" ]; then
                cp target/release/librust_excel_parser.so target/dist/librust_excel_parser-x86_64-unknown-linux-gnu.so
            elif [ "$ARCH" = "aarch64" ]; then
                cp target/release/librust_excel_parser.so target/dist/librust_excel_parser-aarch64-unknown-linux-gnu.so
            fi
            ;;
        "MINGW"*|"MSYS"*|"CYGWIN"*)
            echo "🪟 Building for Windows..."
            cargo build --release
            cp target/release/rust_excel_parser.dll target/dist/rust_excel_parser-x86_64-pc-windows-gnu.dll
            ;;
        *)
            echo "🔨 Building for unknown platform..."
            cargo build --release
            ;;
    esac
    
    echo "✅ Native build completed!"
}

# Function to build with Docker
build_cross() {
    echo "🐳 Cross-compiling all platforms using Docker..."
    
    # Check if Docker is available
    if ! command -v docker &> /dev/null; then
        echo "❌ Docker is not installed or not available"
        echo "📖 Install Docker: https://www.docker.com/products/docker-desktop"
        return 1
    fi
    
    # Create Dockerfile for cross-compilation
    cat > Dockerfile.cross << 'EOF'
# Multi-platform cross-compilation for Rust Excel Parser
FROM rust:1.80-slim

# Install dependencies
RUN apt-get update && apt-get install -y \
    gcc-mingw-w64 \
    gcc-aarch64-linux-gnu \
    gcc-x86-64-linux-gnu \
    clang \
    cmake \
    curl \
    git \
    libssl-dev \
    pkg-config \
    libc6-dev \
    && rm -rf /var/lib/apt/lists/*

# Install osxcross for macOS
WORKDIR /tmp
RUN git clone https://github.com/tpoechtrager/osxcross.git && \
    cd osxcross && \
    curl -L -o tarballs/MacOSX12.3.sdk.tar.xz \
    "https://github.com/joseluisq/macosx-sdks/releases/download/12.3/MacOSX12.3.sdk.tar.xz" && \
    UNATTENDED=yes OSX_VERSION_MIN=10.7 ./build.sh && \
    cp -r target/bin/* /usr/local/bin/ && \
    cp -r target/lib/* /usr/local/lib/ && \
    rm -rf /tmp/osxcross

# Add Rust targets
RUN rustup target add \
    x86_64-unknown-linux-gnu \
    aarch64-unknown-linux-gnu \
    x86_64-pc-windows-gnu \
    x86_64-apple-darwin \
    aarch64-apple-darwin

# Configure linkers
ENV CARGO_TARGET_X86_64_UNKNOWN_LINUX_GNU_LINKER=x86_64-linux-gnu-gcc
ENV CARGO_TARGET_AARCH64_UNKNOWN_LINUX_GNU_LINKER=aarch64-linux-gnu-gcc
ENV CARGO_TARGET_X86_64_PC_WINDOWS_GNU_LINKER=x86_64-w64-mingw32-gcc
ENV CARGO_TARGET_X86_64_APPLE_DARWIN_LINKER=x86_64-apple-darwin20.4-clang
ENV CARGO_TARGET_AARCH64_APPLE_DARWIN_LINKER=aarch64-apple-darwin20.4-clang
ENV PATH="/usr/local/bin:$PATH"

WORKDIR /app
COPY . .
RUN rm -f Cargo.lock

# Build all platforms
RUN cargo build --release --target x86_64-unknown-linux-gnu && \
    cargo build --release --target aarch64-unknown-linux-gnu && \
    cargo build --release --target x86_64-pc-windows-gnu && \
    cargo build --release --target x86_64-apple-darwin && \
    cargo build --release --target aarch64-apple-darwin

# Copy binaries
RUN mkdir -p /output && \
    cp target/x86_64-unknown-linux-gnu/release/librust_excel_parser.so /output/librust_excel_parser-x86_64-unknown-linux-gnu.so && \
    cp target/aarch64-unknown-linux-gnu/release/librust_excel_parser.so /output/librust_excel_parser-aarch64-unknown-linux-gnu.so && \
    cp target/x86_64-pc-windows-gnu/release/rust_excel_parser.dll /output/rust_excel_parser-x86_64-pc-windows-gnu.dll && \
    cp target/x86_64-apple-darwin/release/librust_excel_parser.dylib /output/librust_excel_parser-x86_64-apple-darwin.dylib && \
    cp target/aarch64-apple-darwin/release/librust_excel_parser.dylib /output/librust_excel_parser-aarch64-apple-darwin.dylib

CMD ["ls", "-la", "/output/"]
EOF

    echo "📦 Building Docker image..."
    docker build -f Dockerfile.cross -t rust-excel-parser-cross .
    
    echo "🚀 Cross-compiling all platforms..."
    mkdir -p target/cross-dist
    docker run --rm -v "$(pwd)/target/cross-dist:/host-output" rust-excel-parser-cross bash -c "
        cp /output/* /host-output/ && 
        echo '✅ Cross-compilation completed!'
    "
    
    # Cleanup
    rm -f Dockerfile.cross
    
    echo "✅ Docker cross-compilation completed!"
}

# Function to organize all binaries
organize_binaries() {
    echo "📦 Organizing binaries..."
    
    mkdir -p target/all
    
    # Copy from both locations
    [ -d "target/dist" ] && cp target/dist/* target/all/ 2>/dev/null || true
    [ -d "target/cross-dist" ] && cp target/cross-dist/* target/all/ 2>/dev/null || true
    
    if [ "$(ls -A target/all 2>/dev/null)" ]; then
        echo "✅ All binaries organized in target/all/"
    else
        echo "❌ No binaries found"
        return 1
    fi
}

# Function to show build summary
show_summary() {
    echo ""
    echo "🎉 Build Summary"
    echo "==============="
    
    if [ -d "target/all" ] && [ "$(ls -A target/all)" ]; then
        echo ""
        echo "📋 Built binaries:"
        for file in target/all/*; do
            if [ -f "$file" ]; then
                filename=$(basename "$file")
                size=$(ls -lh "$file" | awk '{print $5}')
                case "$filename" in
                    *apple-darwin*) platform="🍎 macOS" ;;
                    *linux-gnu*) platform="🐧 Linux" ;;
                    *windows*) platform="🪟 Windows" ;;
                    *) platform="❓ Unknown" ;;
                esac
                echo "  $platform $filename ($size)"
            fi
        done
        
        echo ""
        echo "📁 Files location: target/all/"
        echo "🚀 Ready for deployment!"
    else
        echo "❌ No binaries found. Build failed."
        exit 1
    fi
}

# Main execution
case "${1:-all}" in
    "fast")
        echo "⚡ Fast build mode - current platform only"
        build_native
        organize_binaries
        show_summary
        ;;
    "cross")
        echo "🐳 Cross-compilation mode - all platforms via Docker"
        build_cross
        organize_binaries
        show_summary
        ;;
    "all"|"")
        echo "🔥 Complete build mode - native + cross-compilation"
        echo ""
        
        echo "1️⃣ Phase 1: Native Build"
        if build_native; then
            echo "✅ Native build successful"
        else
            echo "⚠️ Native build failed, continuing..."
        fi
        
        echo ""
        echo "2️⃣ Phase 2: Docker Cross-Compilation"
        if build_cross; then
            echo "✅ Cross-compilation successful"
        else
            echo "❌ Cross-compilation failed"
            organize_binaries
            show_summary
            exit 1
        fi
        
        organize_binaries
        show_summary
        ;;
    "--help"|"-h"|"help")
        show_usage
        ;;
    *)
        echo "❌ Unknown mode: $1"
        echo ""
        show_usage
        exit 1
        ;;
esac
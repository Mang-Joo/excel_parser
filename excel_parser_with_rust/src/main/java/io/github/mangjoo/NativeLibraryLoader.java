package io.github.mangjoo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Utility to automatically load platform-specific native libraries
 * This is an internal implementation detail and should not be used directly
 */
class NativeLibraryLoader {
    private static boolean loaded = false;

    public static synchronized void loadLibrary() throws IOException {
        if (loaded) return;

        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();

        String libraryName = getLibraryName(osName, osArch);
        String resourcePath = "/native/" + libraryName;

        // Find library file from resources
        InputStream inputStream = NativeLibraryLoader.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IOException("Native library not found: " + resourcePath);
        }

        try {
            // Copy library to temporary file
            Path tempFile = Files.createTempFile("rust_excel_parser", getLibraryExtension(osName));
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            // Load library
            System.load(tempFile.toAbsolutePath().toString());

            // Cleanup - delete temporary file on JVM exit
            tempFile.toFile().deleteOnExit();

            loaded = true;
        } catch (Exception e){
            throw new IOException("Failed to load native library: " + resourcePath, e);
        } finally {
            inputStream.close();
        }
    }

    private static String getLibraryName(String osName, String osArch) {
        String normalizedArch = normalizeArch(osArch);

        if (osName.contains("mac")) {
            return tryFindLibrary("librust_excel_parser-" + normalizedArch + "-apple-darwin.dylib");
        } else if (osName.contains("win")) {
            // Try MSVC first, then GNU
            String msvcLib = "rust_excel_parser-" + normalizedArch + "-pc-windows-msvc.dll";
            String gnuLib = "rust_excel_parser-" + normalizedArch + "-pc-windows-gnu.dll";
            return tryFindLibrary(msvcLib, gnuLib);
        } else if (osName.contains("linux")) {
            return tryFindLibrary("librust_excel_parser-" + normalizedArch + "-unknown-linux-gnu.so");
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + osName + " with architecture: " + osArch);
        }
    }

    private static String tryFindLibrary(String... libraryNames) {
        for (String libName : libraryNames) {
            if (NativeLibraryLoader.class.getResourceAsStream("/native/" + libName) != null) {
                return libName;
            }
        }
        // If none found, return the first one (will cause proper error message later)
        return libraryNames[0];
    }

    private static String normalizeArch(String osArch) {
        // Normalize architecture name to Rust target format
        switch (osArch.toLowerCase()) {
            case "x86_64":
            case "amd64":
            case "x64":
                return "x86_64";
            case "aarch64":
            case "arm64":
                return "aarch64";
            case "arm":
                return "arm"; // Additional support if needed
            default:
                throw new UnsupportedOperationException("Unsupported architecture: " + osArch);
        }
    }

    private static String getLibraryExtension(String osName) {
        if (osName.contains("mac")) {
            return ".dylib";
        } else if (osName.contains("win")) {
            return ".dll";
        } else {
            return ".so";
        }
    }
}
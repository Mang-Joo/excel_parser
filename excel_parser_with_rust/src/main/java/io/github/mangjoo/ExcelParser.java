package io.github.mangjoo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import io.github.mangjoo.writer.WriteConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * High-performance Excel parser (Rust-based)
 *
 * Usage:
 * <pre>
 * ExcelParser parser = new ExcelParser();
 * ExcelData data = parser.parseExcel("example.xlsx");
 *
 * // Get all rows from first sheet as Map
 * List&lt;Map&lt;String, String&gt;&gt; rows = data.getFirstSheet().getAllRowsAsMap();
 * </pre>
 */
public class ExcelParser {
    private static final ObjectMapper msgpackMapper = new ObjectMapper(new MessagePackFactory());

    static {
        try {
            // Load native library
            NativeLibraryLoader.loadLibrary();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load native library", e);
        }
    }

    // === JNI Native Methods ===
    private static native byte[] getHeaders(String filePath);
    private static native byte[] readExcel(String filePath);
    private static native byte[] readSheet(String filePath, String sheetName);
    private static native boolean writeExcel(String filePath, byte[] configBytes);
    private static native boolean writeMultipleSheets(String filePath, byte[] configsBytes);

    /**
     * Returns column headers from Excel file
     */
    public List<String> getColumnHeaders(String filePath) throws IOException {
        byte[] bytes = getHeaders(filePath);
        if (bytes == null) {
            throw new IOException("Failed to read headers from: " + filePath);
        }
        return msgpackMapper.readValue(bytes, List.class);
    }

    /**
     * Parse complete Excel file
     */
    public ExcelData parseExcel(String filePath) throws IOException {
        byte[] bytes = readExcel(filePath);
        if (bytes == null) {
            throw new IOException("Failed to read Excel file: " + filePath);
        }

        // Manual parsing needed as Rust returns nested arrays
        Object rawData = msgpackMapper.readValue(bytes, Object.class);
        return parseRawExcelData(rawData);
    }

    /**
     * Convert nested array returned from Rust to ExcelData
     */
    private ExcelData parseRawExcelData(Object rawData) {
        if (!(rawData instanceof List)) {
            throw new RuntimeException("Expected array from Rust, got: " + rawData.getClass());
        }

        List<?> outerList = (List<?>) rawData;
        if (outerList.isEmpty() || !(outerList.get(0) instanceof List)) {
            throw new RuntimeException("Invalid Excel data structure");
        }

        List<?> sheetsList = (List<?>) outerList.get(0);
        List<SheetData> sheets = new ArrayList<>();

        for (Object sheetObj : sheetsList) {
            if (!(sheetObj instanceof List)) continue;

            List<?> sheetArray = (List<?>) sheetObj;
            if (sheetArray.size() < 5) continue;

            // [sheet_name, column_names_array, row_data_array, total_rows, total_columns]
            String sheetName = (String) sheetArray.get(0);
            List<?> columnNamesObj = (List<?>) sheetArray.get(1);
            List<String> columnNames = new ArrayList<>();
            for (Object col : columnNamesObj) {
                columnNames.add((String) col);
            }

            List<?> rowsDataObj = (List<?>) sheetArray.get(2);
            List<RowData> rows = new ArrayList<>();

            for (Object rowObj : rowsDataObj) {
                if (!(rowObj instanceof List)) continue;
                List<?> rowArray = (List<?>) rowObj;
                if (rowArray.size() < 2) continue;

                // [cell_data_array, row_index]
                List<?> cellsDataObj = (List<?>) rowArray.get(0);
                int rowIndex = ((Number) rowArray.get(1)).intValue();

                List<CellData> cells = new ArrayList<>();
                for (Object cellObj : cellsDataObj) {
                    if (!(cellObj instanceof List)) continue;
                    List<?> cellArray = (List<?>) cellObj;
                    if (cellArray.size() < 2) continue;

                    // [column_name, value]
                    String columnName = (String) cellArray.get(0);
                    String value = String.valueOf(cellArray.get(1));

                    CellData cellData = new CellData();
                    cellData.setColumnName(columnName);
                    cellData.setValue(value);
                    cells.add(cellData);
                }

                RowData rowData = new RowData();
                rowData.setCells(cells);
                rowData.setRowIndex(rowIndex);
                rows.add(rowData);
            }

            int totalRows = ((Number) sheetArray.get(3)).intValue();
            int totalColumns = ((Number) sheetArray.get(4)).intValue();

            SheetData sheetData = new SheetData();
            sheetData.setName(sheetName);
            sheetData.setColumnNames(columnNames);
            sheetData.setRows(rows);
            sheetData.setTotalRows(totalRows);
            sheetData.setTotalColumns(totalColumns);

            sheets.add(sheetData);
        }

        ExcelData excelData = new ExcelData();
        excelData.setSheets(sheets);
        return excelData;
    }

    /**
     * Parse specific sheet only
     */
    public SheetData parseSheet(String filePath, String sheetName) throws IOException {
        byte[] bytes = readSheet(filePath, sheetName);
        if (bytes == null) {
            throw new IOException("Failed to read sheet '" + sheetName + "' from: " + filePath);
        }
        return msgpackMapper.readValue(bytes, SheetData.class);
    }

    /**
     * Convenience method: parse first sheet only
     */
    public SheetData parseFirstSheet(String filePath) throws IOException {
        ExcelData data = parseExcel(filePath);
        SheetData firstSheet = data.getFirstSheet();
        if (firstSheet == null) {
            throw new IOException("No sheets found in: " + filePath);
        }
        return firstSheet;
    }

    /**
     * Convert Excel file to object list using annotation-based mapping (first sheet)
     *
     * @param filePath Excel file path
     * @param clazz Class type to convert to (requires @ExcelColumn annotation)
     * @return Converted object list
     */
    public <T> List<T> parseToList(String filePath, Class<T> clazz) throws IOException {
        SheetData sheet = parseFirstSheet(filePath);
        List<Map<String, String>> rows = sheet.getAllRowsAsMap();
        return ExcelMapper.mapToObjects(rows, clazz);
    }

    /**
     * Convert Excel file to object list using annotation-based mapping (specific sheet)
     *
     * @param filePath Excel file path
     * @param sheetName Sheet name
     * @param clazz Class type to convert to (requires @ExcelColumn annotation)
     * @return Converted object list
     */
    public <T> List<T> parseToList(String filePath, String sheetName, Class<T> clazz) throws IOException {
        SheetData sheet = parseSheet(filePath, sheetName);
        List<Map<String, String>> rows = sheet.getAllRowsAsMap();
        return ExcelMapper.mapToObjects(rows, clazz);
    }

    /**
     * Convert already parsed sheet data to object list
     *
     * @param sheet Sheet data
     * @param clazz Class type to convert to (requires @ExcelColumn annotation)
     * @return Converted object list
     */
    public <T> List<T> mapToList(SheetData sheet, Class<T> clazz) {
        List<Map<String, String>> rows = sheet.getAllRowsAsMap();
        return ExcelMapper.mapToObjects(rows, clazz);
    }
    
    // === Write Methods ===
    
    /**
     * Write objects to Excel file using annotation-based mapping.
     * 
     * @param filePath Target Excel file path
     * @param data List of objects to write
     * @param clazz Class type of the objects
     * @return true if successful, false otherwise
     * @throws IOException if write fails
     */
    public <T> boolean writeExcel(String filePath, List<T> data, Class<T> clazz) throws IOException {
        return writeExcel(filePath, data, clazz, "Sheet1");
    }
    
    /**
     * Write objects to Excel file with specific sheet name.
     * 
     * @param filePath Target Excel file path
     * @param data List of objects to write
     * @param clazz Class type of the objects
     * @param sheetName Sheet name
     * @return true if successful, false otherwise
     * @throws IOException if write fails
     */
    public <T> boolean writeExcel(String filePath, List<T> data, Class<T> clazz, String sheetName) throws IOException {
        WriteConfig config = ExcelWriter.createWriteConfig(data, clazz, sheetName);
        return writeExcelWithConfig(filePath, config);
    }
    
    /**
     * Write Excel using a pre-configured WriteConfig.
     * 
     * @param filePath Target Excel file path
     * @param config Write configuration
     * @return true if successful, false otherwise
     * @throws IOException if write fails
     */
    public boolean writeExcelWithConfig(String filePath, WriteConfig config) throws IOException {
        byte[] configBytes = msgpackMapper.writeValueAsBytes(config);
        boolean result = writeExcel(filePath, configBytes);
        if (!result) {
            throw new IOException("Failed to write Excel file: " + filePath);
        }
        return true;
    }
    
    /**
     * Write multiple sheets to Excel file.
     * 
     * @param filePath Target Excel file path
     * @param configs List of write configurations (one per sheet)
     * @return true if successful, false otherwise
     * @throws IOException if write fails
     */
    public boolean writeMultipleSheetsWithConfig(String filePath, List<WriteConfig> configs) throws IOException {
        byte[] configsBytes = msgpackMapper.writeValueAsBytes(configs);
        boolean result = writeMultipleSheets(filePath, configsBytes);
        if (!result) {
            throw new IOException("Failed to write Excel file with multiple sheets: " + filePath);
        }
        return result;
    }
    
    /**
     * Create a WriteConfig from objects for manual configuration.
     * This allows customization before writing.
     * 
     * @param data List of objects to write
     * @param clazz Class type of the objects
     * @param sheetName Sheet name
     * @return WriteConfig that can be customized before writing
     */
    public <T> WriteConfig prepareWriteConfig(List<T> data, Class<T> clazz, String sheetName) {
        return ExcelWriter.createWriteConfig(data, clazz, sheetName);
    }
}
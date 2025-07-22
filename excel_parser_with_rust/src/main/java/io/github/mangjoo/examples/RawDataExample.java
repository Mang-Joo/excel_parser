package io.github.mangjoo.examples;

import io.github.mangjoo.ExcelData;
import io.github.mangjoo.ExcelParser;
import io.github.mangjoo.SheetData;
import io.github.mangjoo.RowData;
import io.github.mangjoo.CellData;

import java.util.List;
import java.util.Map;

/**
 * General Excel parsing example without annotation-based mapping
 */
public class RawDataExample {
    
    public static void main(String[] args) {
        try {
            ExcelParser parser = new ExcelParser();
            // Copy resource to temp file for reading
            java.io.InputStream is = RawDataExample.class.getClassLoader().getResourceAsStream("exmaple.xlsx");
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("example", ".xlsx");
            java.nio.file.Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            is.close();
            String filePath = tempFile.toString();
            
            System.out.println("=== Raw Data Excel Parsing Example ===\n");
            
            // 1. Read headers only
            System.out.println("1. Reading Column Headers:");
            List<String> headers = parser.getColumnHeaders(filePath);
            System.out.println("  Headers: " + headers);
            
            // 2. Parse complete Excel file
            System.out.println("\n2. Parsing Complete Excel File:");
            ExcelData excelData = parser.parseExcel(filePath);
            System.out.println("  Total sheets: " + excelData.getSheets().size());
            
            for (SheetData sheet : excelData.getSheets()) {
                System.out.println("  Sheet name: " + sheet.getName());
                System.out.println("  Total rows: " + sheet.getTotalRows());
                System.out.println("  Total columns: " + sheet.getTotalColumns());
                System.out.println("  Column names: " + sheet.getColumnNames());
            }
            
            // 3. Parse first sheet only
            System.out.println("\n3. Parsing First Sheet Only:");
            SheetData firstSheet = parser.parseFirstSheet(filePath);
            System.out.println("  Sheet name: " + firstSheet.getName());
            
            // 4. Direct row data access
            System.out.println("\n4. Direct Row Data Access:");
            List<RowData> rows = firstSheet.getRows();
            for (int i = 0; i < Math.min(rows.size(), 3); i++) {
                RowData row = rows.get(i);
                System.out.println("  Row " + (i + 1) + " (index: " + row.getRowIndex() + "):");
                
                for (CellData cell : row.getCells()) {
                    System.out.println("    " + cell.getColumnName() + ": " + cell.getValue());
                }
            }
            
            // 5. Convert to Map format
            System.out.println("\n5. Convert to Map Format:");
            List<Map<String, String>> mapRows = firstSheet.getAllRowsAsMap();
            for (int i = 0; i < Math.min(mapRows.size(), 2); i++) {
                Map<String, String> mapRow = mapRows.get(i);
                System.out.println("  Row " + (i + 1) + ": " + mapRow);
            }
            
            // 6. Extract specific column values
            System.out.println("\n6. Extract Specific Column Values:");
            for (int i = 0; i < Math.min(mapRows.size(), 2); i++) {
                Map<String, String> row = mapRows.get(i);
                String id = row.get("ID");
                String name = row.get("Name");
                String age = row.get("Age");
                System.out.println("  ID: " + id + ", Name: " + name + ", Age: " + age);
            }
            
            System.out.println("\n✅ Raw Data Parsing Complete!");
            
        } catch (Exception e) {
            System.err.println("❌ Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
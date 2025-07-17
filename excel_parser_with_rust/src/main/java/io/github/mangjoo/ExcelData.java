package io.github.mangjoo;

import java.util.List;

/**
 * Class representing Excel document data
 */
public class ExcelData {
    private List<SheetData> sheets;

    public ExcelData() {}

    public ExcelData(List<SheetData> sheets) {
        this.sheets = sheets;
    }

    public List<SheetData> getSheets() {
        return sheets;
    }

    public void setSheets(List<SheetData> sheets) {
        this.sheets = sheets;
    }

    /**
     * Return first sheet
     */
    public SheetData getFirstSheet() {
        return sheets != null && !sheets.isEmpty() ? sheets.get(0) : null;
    }

    /**
     * Find sheet by name
     */
    public SheetData getSheet(String name) {
        if (sheets == null) return null;
        return sheets.stream()
            .filter(sheet -> sheet.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
}
package io.github.mangjoo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class representing Excel sheet data
 */
public class SheetData {
    private String name;
    private List<String> columnNames;
    private List<RowData> rows;
    private int totalRows;
    private int totalColumns;

    public SheetData() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getColumnNames() { return columnNames; }
    public void setColumnNames(List<String> columnNames) { this.columnNames = columnNames; }

    public List<RowData> getRows() { return rows; }
    public void setRows(List<RowData> rows) { this.rows = rows; }

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getTotalColumns() { return totalColumns; }
    public void setTotalColumns(int totalColumns) { this.totalColumns = totalColumns; }

    /**
     * Return specific row data as Map (column name → value)
     */
    public Map<String, String> getRowAsMap(int rowIndex) {
        if (rows == null || rowIndex >= rows.size()) return null;

        RowData row = rows.get(rowIndex);
        return row.getCells().stream()
            .collect(Collectors.toMap(
                CellData::getColumnName,
                CellData::getValue
            ));
    }

    /**
     * Return all rows as Map list
     */
    public List<Map<String, String>> getAllRowsAsMap() {
        if (rows == null) return null;

        return rows.stream()
            .map(row -> row.getCells().stream()
                .collect(Collectors.toMap(
                    CellData::getColumnName,
                    CellData::getValue
                )))
            .collect(Collectors.toList());
    }
}
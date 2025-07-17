package io.github.mangjoo;

import java.util.List;

/**
 * Class representing Excel row data
 */
public class RowData {
    private List<CellData> cells;
    private int rowIndex;

    public RowData() {}

    public List<CellData> getCells() { return cells; }
    public void setCells(List<CellData> cells) { this.cells = cells; }

    public int getRowIndex() { return rowIndex; }
    public void setRowIndex(int rowIndex) { this.rowIndex = rowIndex; }

    /**
     * Get value of specific column
     */
    public String getValue(String columnName) {
        if (cells == null) return null;

        return cells.stream()
            .filter(cell -> cell.getColumnName().equals(columnName))
            .map(CellData::getValue)
            .findFirst()
            .orElse(null);
    }
}
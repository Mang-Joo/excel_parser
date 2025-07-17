package io.github.mangjoo;

/**
 * Class representing Excel cell data
 */
public class CellData {
    private String columnName;
    private String value;

    public CellData() {}

    public CellData(String columnName, String value) {
        this.columnName = columnName;
        this.value = value;
    }

    public String getColumnName() { return columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    @Override
    public String toString() {
        return columnName + "=" + value;
    }
}
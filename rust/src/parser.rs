use calamine::{open_workbook, Reader, Xlsx};
use serde::{Deserialize, Serialize};
use std::error::Error;

pub struct ExcelParser {
    file_path: String,
}

impl ExcelParser {
    pub fn new(file_path: impl Into<String>) -> Self {
        Self {
            file_path: file_path.into(),
        }
    }

    pub fn read_headers(&self) -> Result<Vec<String>, Box<dyn Error>> {
        let mut workbook: Xlsx<_> = open_workbook(&self.file_path)?;
        let sheet_names = workbook.sheet_names().to_vec();

        if sheet_names.is_empty() {
            return Err("No worksheets found".into());
        }

        let range = workbook.worksheet_range(&sheet_names[0])?;
        let headers: Vec<String> = range
            .rows()
            .next()
            .map(|row| row.iter().map(|cell| cell.to_string()).collect())
            .unwrap_or_default();

        Ok(headers)
    }

    pub fn read_data(&self) -> Result<ExcelData, Box<dyn Error>> {
        let mut workbook: Xlsx<_> = open_workbook(&self.file_path)?;
        let mut sheets = Vec::new();

        for sheet_name in workbook.sheet_names().to_vec() {
            let sheet_data = self.read_sheet(&mut workbook, &sheet_name)?;
            sheets.push(sheet_data);
        }

        Ok(ExcelData { sheets })
    }

    fn read_sheet(
        &self,
        workbook: &mut Xlsx<std::io::BufReader<std::fs::File>>,
        sheet_name: &str,
    ) -> Result<SheetData, Box<dyn Error>> {
        let range = workbook.worksheet_range(sheet_name)?;
        let mut rows_iter = range.rows();

        let headers: Vec<String> = rows_iter
            .next()
            .map(|row| row.iter().map(|cell| cell.to_string()).collect())
            .unwrap_or_default();

        let mut rows = Vec::new();
        for (row_index, row) in rows_iter.enumerate() {
            let cells = row
                .iter()
                .enumerate()
                .filter_map(|(col_index, cell)| {
                    headers.get(col_index).map(|header| CellData {
                        column_name: header.clone(),
                        value: cell.to_string(),
                    })
                })
                .collect();

            rows.push(RowData {
                cells,
                row_index: row_index as u32 + 1,
            });
        }

        let total_rows = rows.len() as u32;
        let total_columns = range.width() as u32;

        Ok(SheetData {
            name: sheet_name.to_string(),
            column_names: headers,
            rows,
            total_rows,
            total_columns,
        })
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CellData {
    pub column_name: String,
    pub value: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct RowData {
    pub cells: Vec<CellData>,
    pub row_index: u32,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SheetData {
    pub name: String,
    pub column_names: Vec<String>,
    pub rows: Vec<RowData>,
    pub total_rows: u32,
    pub total_columns: u32,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ExcelData {
    pub sheets: Vec<SheetData>,
}

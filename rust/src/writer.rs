use serde::{Deserialize, Serialize};
use std::error::Error;
use rust_xlsxwriter::{Workbook, Format, FormatAlign};

#[derive(Debug, Serialize, Deserialize)]
pub struct WriteConfig {
    pub sheet_name: String,
    pub headers: Vec<HeaderConfig>,
    pub data: Vec<Vec<String>>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct HeaderConfig {
    pub name: String,
    pub width: Option<f64>,
    pub format: Option<String>,
}

pub struct ExcelWriter {
    file_path: String,
}

impl ExcelWriter {
    pub fn new(file_path: String) -> Self {
        ExcelWriter { file_path }
    }

    /// Write data to Excel file from WriteConfig
    pub fn write_data(&self, config: &WriteConfig) -> Result<(), Box<dyn Error>> {
        let mut workbook = Workbook::new();
        let worksheet = workbook.add_worksheet();
        
        // Set worksheet name
        worksheet.set_name(&config.sheet_name)?;
        
        // Create header format
        let header_format = Format::new()
            .set_bold()
            .set_align(FormatAlign::Center)
            .set_background_color(0xD3D3D3); // Light gray
        
        // Write headers
        for (col, header) in config.headers.iter().enumerate() {
            worksheet.write_with_format(0, col as u16, &header.name, &header_format)?;
            
            // Set column width if specified
            if let Some(width) = header.width {
                worksheet.set_column_width(col as u16, width)?;
            }
        }
        
        // Write data rows
        for (row_idx, row_data) in config.data.iter().enumerate() {
            for (col_idx, cell_value) in row_data.iter().enumerate() {
                // Apply format if specified for this column
                if let Some(format_str) = &config.headers.get(col_idx).and_then(|h| h.format.as_ref()) {
                    let cell_format = self.create_format_from_string(format_str)?;
                    worksheet.write_with_format(
                        (row_idx + 1) as u32,
                        col_idx as u16,
                        cell_value,
                        &cell_format
                    )?;
                } else {
                    worksheet.write(
                        (row_idx + 1) as u32,
                        col_idx as u16,
                        cell_value
                    )?;
                }
            }
        }
        
        // Save the workbook
        workbook.save(&self.file_path)?;
        Ok(())
    }
    
    /// Create format from format string
    fn create_format_from_string(&self, format_str: &str) -> Result<Format, Box<dyn Error>> {
        let mut format = Format::new();
        
        match format_str {
            "@" => {
                // Text format
                format = format.set_num_format("@");
            }
            "#,##0" => {
                // Number with thousands separator
                format = format.set_num_format("#,##0");
            }
            "#,##0.00" => {
                // Number with 2 decimal places
                format = format.set_num_format("#,##0.00");
            }
            "yyyy-MM-dd" => {
                // Date format
                format = format.set_num_format("yyyy-mm-dd");
            }
            "yyyy-MM-dd HH:mm:ss" => {
                // DateTime format
                format = format.set_num_format("yyyy-mm-dd hh:mm:ss");
            }
            _ => {
                // Use custom format string as-is
                format = format.set_num_format(format_str);
            }
        }
        
        Ok(format)
    }

    /// Write data with multiple sheets
    pub fn write_multiple_sheets(&self, configs: Vec<WriteConfig>) -> Result<(), Box<dyn Error>> {
        let mut workbook = Workbook::new();
        
        for config in configs {
            let worksheet = workbook.add_worksheet();
            worksheet.set_name(&config.sheet_name)?;
            
            // Create header format
            let header_format = Format::new()
                .set_bold()
                .set_align(FormatAlign::Center)
                .set_background_color(0xD3D3D3);
            
            // Write headers
            for (col, header) in config.headers.iter().enumerate() {
                worksheet.write_with_format(0, col as u16, &header.name, &header_format)?;
                
                if let Some(width) = header.width {
                    worksheet.set_column_width(col as u16, width)?;
                }
            }
            
            // Write data
            for (row_idx, row_data) in config.data.iter().enumerate() {
                for (col_idx, cell_value) in row_data.iter().enumerate() {
                    worksheet.write(
                        (row_idx + 1) as u32,
                        col_idx as u16,
                        cell_value
                    )?;
                }
            }
        }
        
        workbook.save(&self.file_path)?;
        Ok(())
    }
}
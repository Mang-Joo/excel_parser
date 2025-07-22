use crate::parser::{ExcelData, SheetData};
use crate::writer::WriteConfig;

pub struct MessagePackConverter;

impl MessagePackConverter {
    pub fn to_bytes(excel_data: &ExcelData) -> Result<Vec<u8>, rmp_serde::encode::Error> {
        rmp_serde::to_vec(excel_data)
    }

    pub fn sheet_to_bytes(sheet: &SheetData) -> Result<Vec<u8>, rmp_serde::encode::Error> {
        rmp_serde::to_vec(sheet)
    }

    pub fn headers_to_bytes(headers: &[String]) -> Result<Vec<u8>, rmp_serde::encode::Error> {
        rmp_serde::to_vec(headers)
    }

    pub fn from_bytes(bytes: &[u8]) -> Result<ExcelData, rmp_serde::decode::Error> {
        rmp_serde::from_slice(bytes)
    }

    pub fn sheet_from_bytes(bytes: &[u8]) -> Result<SheetData, rmp_serde::decode::Error> {
        rmp_serde::from_slice(bytes)
    }

    pub fn headers_from_bytes(bytes: &[u8]) -> Result<Vec<String>, rmp_serde::decode::Error> {
        rmp_serde::from_slice(bytes)
    }
    
    pub fn write_config_to_bytes(config: &WriteConfig) -> Result<Vec<u8>, rmp_serde::encode::Error> {
        rmp_serde::to_vec(config)
    }
    
    pub fn write_config_from_bytes(bytes: &[u8]) -> Result<WriteConfig, rmp_serde::decode::Error> {
        rmp_serde::from_slice(bytes)
    }
    
    pub fn write_configs_to_bytes(configs: &[WriteConfig]) -> Result<Vec<u8>, rmp_serde::encode::Error> {
        rmp_serde::to_vec(configs)
    }
    
    pub fn write_configs_from_bytes(bytes: &[u8]) -> Result<Vec<WriteConfig>, rmp_serde::decode::Error> {
        rmp_serde::from_slice(bytes)
    }
}

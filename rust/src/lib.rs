pub mod jni_wrapper;
pub mod messagepack_converter;
pub mod parser;

#[cfg(test)]
mod tests {
    use super::messagepack_converter::MessagePackConverter;
    use super::parser::ExcelParser;

    #[test]
    fn test_excel_parsing() {
        let parser = ExcelParser::new("example.xlsx");
        let headers = parser.read_headers().expect("Failed to read headers");
        assert_eq!(headers, vec!["ID", "Name", "Age"]);

        let data = parser.read_data().expect("Failed to read data");
        assert_eq!(data.sheets.len(), 1);
        assert_eq!(data.sheets[0].name, "Sheet1");
        assert_eq!(data.sheets[0].total_rows, 2);
    }

    #[test]
    fn test_messagepack_serialization() {
        let parser = ExcelParser::new("example.xlsx");
        let data = parser.read_data().expect("Failed to read Excel");

        let bytes = MessagePackConverter::to_bytes(&data).expect("Failed to serialize");
        let deserialized = MessagePackConverter::from_bytes(&bytes).expect("Failed to deserialize");

        assert_eq!(data.sheets.len(), deserialized.sheets.len());
        assert_eq!(data.sheets[0].name, deserialized.sheets[0].name);
        assert_eq!(
            data.sheets[0].column_names,
            deserialized.sheets[0].column_names
        );

        println!("✅ MessagePack size: {} bytes", bytes.len());
    }

    #[test]
    fn test_key_value_mapping() {
        let parser = ExcelParser::new("example.xlsx");
        let data = parser.read_data().expect("Failed to read Excel");

        let sheet = &data.sheets[0];
        let first_row = &sheet.rows[0];

        // Verify key-value mapping
        assert_eq!(first_row.cells[0].column_name, "ID");
        assert_eq!(first_row.cells[0].value, "1");
        assert_eq!(first_row.cells[1].column_name, "Name");
        assert_eq!(first_row.cells[1].value, "AliceAA");
        assert_eq!(first_row.cells[2].column_name, "Age");
        assert_eq!(first_row.cells[2].value, "35");

        println!("✅ Key-value mapping verified");
    }
}

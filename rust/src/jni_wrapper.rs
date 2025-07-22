use crate::messagepack_converter::MessagePackConverter;
use crate::parser::ExcelParser;
use crate::writer::{ExcelWriter, WriteConfig};
use jni::objects::{JClass, JString, JByteArray};
use jni::sys::{jbyteArray, jboolean};
use jni::JNIEnv;

// Helper function to convert bytes to Java byte array
fn bytes_to_java_array(env: &mut JNIEnv, bytes: Vec<u8>) -> jbyteArray {
    match env.new_byte_array(bytes.len() as i32) {
        Ok(array) => {
            let signed_bytes: Vec<i8> = bytes.iter().map(|&b| b as i8).collect();
            if env.set_byte_array_region(&array, 0, &signed_bytes).is_ok() {
                array.into_raw()
            } else {
                std::ptr::null_mut()
            }
        }
        Err(_) => std::ptr::null_mut(),
    }
}

// Helper function to extract string from JString
fn get_string_from_java(env: &mut JNIEnv, jstring: &JString) -> Option<String> {
    env.get_string(jstring).ok().map(|s| s.into())
}

#[no_mangle]
pub extern "system" fn Java_io_github_mangjoo_ExcelParser_getHeaders<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    file_path: JString<'local>,
) -> jbyteArray {
    let path = match get_string_from_java(&mut env, &file_path) {
        Some(p) => p,
        None => return std::ptr::null_mut(),
    };

    let parser = ExcelParser::new(path);

    match parser.read_headers() {
        Ok(headers) => match MessagePackConverter::headers_to_bytes(&headers) {
            Ok(bytes) => bytes_to_java_array(&mut env, bytes),
            Err(_) => std::ptr::null_mut(),
        },
        Err(_) => std::ptr::null_mut(),
    }
}

#[no_mangle]
pub extern "system" fn Java_io_github_mangjoo_ExcelParser_readExcel<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    file_path: JString<'local>,
) -> jbyteArray {
    let path = match get_string_from_java(&mut env, &file_path) {
        Some(p) => p,
        None => return std::ptr::null_mut(),
    };

    let parser = ExcelParser::new(path);

    match parser.read_data() {
        Ok(data) => match MessagePackConverter::to_bytes(&data) {
            Ok(bytes) => bytes_to_java_array(&mut env, bytes),
            Err(_) => std::ptr::null_mut(),
        },
        Err(_) => std::ptr::null_mut(),
    }
}

#[no_mangle]
pub extern "system" fn Java_io_github_mangjoo_ExcelParser_readSheet<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    file_path: JString<'local>,
    sheet_name: JString<'local>,
) -> jbyteArray {
    let path = match get_string_from_java(&mut env, &file_path) {
        Some(p) => p,
        None => return std::ptr::null_mut(),
    };

    let sheet_name = match get_string_from_java(&mut env, &sheet_name) {
        Some(s) => s,
        None => return std::ptr::null_mut(),
    };

    let parser = ExcelParser::new(path);

    match parser.read_data() {
        Ok(data) => {
            if let Some(sheet) = data.sheets.iter().find(|s| s.name == sheet_name) {
                match MessagePackConverter::sheet_to_bytes(sheet) {
                    Ok(bytes) => bytes_to_java_array(&mut env, bytes),
                    Err(_) => std::ptr::null_mut(),
                }
            } else {
                std::ptr::null_mut()
            }
        }
        Err(_) => std::ptr::null_mut(),
    }
}

// Helper function to convert Java byte array to bytes
fn java_array_to_bytes(env: &mut JNIEnv, array: jbyteArray) -> Option<Vec<u8>> {
    if array.is_null() {
        return None;
    }
    
    // Create JByteArray from raw pointer
    let j_array: JByteArray = unsafe { JByteArray::from_raw(array) };
    
    // Get array length
    let len = match env.get_array_length(&j_array) {
        Ok(l) => l,
        Err(_) => return None,
    };
    
    // Create a buffer to hold the bytes
    let mut bytes = vec![0i8; len as usize];
    
    // Copy the Java array into our buffer
    match env.get_byte_array_region(&j_array, 0, &mut bytes) {
        Ok(_) => {
            // Convert i8 to u8
            Some(bytes.iter().map(|&b| b as u8).collect())
        }
        Err(_) => None,
    }
}

#[no_mangle]
pub extern "system" fn Java_io_github_mangjoo_ExcelParser_writeExcel<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    file_path: JString<'local>,
    config_bytes: jbyteArray,
) -> jboolean {
    let path = match get_string_from_java(&mut env, &file_path) {
        Some(p) => p,
        None => return 0, // false
    };
    
    let bytes = match java_array_to_bytes(&mut env, config_bytes) {
        Some(b) => b,
        None => return 0, // false
    };
    
    let config: WriteConfig = match MessagePackConverter::write_config_from_bytes(&bytes) {
        Ok(c) => c,
        Err(e) => {
            eprintln!("Failed to deserialize WriteConfig: {:?}", e);
            return 0; // false
        }
    };
    
    eprintln!("WriteConfig: sheet_name={}, headers={}, rows={}", 
        config.sheet_name, config.headers.len(), config.data.len());
    
    let writer = ExcelWriter::new(path.clone());
    
    match writer.write_data(&config) {
        Ok(_) => {
            eprintln!("Successfully wrote Excel file: {}", path);
            1 // true
        }
        Err(e) => {
            eprintln!("Failed to write Excel file: {:?}", e);
            0 // false
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_io_github_mangjoo_ExcelParser_writeMultipleSheets<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    file_path: JString<'local>,
    configs_bytes: jbyteArray,
) -> jboolean {
    let path = match get_string_from_java(&mut env, &file_path) {
        Some(p) => p,
        None => return 0, // false
    };
    
    let bytes = match java_array_to_bytes(&mut env, configs_bytes) {
        Some(b) => b,
        None => return 0, // false
    };
    
    let configs: Vec<WriteConfig> = match MessagePackConverter::write_configs_from_bytes(&bytes) {
        Ok(c) => c,
        Err(_) => return 0, // false
    };
    
    let writer = ExcelWriter::new(path);
    
    match writer.write_multiple_sheets(configs) {
        Ok(_) => 1, // true
        Err(_) => 0, // false
    }
}

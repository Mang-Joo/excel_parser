use crate::messagepack_converter::MessagePackConverter;
use crate::parser::ExcelParser;
use jni::objects::{JClass, JString};
use jni::sys::jbyteArray;
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

package io.github.mangjoo.examples;

import io.github.mangjoo.ExcelParser;

import java.util.List;

/**
 * Example usage of annotation-based Excel parsing
 */
public class AnnotationExample {
    
    public static void main(String[] args) {
        try {
            ExcelParser parser = new ExcelParser();
            
            System.out.println("=== Annotation-based Excel Parsing Example ===\n");
            
            // 1. Simple mapping to Person objects
            System.out.println("1. Person Object Mapping:");
            // Copy resource to temp file for reading
            java.io.InputStream is = AnnotationExample.class.getClassLoader().getResourceAsStream("exmaple.xlsx");
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("example", ".xlsx");
            java.nio.file.Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            is.close();
            
            List<Person> people = parser.parseToList(tempFile.toString(), Person.class);
            
            for (Person person : people) {
                System.out.println("  " + person);
            }
            
            System.out.println("\n2. Annotation Mapping Details:");
            System.out.println("  - @ExcelColumn(\"ID\") → Person.id");
            System.out.println("  - @ExcelColumn(\"Name\") → Person.name");
            System.out.println("  - @ExcelColumn(\"Age\") → Person.age");
            
            // 3. Validate annotation mapping
            System.out.println("\n3. Data Validation:");
            if (!people.isEmpty()) {
                Person firstPerson = people.get(0);
                System.out.println("  First person:");
                System.out.println("    ID: " + firstPerson.getId());
                System.out.println("    Name: " + firstPerson.getName());
                System.out.println("    Age: " + firstPerson.getAge());
                
                if (people.size() > 1) {
                    Person secondPerson = people.get(1);
                    System.out.println("  Second person:");
                    System.out.println("    ID: " + secondPerson.getId());
                    System.out.println("    Name: " + secondPerson.getName());
                    System.out.println("    Age: " + secondPerson.getAge());
                }
            }
            
            System.out.println("\n✅ Annotation-based mapping successful!");
            
        } catch (Exception e) {
            System.err.println("❌ Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Employee example (more complex mapping)
     */
    public static void employeeExample() {
        try {
            ExcelParser parser = new ExcelParser();
            
            System.out.println("=== Employee Mapping Example ===");
            
            // This example assumes employee.xlsx file exists
            // In practice, you need to prepare an appropriate Excel file
            List<Employee> employees = parser.parseToList("employee.xlsx", Employee.class);
            
            for (Employee emp : employees) {
                System.out.println(emp);
                System.out.println("  Full Name: " + emp.getFullName());
                System.out.println("  Active: " + (emp.getIsActive() ? "Yes" : "No"));
                System.out.println();
            }
            
        } catch (Exception e) {
            System.err.println("Error while running Employee example: " + e.getMessage());
        }
    }
}
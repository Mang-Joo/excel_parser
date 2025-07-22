package io.github.mangjoo.examples;

import io.github.mangjoo.ExcelParser;
import io.github.mangjoo.ExcelData;
import io.github.mangjoo.annotations.ExcelReadColumn;
import io.github.mangjoo.annotations.ExcelWriteColumn;
import io.github.mangjoo.annotations.ExcelReadWrite;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Example demonstrating the difference between read and write annotations.
 * This shows how you can have different column names or formatting for reading vs writing.
 */
public class ReadWriteExample {
    
    /**
     * Example: Employee data with different read/write requirements
     */
    public static class Employee {
        // Read from multiple possible column names, write with standard name
        @ExcelReadColumn(value = "Employee ID", aliases = {"EmpID", "ID", "EmployeeNumber"})
        @ExcelWriteColumn(value = "Employee ID", order = 1, width = 15)
        private String employeeId;
        
        // Simple read/write with same configuration
        @ExcelReadWrite(value = "Full Name", writeOrder = 2, columnWidth = 30)
        private String name;
        
        // Read as string, but write with specific number format
        @ExcelReadColumn(value = "Annual Salary", aliases = {"Salary", "Base Salary", "Annual Compensation"})
        @ExcelWriteColumn(value = "Annual Salary", order = 3, format = "$#,##0.00", width = 20)
        private Double salary;
        
        // Read with default value, write with specific format
        @ExcelReadColumn(value = "Dept", aliases = {"Department", "Dept.", "Department Name"}, defaultValue = "Unassigned")
        @ExcelWriteColumn(value = "Dept", order = 4, width = 15)
        private String department;
        
        // Optional field for reading, but always written
        @ExcelReadColumn(value = "Manager Name", aliases = {"Manager", "Supervisor", "Reports To"}, required = false)
        @ExcelWriteColumn(value = "Manager Name", order = 5, width = 25)
        private String manager;
        
        // Skip this field when writing (internal use only)
        @ExcelReadColumn(value = "Internal Code", required = false)
        @ExcelWriteColumn(value = "Internal Code", skip = true)
        private String internalCode;
        
        // Constructor
        public Employee() {}
        
        public Employee(String employeeId, String name, Double salary, String department, String manager) {
            this.employeeId = employeeId;
            this.name = name;
            this.salary = salary;
            this.department = department;
            this.manager = manager;
        }
        
        // Getters and setters
        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Double getSalary() { return salary; }
        public void setSalary(Double salary) { this.salary = salary; }
        
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        
        public String getManager() { return manager; }
        public void setManager(String manager) { this.manager = manager; }
        
        public String getInternalCode() { return internalCode; }
        public void setInternalCode(String internalCode) { this.internalCode = internalCode; }
        
        @Override
        public String toString() {
            return String.format("Employee{id='%s', name='%s', salary=%.2f, dept='%s', manager='%s'}",
                employeeId, name, salary, department, manager);
        }
    }
    
    public static void main(String[] args) {
        try {
            ExcelParser parser = new ExcelParser();
            
            // Step 1: Create sample data and write to Excel
            System.out.println("=== Writing Employee Data ===");
            List<Employee> employees = Arrays.asList(
                new Employee("E001", "John Doe", 75000.0, "Engineering", "Jane Smith"),
                new Employee("E002", "Alice Johnson", 65000.0, "Marketing", "Bob Brown"),
                new Employee("E003", "Charlie Wilson", 80000.0, "Engineering", "Jane Smith"),
                new Employee("E004", "Diana Prince", 90000.0, "Executive", null)
            );
            
            boolean writeSuccess = parser.writeExcel("employees.xlsx", employees, Employee.class);
            System.out.println("Write successful: " + writeSuccess);
            
            // Step 2: Read the data back
            System.out.println("\n=== Reading Employee Data ===");
            List<Employee> readEmployees = parser.parseToList("employees.xlsx", Employee.class);
            for (Employee emp : readEmployees) {
                System.out.println(emp);
            }
            
            // Step 3: Demonstrate reading from different column names
            System.out.println("\n=== Reading from Legacy Format ===");
            // This would work with Excel files that have columns named "EmpID" instead of "Employee ID"
            // thanks to the aliases in @ExcelReadColumn
            
            // Step 4: Transform and write with different formatting
            System.out.println("\n=== Transforming and Writing ===");
            for (Employee emp : readEmployees) {
                // Give everyone a 10% raise
                emp.setSalary(emp.getSalary() * 1.1);
            }
            
            boolean transformSuccess = parser.writeExcel("employees_updated.xlsx", readEmployees, Employee.class);
            System.out.println("Transform and write successful: " + transformSuccess);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
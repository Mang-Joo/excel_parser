package io.github.mangjoo.examples;

import io.github.mangjoo.annotations.ExcelColumn;
import java.math.BigDecimal;

/**
 * Complex data types and optional field mapping example
 */
public class Employee {
    
    @ExcelColumn("EmployeeID")
    private Long employeeId;
    
    @ExcelColumn("FirstName")
    private String firstName;
    
    @ExcelColumn("LastName")
    private String lastName;
    
    @ExcelColumn("Email")
    private String email;
    
    @ExcelColumn("Salary")
    private BigDecimal salary;
    
    @ExcelColumn("IsActive")
    private Boolean isActive;
    
    @ExcelColumn(value = "Department", required = false, defaultValue = "General")
    private String department;
    
    @ExcelColumn(value = "Phone", required = false)
    private String phone;
    
    @ExcelColumn(value = "Score", required = false)
    private Double performanceScore;
    
    // Default constructor
    public Employee() {}
    
    // Getters and Setters
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public Double getPerformanceScore() { return performanceScore; }
    public void setPerformanceScore(Double performanceScore) { this.performanceScore = performanceScore; }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    @Override
    public String toString() {
        return "Employee{" +
                "employeeId=" + employeeId +
                ", name='" + getFullName() + "'" +
                ", email='" + email + "'" +
                ", department='" + department + "'" +
                ", salary=" + salary +
                ", isActive=" + isActive +
                "}";
    }
}
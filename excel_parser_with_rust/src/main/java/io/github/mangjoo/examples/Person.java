package io.github.mangjoo.examples;

import io.github.mangjoo.annotations.ExcelColumn;

/**
 * Basic data type mapping example
 */
public class Person {
    
    @ExcelColumn("ID")
    private int id;
    
    @ExcelColumn("Name")
    private String name;
    
    @ExcelColumn("Age")
    private int age;
    
    // Default constructor (required)
    public Person() {}
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    @Override
    public String toString() {
        return "Person{id=" + id + ", name='" + name + "', age=" + age + "}";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Person person = (Person) obj;
        return id == person.id && age == person.age && 
               (name != null ? name.equals(person.name) : person.name == null);
    }
}
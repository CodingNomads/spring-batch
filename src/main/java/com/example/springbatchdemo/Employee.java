package com.example.springbatchdemo;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Employee {
    
    @Id
    private Integer id;
    private String name;
    private String designation;
    private Date nameUpdated;
    private Date designationUpdated;
    
    
    public Employee(Integer id, String name, String designation, Date nameUpdated, Date designationUpdated) {
        this.id = id;
        this.name = name;
        this.designation = designation;
        this.nameUpdated = nameUpdated;
        this.designationUpdated = designationUpdated;
    }
    
    public Employee() {}
    
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDesignation() {
        return designation;
    }
    
    public void setDesignation(String designation) {
        this.designation = designation;
    }
    
    public Date getNameUpdated() {
        return nameUpdated;
    }
    
    public void setNameUpdated(Date lastTimestamp) {
        this.nameUpdated = lastTimestamp;
    }
    
    public Date getDesignationUpdated() {
        return designationUpdated;
    }
    
    public void setDesignationUpdated(Date designationUpdated) {
        this.designationUpdated = designationUpdated;
    }
    
    
    @Override
    public String toString() {
        return String.format("Employee{id=%d, name='%s', designation='%s', nameUpdated='%s', designationUpdated='%s'}", id, name, designation, nameUpdated, designationUpdated);
    }
}

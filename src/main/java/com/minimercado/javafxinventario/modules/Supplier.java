package com.minimercado.javafxinventario.modules;

import java.sql.Timestamp;

/**
 * Represents a supplier in the inventory system
 */
public class Supplier {
    private int id;
    private String name;
    private String contactName;
    private String phone;
    private String email;
    private String address;
    private String notes;
    private Timestamp lastOrderDate;
    private boolean active;
    private double averageDeliveryTime; // in days
    private double reliabilityScore; // 0-100
    
    // Default constructor
    public Supplier() {
        this.active = true;
        this.reliabilityScore = 100.0; // Default to perfect score
    }
    
    // Constructor with basic fields
    public Supplier(String name, String contactName, String phone, String email, String address) {
        this();
        this.name = name;
        this.contactName = contactName;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }
    
    // Full constructor
    public Supplier(int id, String name, String contactName, String phone, String email, 
                    String address, String notes, Timestamp lastOrderDate, boolean active) {
        this.id = id;
        this.name = name;
        this.contactName = contactName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.notes = notes;
        this.lastOrderDate = lastOrderDate;
        this.active = active;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getContactName() {
        return contactName;
    }
    
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Timestamp getLastOrderDate() {
        return lastOrderDate;
    }
    
    public void setLastOrderDate(Timestamp lastOrderDate) {
        this.lastOrderDate = lastOrderDate;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public double getAverageDeliveryTime() {
        return averageDeliveryTime;
    }
    
    public void setAverageDeliveryTime(double averageDeliveryTime) {
        this.averageDeliveryTime = averageDeliveryTime;
    }
    
    public double getReliabilityScore() {
        return reliabilityScore;
    }
    
    public void setReliabilityScore(double reliabilityScore) {
        this.reliabilityScore = reliabilityScore;
    }
    
    @Override
    public String toString() {
        return name;
    }
}

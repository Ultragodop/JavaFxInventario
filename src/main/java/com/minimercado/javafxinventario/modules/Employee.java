package com.minimercado.javafxinventario.modules;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un empleado en el sistema.
 */
public class Employee {
    public int id;
    public String firstName;
    public String lastName;
    public String documentId;
    public String position;
    public double baseSalary;
    public LocalDate hireDate;
    public String contactPhone;
    public String email;
    public String address;
    public boolean active = true;
    public List<EmployeePayment> payments;

    public Employee() {
        this.payments = new ArrayList<>();
        this.hireDate = LocalDate.now();
    }

    public Employee(String firstName, String lastName, String documentId, String position, double baseSalary) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.documentId = documentId;
        this.position = position;
        this.baseSalary = baseSalary;
    }

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(double baseSalary) {
        this.baseSalary = baseSalary;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<EmployeePayment> getPayments() {
        return payments;
    }

    public void setPayments(List<EmployeePayment> payments) {
        this.payments = payments;
    }

    public void addPayment(EmployeePayment payment) {
        if (payment != null) {
            payments.add(payment);
        }
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }

    /**
     * Obtiene el nombre completo del empleado.
     * @return El nombre completo (nombre + apellido)
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}

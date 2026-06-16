package com.quizmicroservice.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_username", columnNames = "username")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "role", nullable = false, length = 30)
    private String role = "STUDENT";

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "roll_no", length = 50)
    private String rollNo;

    @Column(name = "branch", length = 100)
    private String branch;

    @Column(name = "father_name", length = 100)
    private String fatherName;

    @Column(name = "mother_name", length = 100)
    private String motherName;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "institute", length = 150)
    private String institute;

    @Column(name = "current_address_line1", length = 200)
    private String currentAddressLine1;

    @Column(name = "current_address_line2", length = 200)
    private String currentAddressLine2;

    @Column(name = "current_city", length = 100)
    private String currentCity;

    @Column(name = "current_state", length = 100)
    private String currentState;

    @Column(name = "current_pincode", length = 20)
    private String currentPincode;

    @Column(name = "permanent_address_line1", length = 200)
    private String permanentAddressLine1;

    @Column(name = "permanent_address_line2", length = 200)
    private String permanentAddressLine2;

    @Column(name = "permanent_city", length = 100)
    private String permanentCity;

    @Column(name = "permanent_state", length = 100)
    private String permanentState;

    @Column(name = "permanent_pincode", length = 20)
    private String permanentPincode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public User() {
    }

    @PrePersist
    public void prePersist() {
        normalizeFields();

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }

        if (role == null || role.isBlank()) {
            role = "STUDENT";
        }
    }

    @PreUpdate
    public void preUpdate() {
        normalizeFields();

        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }

        if (role == null || role.isBlank()) {
            role = "STUDENT";
        }
    }

    private void normalizeFields() {
        firstName = normalizeText(firstName);
        lastName = normalizeText(lastName);
        username = normalizeText(username);
        email = normalizeEmail(email);
        role = normalizeUpper(role);
        status = normalizeUpper(status);
        rollNo = normalizeText(rollNo);
        branch = normalizeText(branch);
        fatherName = normalizeText(fatherName);
        motherName = normalizeText(motherName);
        institute = normalizeText(institute);

        currentAddressLine1 = normalizeText(currentAddressLine1);
        currentAddressLine2 = normalizeText(currentAddressLine2);
        currentCity = normalizeText(currentCity);
        currentState = normalizeText(currentState);
        currentPincode = normalizeText(currentPincode);

        permanentAddressLine1 = normalizeText(permanentAddressLine1);
        permanentAddressLine2 = normalizeText(permanentAddressLine2);
        permanentCity = normalizeText(permanentCity);
        permanentState = normalizeText(permanentState);
        permanentPincode = normalizeText(permanentPincode);
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeEmail(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }

    private String normalizeUpper(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = normalizeText(firstName);
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = normalizeText(lastName);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = normalizeText(username);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = normalizeEmail(email);
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = normalizeUpper(role);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = normalizeUpper(status);
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = normalizeText(rollNo);
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = normalizeText(branch);
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = normalizeText(fatherName);
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = normalizeText(motherName);
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getInstitute() {
        return institute;
    }

    public void setInstitute(String institute) {
        this.institute = normalizeText(institute);
    }

    public String getCurrentAddressLine1() {
        return currentAddressLine1;
    }

    public void setCurrentAddressLine1(String currentAddressLine1) {
        this.currentAddressLine1 = normalizeText(currentAddressLine1);
    }

    public String getCurrentAddressLine2() {
        return currentAddressLine2;
    }

    public void setCurrentAddressLine2(String currentAddressLine2) {
        this.currentAddressLine2 = normalizeText(currentAddressLine2);
    }

    public String getCurrentCity() {
        return currentCity;
    }

    public void setCurrentCity(String currentCity) {
        this.currentCity = normalizeText(currentCity);
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = normalizeText(currentState);
    }

    public String getCurrentPincode() {
        return currentPincode;
    }

    public void setCurrentPincode(String currentPincode) {
        this.currentPincode = normalizeText(currentPincode);
    }

    public String getPermanentAddressLine1() {
        return permanentAddressLine1;
    }

    public void setPermanentAddressLine1(String permanentAddressLine1) {
        this.permanentAddressLine1 = normalizeText(permanentAddressLine1);
    }

    public String getPermanentAddressLine2() {
        return permanentAddressLine2;
    }

    public void setPermanentAddressLine2(String permanentAddressLine2) {
        this.permanentAddressLine2 = normalizeText(permanentAddressLine2);
    }

    public String getPermanentCity() {
        return permanentCity;
    }

    public void setPermanentCity(String permanentCity) {
        this.permanentCity = normalizeText(permanentCity);
    }

    public String getPermanentState() {
        return permanentState;
    }

    public void setPermanentState(String permanentState) {
        this.permanentState = normalizeText(permanentState);
    }

    public String getPermanentPincode() {
        return permanentPincode;
    }

    public void setPermanentPincode(String permanentPincode) {
        this.permanentPincode = normalizeText(permanentPincode);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
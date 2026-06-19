package com.quizmicroservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UpdateStudentRequest {
	
	@NotBlank
    private String firstName;
    private String lastName;
    
    @NotBlank
    private String username;
    
    @Email
    private String email;

    private String fatherName;
    private String motherName;
    
    @NotBlank
    private String dob;
    private String institute;

    private String status;
    
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }

    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
    
    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getInstitute() { return institute; }
    public void setInstitute(String institute) { this.institute = institute; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
package com.talentiq.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RecruiterRegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Work email is required")
    @Email(message = "Valid work email is required")
    private String workEmail;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    private String phone;

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 150, message = "Company name must be between 2 and 150 characters")
    private String companyName;

    @NotBlank(message = "Company website or domain is required")
    private String companyWebsite;

    @NotBlank(message = "Company address is required")
    private String companyAddress;

    @NotBlank(message = "Designation/job title is required")
    private String designation;

    public RecruiterRegisterRequest() {
    }

    public RecruiterRegisterRequest(String fullName, String workEmail, String password, String confirmPassword, String phone, String companyName, String companyWebsite, String companyAddress, String designation) {
        this.fullName = fullName;
        this.workEmail = workEmail;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.phone = phone;
        this.companyName = companyName;
        this.companyWebsite = companyWebsite;
        this.companyAddress = companyAddress;
        this.designation = designation;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getWorkEmail() {
        return workEmail;
    }

    public void setWorkEmail(String workEmail) {
        this.workEmail = workEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyWebsite() {
        return companyWebsite;
    }

    public void setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }
}


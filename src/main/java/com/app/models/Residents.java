package com.app.models;

import java.time.LocalDate;

public class Residents {
    private int id;
    private String fullName;
    private LocalDate dateOfBirth;
    private String placeOfBirth;
    private String ethnicity;
    private String occupation;
    private String hometown;
    private String idCardNumber;
    private String residenceStatus;
    private String phone;
    private String gender;
    private String relationshipToOwner;
    private String roomNumber;
    private String status;

    public Residents(int id, String fullName, LocalDate dateOfBirth, String placeOfBirth, String ethnicity,
                     String occupation, String hometown, String idCardNumber, String residenceStatus,
                     String phone, String gender, String relationshipToOwner, String roomNumber, String status) {
        this.id = id;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.placeOfBirth = placeOfBirth;
        this.ethnicity = ethnicity;
        this.occupation = occupation;
        this.hometown = hometown;
        this.idCardNumber = idCardNumber;
        this.residenceStatus = residenceStatus;
        this.phone = phone;
        this.gender = gender;
        this.relationshipToOwner = relationshipToOwner;
        this.roomNumber = roomNumber;
        this.status = status;
    }

    // Residents
    public Residents(int id, String fullName, LocalDate dateOfBirth, String gender, String phone,
                     String idCardNumber, String roomNumber, String relationshipToOwner, String residenceStatus, String status) {
        this.id = id;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.idCardNumber = idCardNumber;
        this.roomNumber = roomNumber;
        this.relationshipToOwner = relationshipToOwner;
        this.residenceStatus = residenceStatus;
        this.status = status;
    }

    // Room detail
    public Residents(int id, String fullName, LocalDate dateOfBirth, String gender, String phone,
                     String idCardNumber, String relationshipToOwner, String residenceStatus) {
        this.id = id;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.idCardNumber = idCardNumber;
        this.relationshipToOwner = relationshipToOwner;
        this.residenceStatus = residenceStatus;
    }

    public Residents(int id, String fullName, String roomNumber) {
        this.id = id;
        this.fullName = fullName;
        this.roomNumber = roomNumber;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public String getOccupation() {
        return occupation;
    }

    public String getHometown() {
        return hometown;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public String getResidenceStatus() {
        return residenceStatus;
    }

    public String getPhone() {
        return phone;
    }

    public String getGender() {
        return gender;
    }

    public String getRelationshipToOwner() {
        return relationshipToOwner;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public void setResidenceStatus(String residenceStatus) {
        this.residenceStatus = residenceStatus;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setRelationshipToOwner(String relationshipToOwner) {
        this.relationshipToOwner = relationshipToOwner;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return fullName;
    }
}


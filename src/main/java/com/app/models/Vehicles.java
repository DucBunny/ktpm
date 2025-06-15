package com.app.models;

import java.time.LocalDate;

public class Vehicles {
    private int id;
    private String roomNumber;
    private String plateNumber;
    private String type;
    private String brand;
    private String color;
    private LocalDate registrationDate;
    private String isActive;
    private String note;

    // Vehicles
    public Vehicles(int id, String type, String plateNumber, String brand, String color,
                    LocalDate registrationDate, String isActive, String note) {
        this.id = id;
        this.type = type;
        this.plateNumber = plateNumber;
        this.brand = brand;
        this.color = color;
        this.registrationDate = registrationDate;
        this.isActive = isActive;
        this.note = note;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public String getType() {
        return type;
    }

    public String getBrand() {
        return brand;
    }

    public String getColor() {
        return color;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public String getIsActive() {
        return isActive;
    }

    public String getNote() {
        return note;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void setActive(String active) {
        this.isActive = active;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
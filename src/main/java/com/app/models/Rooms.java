package com.app.models;

public class Rooms {
    private String roomNumber;
    private int floor;
    private float area;
    private String status;

    // Rooms
    public Rooms(String roomNumber, int floor, float area, String status) {
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.area = area;
        this.status = status;
    }

    // Getters
    public String getRoomNumber() {
        return roomNumber;
    }

    public int getFloor() {
        return floor;
    }

    public float getArea() {
        return area;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public void setArea(float area) {
        this.area = area;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
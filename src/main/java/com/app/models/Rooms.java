package com.app.models;

public class Rooms {
    private String roomNumber;
    private int floor;
    private float area;
    private String status;

    public Rooms(String roomNumber, int floor, float area, String status) {
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.area = area;
        this.status = status;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public float getArea() {
        return area;
    }

    public void setArea(float area) {
        this.area = area;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

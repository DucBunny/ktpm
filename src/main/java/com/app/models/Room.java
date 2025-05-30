package com.app.models;

public class Room {
    private int roomNumber;
    private String floor;
    private String area;
    private String status;

    public Room(int roomNumber, String floor, String area, String status) {
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.area = area;
        this.status = status;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

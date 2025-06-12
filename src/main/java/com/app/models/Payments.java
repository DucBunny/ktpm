package com.app.models;

import java.time.LocalDate;

public class Payments {
    private int id;
    private String roomNumber;
    private String collectionPeriod;
    private String amount;
    private LocalDate paymentDate;
    private String note;
    private String status;

    public Payments(int id, String roomNumber, String amount, LocalDate paymentDate, String note, String status) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.note = note;
        this.status = status;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getCollectionPeriod() {
        return collectionPeriod;
    }

    public String getAmount() {
        return amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getNote() {
        return note;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setCollectionPeriod(String collectionPeriod) {
        this.collectionPeriod = collectionPeriod;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}


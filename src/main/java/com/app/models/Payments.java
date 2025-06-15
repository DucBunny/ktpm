package com.app.models;

import java.time.LocalDate;

public class Payments {
    private int id;
    private String roomNumber;
    private String collectionPeriod;
    private String totalAmount;
    private String paidAmount;
    private String debtAmount;
    private String excessAmount;
    private LocalDate paymentDate;
    private String note;
    private String status;

    // Payments
    public Payments(int id, String roomNumber, String totalAmount, String paidAmount, LocalDate paymentDate, String note, String status) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
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

    public String getTotalAmount() {
        return totalAmount;
    }

    public String getPaidAmount() {
        return paidAmount;
    }

    public String getDebtAmount() {
        return debtAmount;
    }

    public String getExcessAmount() {
        return excessAmount;
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

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setPaidAmount(String paidAmount) {
        this.paidAmount = paidAmount;
    }

    public void setDebtAmount(String debtAmount) {
        this.debtAmount = debtAmount;
    }

    public void setExcessAmount(String excessAmount) {
        this.excessAmount = excessAmount;
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


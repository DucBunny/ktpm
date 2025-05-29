package com.app.models;

import java.time.LocalDate;

public class PaymentDetail {
    private int id;
    private String residentName;
    private String amount;
    private String revenueItem;
    private String note;
    private LocalDate paymentDate;
    private String roomNumber;

    public PaymentDetail(int id, String residentName, String amount, String revenueItem, String note, LocalDate paymentDate, String roomNumber) {
        super();
        this.id = id;
        this.residentName = residentName;
        this.amount = amount;
        this.revenueItem = revenueItem;
        this.note = note;
        this.paymentDate = paymentDate;
        this.roomNumber = roomNumber;
    }

    public int getId() {
        return id;
    }

    public String getResidentName() {
        return residentName;
    }

    public String getAmount() {
        return amount;
    }

    public String getRevenueItem() {
        return revenueItem;
    }

    public String getNote() {
        return note;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getRoomNumber() {
        return roomNumber;
    }
}

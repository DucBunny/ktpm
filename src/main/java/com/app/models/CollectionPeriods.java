package com.app.models;

import java.time.LocalDate;

public class CollectionPeriods {
    private int id;
    private String name;
    private String code;
    private LocalDate startDate;
    private LocalDate endDate;
    private String type;
    private String totalAmount;
    private String totalPaidAmount;

    public CollectionPeriods(int id, String name, String code, String totalAmount, String totalPaidAmount, LocalDate startDate, LocalDate endDate, String type) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.totalAmount = totalAmount;
        this.totalPaidAmount = totalPaidAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getType() {
        return type;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public String getTotalPaidAmount() {
        return totalPaidAmount;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setTotalPaidAmount(String totalPaidAmount) {
        this.totalPaidAmount = totalPaidAmount;
    }

    @Override
    public String toString() {
        return "CollectionPeriod{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", type=" + type +
                '}';
    }
}
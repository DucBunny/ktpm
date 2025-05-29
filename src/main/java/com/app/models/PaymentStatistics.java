package com.app.models;

public class PaymentStatistics {
    private String nameRevenue;
    private String totalAmount;
    private String description;
    private String category;
    private String status;
    private int numberOfPayers;

    public PaymentStatistics(String nameRevenue, String totalAmount, String description, String category, String status, int numberOfPayers) {
        this.nameRevenue = nameRevenue;
        this.totalAmount = totalAmount;
        this.description = description;
        this.category = category;
        this.status = status;
        this.numberOfPayers = numberOfPayers;
    }

    public String getNameRevenue() {
        return nameRevenue;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getStatus() {
        return status;
    }

    public int getNumberOfPayers() {
        return numberOfPayers;
    }
}

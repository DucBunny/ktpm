package com.app.models;

public class CollectionItems {
    private int revenueId;
    String name;
    double quantity;
    String quantityUnit;
    double unitPrice;
    double totalAmount;
    String category;
    String roomNumber;

    // Export report
    public CollectionItems(String name, String quantityUnit, double quantity, double unitPrice, double totalAmount) {
        this.name = name;
        this.quantityUnit = quantityUnit;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
    }

    // Revenues Periods
    public CollectionItems(Revenues item, String roomNumber) {
        this.revenueId = item.getId();
        this.name = item.getName();
        this.quantity = 0;
        this.quantityUnit = item.getQuantityUnit() == null ? "" : item.getQuantityUnit();
        this.unitPrice = item.getUnitPrice().isEmpty() ? 1 : Double.parseDouble(item.getUnitPrice());
        this.totalAmount = this.unitPrice * this.quantity;
        this.category = item.getCategory();
        this.roomNumber = roomNumber;
    }

    // Getters
    public int getRevenueId() {
        return revenueId;
    }

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getQuantityUnit() {
        return quantityUnit;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getCategory() {
        return category;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    // Setters
    public void setRevenueId(int revenueId) {
        this.revenueId = revenueId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
        this.totalAmount = this.quantity * this.unitPrice; // Cập nhật totalAmount
    }

    public void setQuantityUnit(String quantityUnit) {
        this.quantityUnit = quantityUnit;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.totalAmount = this.quantity * this.unitPrice; // Cập nhật totalAmount
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
}

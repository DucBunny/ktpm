package com.app.models;

public class Revenues {
    private int id;
    private String name;
    private String status;
    private String unitPrice;
    private String description;
    private String category;

    public Revenues(int id, String name, String unitPrice, String description, String category, String status) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.unitPrice = unitPrice;
        this.description = description;
        this.category = category;
    }

    public Revenues(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getCategory() {
        return category;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return name;
    }
}
package com.app.models;

public class Revenues {
    private int id;
    private String name;
    private String code;
    private String status;
    private String unitPrice;
    private String quantityUnit;
    private String description;
    private String category;

    // Revenues
    public Revenues(int id, String name, String code, String unitPrice, String quantityUnit, String description, String category, String status) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.status = status;
        this.unitPrice = unitPrice;
        this.quantityUnit = quantityUnit;
        this.description = description;
        this.category = category;
    }

    // Revenues Periods
    public Revenues(int id, String name, String unitPrice, String quantityUnit, String category) {
        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantityUnit = quantityUnit;
        this.category = category;
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

    public String getUnitPrice() {
        return unitPrice;
    }

    public String getQuantityUnit() {
        return quantityUnit;
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

    public void setCode(String code) {
        this.code = code;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setQuantityUnit(String quantityUnit) {
        this.quantityUnit = quantityUnit;
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
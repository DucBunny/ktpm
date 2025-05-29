package com.app.models;

public class Revenues {
    private int id;
    private String name;
    private String status;
    private String value;
    private String description;
    private String category;

    public Revenues(int id, String name, String value, String description, String category, String status) {
        super();
        this.id = id;
        this.name = name;
        this.status = status;
        this.value = value;
        this.description = description;
        this.category = category;
    }

    public Revenues(int id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return name;
    }
}

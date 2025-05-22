package com.app.models;

public class Revenues {
    private int id;
    private String name;
    private String created_at;
    private String status;
    private String value;
    private String description;

    public Revenues(int id, String name, String created_at, String status, String value, String description) {
        super();
        this.id = id;
        this.name = name;
        this.created_at = created_at;
        this.status = status;
        this.value = value;
        this.description = description;
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
    public String getCreated_at() {
        return created_at;
    }
    public void setCreated_at(String created_at) {
        this.created_at = created_at;
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
}

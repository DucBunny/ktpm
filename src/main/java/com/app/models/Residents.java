package com.app.models;

import java.time.LocalDate;

public class Residents {
    private int id;
    private String name;
    private String roomNumber;
    private String phone;
    private LocalDate date_of_birth;
    private String gender;
    private String citizen_id;
    private String relationship_to_owner;

    public Residents(int id, String name, LocalDate date_of_birth, String gender, String phone, String citizen_id, String roomNumber, String relationship_to_owner) {
        super();
        this.id = id;
        this.name = name;
        this.date_of_birth = date_of_birth;
        this.gender = gender;
        this.phone = phone;
        this.citizen_id = citizen_id;
        this.roomNumber = roomNumber;
        this.relationship_to_owner = relationship_to_owner;
    }

    public Residents(int id, String name, String roomNumber) {
        super();
        this.id = id;
        this.name = name;
        this.roomNumber = roomNumber;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDateOfBirth() {
        return date_of_birth;
    }

    public String getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public String getCitizenId() {
        return citizen_id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getRelationshipToOwner() {
        return relationship_to_owner;
    }

    @Override
    public String toString() {
        return name;
    }
}


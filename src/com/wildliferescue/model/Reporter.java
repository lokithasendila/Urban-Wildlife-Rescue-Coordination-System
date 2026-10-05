package com.wildliferescue.model;

// A member of the public or a staff member who reports an incident.
public class Reporter extends Person {

    private String email;

    public Reporter(String name, String contactNumber, String email) {
        super(name, contactNumber);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getRole() {
        return "Reporter";
    }
}
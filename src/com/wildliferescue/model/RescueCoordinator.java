package com.wildliferescue.model;

// The staff member who reviews incidents and assigns rescue teams.
public class RescueCoordinator extends Person {

    private String staffId;

    public RescueCoordinator(String staffId, String name, String contactNumber) {
        super(name, contactNumber);
        this.staffId = staffId;
    }

    public String getStaffId() {
        return staffId;
    }

    @Override
    public String getRole() {
        return "Rescue Coordinator";
    }
}
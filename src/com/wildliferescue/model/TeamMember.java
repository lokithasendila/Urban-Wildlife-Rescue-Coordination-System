package com.wildliferescue.model;

// A person who belongs to a rescue team.
public class TeamMember extends Person {

    private String memberId;

    public TeamMember(String memberId, String name, String contactNumber) {
        super(name, contactNumber);
        this.memberId = memberId;
    }

    public String getMemberId() {
        return memberId;
    }

    @Override
    public String getRole() {
        return "Team Member";
    }
}
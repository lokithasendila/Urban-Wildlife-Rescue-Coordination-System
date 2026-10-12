package com.wildliferescue.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// A single reported incident.
public class RescueIncident {

    private String incidentId;
    private Animal animal;
    private String location;
    private boolean highRiskLocation;
    private int numberOfAnimals;
    private boolean immediateDanger;
    private List<Equipment> requiredEquipment;
    private Reporter reporter;
    private int priorityScore;
    private PriorityLevel priority;
    private IncidentStatus status;
    private RescueTeam assignedTeam;

    public RescueIncident(String incidentId, Animal animal, String location,
                          boolean highRiskLocation, int numberOfAnimals,
                          boolean immediateDanger, Reporter reporter) {
        this.incidentId = incidentId;
        this.animal = animal;
        this.location = location;
        this.highRiskLocation = highRiskLocation;
        this.numberOfAnimals = numberOfAnimals;
        this.immediateDanger = immediateDanger;
        this.reporter = reporter;
        this.requiredEquipment = new ArrayList<>();
        this.status = IncidentStatus.PENDING_ASSIGNMENT;   // every new incident starts here
        this.priorityScore = 0;
        this.priority = null;                              // set later by PriorityCalculator
        this.assignedTeam = null;                          // no team yet
    }

    public void addRequiredEquipment(Equipment item) {
        requiredEquipment.add(item);
    }

    // The skill needed comes from the animal (polymorphism)
    public String getRequiredSkill() {
        return animal.getRequiredSkill();
    }

    // Called by the PriorityCalculator in Step 5
    public void setPriority(int score, PriorityLevel level) {
        this.priorityScore = score;
        this.priority = level;
    }

    // Links a team to this incident
    public void assignTeam(RescueTeam team) {
        this.assignedTeam = team;
        this.status = IncidentStatus.ASSIGNED;
    }

    public void updateStatus(IncidentStatus newStatus) {
        this.status = newStatus;
    }

    public boolean isPending() {
        return status == IncidentStatus.PENDING_ASSIGNMENT;
    }

    // ----- Getters -----

    public String getIncidentId() {
        return incidentId;
    }

    public Animal getAnimal() {
        return animal;
    }

    public String getLocation() {
        return location;
    }

    public boolean isHighRiskLocation() {
        return highRiskLocation;
    }

    public int getNumberOfAnimals() {
        return numberOfAnimals;
    }

    public boolean isImmediateDanger() {
        return immediateDanger;
    }

    public List<Equipment> getRequiredEquipment() {
        return Collections.unmodifiableList(requiredEquipment);
    }

    public Reporter getReporter() {
        return reporter;
    }

    public int getPriorityScore() {
        return priorityScore;
    }

    public PriorityLevel getPriority() {
        return priority;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public RescueTeam getAssignedTeam() {
        return assignedTeam;
    }

    @Override
    public String toString() {
        String teamText = (assignedTeam == null) ? "None" : assignedTeam.getTeamId();
        return incidentId + " | " + animal + " x" + numberOfAnimals + " | " + location
                + " | danger: " + (immediateDanger ? "Yes" : "No")
                + " | priority: " + priority + " | status: " + status
                + " | team: " + teamText;
    }
}
package com.wildliferescue.model;

import java.util.ArrayList;
import java.util.List;

//one incoming report exactly as the public sent it before it is checked.
//the details are checked later when a worker thread processes the report.
public class IncidentReport {

    private String animalType;
    private AnimalCondition condition;
    private String location;
    private boolean highRiskLocation;
    private int numberOfAnimals;
    private boolean immediateDanger;
    private String reporterName;
    private String reporterContact;
    private String reporterEmail;
    private List<Equipment> requiredEquipment;

    public IncidentReport(String animalType, AnimalCondition condition, String location,
                          boolean highRiskLocation, int numberOfAnimals, boolean immediateDanger,
                          String reporterName, String reporterContact, String reporterEmail) {
        this.animalType = animalType;
        this.condition = condition;
        this.location = location;
        this.highRiskLocation = highRiskLocation;
        this.numberOfAnimals = numberOfAnimals;
        this.immediateDanger = immediateDanger;
        this.reporterName = reporterName;
        this.reporterContact = reporterContact;
        this.reporterEmail = reporterEmail;
        this.requiredEquipment = new ArrayList<>();
    }

    public void addRequiredEquipment(Equipment item) {
        requiredEquipment.add(item);
    }

    public String getAnimalType() { return animalType; }
    public AnimalCondition getCondition() { return condition; }
    public String getLocation() { return location; }
    public boolean isHighRiskLocation() { return highRiskLocation; }
    public int getNumberOfAnimals() { return numberOfAnimals; }
    public boolean isImmediateDanger() { return immediateDanger; }
    public String getReporterName() { return reporterName; }
    public String getReporterContact() { return reporterContact; }
    public String getReporterEmail() { return reporterEmail; }
    public List<Equipment> getRequiredEquipment() { return new ArrayList<>(requiredEquipment); }
}
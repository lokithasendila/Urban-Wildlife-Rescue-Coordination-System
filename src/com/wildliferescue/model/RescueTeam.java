package com.wildliferescue.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// A rescue team with its members, skills, equipment and workload.
public class RescueTeam {

    private String teamId;
    private List<TeamMember> members;
    private List<String> animalCategories;
    private List<String> skills;
    private List<Equipment> equipment;
    private TeamStatus status;
    private int maxIncidents;
    private int currentIncidents;

    public RescueTeam(String teamId, int maxIncidents) {
        this.teamId = teamId;
        this.maxIncidents = maxIncidents;
        this.currentIncidents = 0;
        this.status = TeamStatus.AVAILABLE;
        this.members = new ArrayList<>();
        this.animalCategories = new ArrayList<>();
        this.skills = new ArrayList<>();
        this.equipment = new ArrayList<>();
    }

    // ----- Methods to build up the team -----

    public void addMember(TeamMember member) {
        members.add(member);
    }

    public void addAnimalCategory(String category) {
        animalCategories.add(category.trim());
    }

    public void addSkill(String skill) {
        skills.add(skill.trim());
    }

    public void addEquipment(Equipment item) {
        equipment.add(item);
    }

    // ----- Methods used for team matching -----

    // Can this team handle this type of animal? e.g. "Bird"
    public boolean canHandle(String animalType) {
        for (String category : animalCategories) {
            if (category.equalsIgnoreCase(animalType)) {
                return true;
            }
        }
        return false;
    }

    // Does this team have this skill? e.g. "Bird Handling"
    public boolean hasSkill(String skill) {
        for (String s : skills) {
            if (s.equalsIgnoreCase(skill)) {
                return true;
            }
        }
        return false;
    }

    // Does this team have ALL the equipment in the list?
    public boolean hasEquipment(List<Equipment> requiredItems) {
        return equipment.containsAll(requiredItems);
    }

    // Available = not off duty AND not at full capacity
    public boolean isAvailable() {
        return status != TeamStatus.UNAVAILABLE && currentIncidents < maxIncidents;
    }

    // How many more incidents this team can take
    public int getRemainingCapacity() {
        return maxIncidents - currentIncidents;
    }

    // ----- Methods to update workload -----

    public void addIncident() {
        currentIncidents++;
        status = TeamStatus.ASSIGNED;
    }

    public void completeIncident() {
        if (currentIncidents > 0) {
            currentIncidents--;
        }
        if (currentIncidents == 0 && status == TeamStatus.ASSIGNED) {
            status = TeamStatus.AVAILABLE;
        }
    }

    // ----- Getters and setters -----

    public String getTeamId() {
        return teamId;
    }

    public List<TeamMember> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public List<String> getAnimalCategories() {
        return Collections.unmodifiableList(animalCategories);
    }

    public List<String> getSkills() {
        return Collections.unmodifiableList(skills);
    }

    public List<Equipment> getEquipment() {
        return Collections.unmodifiableList(equipment);
    }

    public TeamStatus getStatus() {
        return status;
    }

    public void setStatus(TeamStatus status) {
        this.status = status;
    }

    public int getMaxIncidents() {
        return maxIncidents;
    }

    public int getCurrentIncidents() {
        return currentIncidents;
    }

    @Override
    public String toString() {
        return teamId + " | " + status + " | workload " + currentIncidents + "/" + maxIncidents
                + " | animals " + animalCategories + " | skills " + skills
                + " | equipment " + equipment;
    }
}
package com.wildliferescue.service;

import com.wildliferescue.exception.DuplicateIncidentException;
import com.wildliferescue.exception.InvalidIncidentDataException;
import com.wildliferescue.exception.InvalidPriorityException;
import com.wildliferescue.exception.InvalidTeamException;
import com.wildliferescue.exception.RescueSystemException;
import com.wildliferescue.model.Animal;
import com.wildliferescue.model.Equipment;
import com.wildliferescue.model.IncidentStatus;
import com.wildliferescue.model.Reporter;
import com.wildliferescue.model.RescueIncident;
import com.wildliferescue.model.RescueTeam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//the central controller stores incidents and teams and connects the
//priorityCalculator and TeamMatcher together.
public class IncidentManager {

    //maps keep the ID as the key, so finding by ID is fast and duplicates are easy to spot
    private Map<String, RescueIncident> incidents;
    private Map<String, RescueTeam> teams;

    //composition the manager creates and owns these two helpers
    private PriorityCalculator priorityCalculator;
    private TeamMatcher teamMatcher;

    private int nextIdNumber;

    public IncidentManager() {
        this.incidents = new LinkedHashMap<>();   //keeps the order incidents were added
        this.teams = new LinkedHashMap<>();
        this.priorityCalculator = new PriorityCalculator();
        this.teamMatcher = new TeamMatcher();
        this.nextIdNumber = 1;
    }

    //Reporting

    //use case Report Incident
    public RescueIncident reportIncident(Animal animal, String location, boolean highRiskLocation,
                                         int numberOfAnimals, boolean immediateDanger,
                                         Reporter reporter, List<Equipment> requiredEquipment)
            throws RescueSystemException {

        String id = generateIncidentId();
        RescueIncident incident = new RescueIncident(id, animal, location, highRiskLocation,
                numberOfAnimals, immediateDanger, reporter);

        if (requiredEquipment != null) {
            for (Equipment item : requiredEquipment) {
                incident.addRequiredEquipment(item);
            }
        }

        priorityCalculator.assess(incident);   //sets score and category
        addIncident(incident);
        return incident;
    }

    //stores an existing incident
    public void addIncident(RescueIncident incident) throws DuplicateIncidentException {
        if (incidents.containsKey(incident.getIncidentId())) {
            throw new DuplicateIncidentException(
                    "Incident ID " + incident.getIncidentId() + " already exists.");
        }
        incidents.put(incident.getIncidentId(), incident);
    }

    //creates the next unused ID
    private String generateIncidentId() {
        String id;
        do {
            id = String.format("INC%03d", nextIdNumber++);
        } while (incidents.containsKey(id));
        return id;
    }

    //teams

    //use case Register Rescue Team
    public void registerTeam(RescueTeam team) throws InvalidTeamException {
        if (team == null) {
            throw new InvalidTeamException("Team information is missing.");
        }
        if (teams.containsKey(team.getTeamId())) {
            throw new InvalidTeamException("Team ID " + team.getTeamId() + " already exists.");
        }
        teams.put(team.getTeamId(), team);
    }

    //Matching and assigning
    //use case Search Available Teams
    public List<RescueTeam> findTeams(String incidentId) throws InvalidIncidentDataException {
        RescueIncident incident = getIncident(incidentId);
        return teamMatcher.findSuitableTeams(incident, new ArrayList<>(teams.values()));
    }

    //use case assugn rescue team 
    public void assignTeam(String incidentId, String teamId) throws RescueSystemException {
        RescueIncident incident = getIncident(incidentId);
        RescueTeam team = teams.get(teamId);
        if (team == null) {
            throw new InvalidTeamException("Team " + teamId + " does not exist.");
        }
        incident.assignTeam(team);   // runs the availability, skill and equipment checks
    }

    //use case Update Incident Status
    public void updateStatus(String incidentId, IncidentStatus status) throws InvalidIncidentDataException {
        getIncident(incidentId).updateStatus(status);
    }

    //queries

    public RescueIncident getIncident(String incidentId) throws InvalidIncidentDataException {
        RescueIncident incident = (incidentId == null) ? null : incidents.get(incidentId.trim());
        if (incident == null) {
            throw new InvalidIncidentDataException("Incident " + incidentId + " was not found.");
        }
        return incident;
    }

    //pending incidents most urgent first
    public List<RescueIncident> getPendingIncidents() throws InvalidPriorityException {
        List<RescueIncident> pending = new ArrayList<>();
        for (RescueIncident incident : incidents.values()) {
            if (incident.isPending()) {
                if (incident.getPriority() == null) {
                    priorityCalculator.assess(incident);   // e.g. loaded without a priority
                }
                pending.add(incident);
            }
        }
        priorityCalculator.sortByUrgency(pending);
        return pending;
    }

    public List<RescueIncident> getAllIncidents() {
        return Collections.unmodifiableList(new ArrayList<>(incidents.values()));
    }

    public List<RescueTeam> getAllTeams() {
        return Collections.unmodifiableList(new ArrayList<>(teams.values()));
    }
}
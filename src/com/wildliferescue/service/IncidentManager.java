package com.wildliferescue.service;

import com.wildliferescue.exception.DuplicateIncidentException;
import com.wildliferescue.exception.InvalidIncidentDataException;
import com.wildliferescue.exception.InvalidPriorityException;
import com.wildliferescue.exception.InvalidTeamException;
import com.wildliferescue.exception.ProcessingException;
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

//the central controller stores incidents,teams and connects
//prioity classmatcher
public class IncidentManager {

    //maps keep the ID as the key, so finding by ID is fast and duplicates are easy to spot
    private Map<String, RescueIncident> incidents;
    private Map<String, RescueTeam> teams;
    private Map<String, List<RescueTeam>> suggestedTeams;   // teams found when each incident was reported

    //composition the manager creates and owns these two helpers
    private PriorityCalculator priorityCalculator;
    private TeamMatcher teamMatcher;

    private int nextIdNumber;

    public IncidentManager() {
        this.incidents = new LinkedHashMap<>();   //keeps the order incidents were added
        this.teams = new LinkedHashMap<>();
        this.suggestedTeams = new LinkedHashMap<>();
        this.priorityCalculator = new PriorityCalculator();
        this.teamMatcher = new TeamMatcher();
        this.nextIdNumber = 1;
    }

    //reporting part
    // Use case "Report Incident" (activity diagram, Figure 3.1):
    // 1. generate a unique ID and record the incident as Pending Assignment
    // 2. FORK: calculate the priority and search for teams at the same time
    // 3. JOIN: wait for both to finish
    public RescueIncident reportIncident(Animal animal, String location, boolean highRiskLocation,
                                         int numberOfAnimals, boolean immediateDanger,
                                         Reporter reporter, List<Equipment> requiredEquipment)
            throws RescueSystemException {

        RescueIncident incident = recordNewIncident(animal, location, highRiskLocation,
                numberOfAnimals, immediateDanger, reporter, requiredEquipment);

        assessAndSearchConcurrently(incident);
        return incident;
    }

    //synchronized so two threads can never get the same ID
    private synchronized RescueIncident recordNewIncident(Animal animal, String location,
                                                          boolean highRiskLocation, int numberOfAnimals,
                                                          boolean immediateDanger, Reporter reporter,
                                                          List<Equipment> requiredEquipment)
            throws RescueSystemException {

        String id = nextFreeId();
        RescueIncident incident = new RescueIncident(id, animal, location, highRiskLocation,
                numberOfAnimals, immediateDanger, reporter);   // throws if the details are invalid

        if (requiredEquipment != null) {
            for (Equipment item : requiredEquipment) {
                incident.addRequiredEquipment(item);
            }
        }

        incidents.put(id, incident);   //only stored once it is valid so no ID is wasted here
        return incident;
    }

    //two threads run at the same time after that then we wait for both
    private void assessAndSearchConcurrently(RescueIncident incident) throws RescueSystemException {
        List<RescueTeam> teamList = getTeamSnapshot();
        List<RescueTeam> found = new ArrayList<>();

        //lambdas can only use variables that never change so the errors are kept in an array
        RescueSystemException[] errors = new RescueSystemException[2];

        Thread priorityThread = new Thread(() -> {
            try {
                priorityCalculator.assess(incident);
            } catch (RescueSystemException e) {
                errors[0] = e;
            }
        }, "priority-" + incident.getIncidentId());

        Thread searchThread = new Thread(() -> {
            try {
                found.addAll(teamMatcher.findSuitableTeams(incident, teamList));
            } catch (RescueSystemException e) {
                errors[1] = e;
            }
        }, "search-" + incident.getIncidentId());

        //here both threads start running at the same time
        priorityThread.start();
        searchThread.start();

        //wait here until BOTH threads have finished
        try {
            priorityThread.join();
            searchThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   //keep the "interrupted" flag set
            throw new ProcessingException("Processing of " + incident.getIncidentId()
                    + " was interrupted.", e);
        }

        if (errors[0] != null) {
            throw errors[0];
        }
        if (errors[1] != null) {
            throw errors[1];
        }
        saveSuggestions(incident.getIncidentId(), found);
    }

    private synchronized void saveSuggestions(String incidentId, List<RescueTeam> found) {
        suggestedTeams.put(incidentId, found);
    }

    //teams that were suitable when the incident was reported
    public synchronized List<RescueTeam> getSuggestedTeams(String incidentId) {
        List<RescueTeam> found = suggestedTeams.get(incidentId);
        if (found == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(found));
    }

    //stores an existing incident
    public synchronized void addIncident(RescueIncident incident) throws DuplicateIncidentException {
        if (incidents.containsKey(incident.getIncidentId())) {
            throw new DuplicateIncidentException(
                    "Incident ID " + incident.getIncidentId() + " already exists.");
        }
        incidents.put(incident.getIncidentId(), incident);
    }

    //finds the next unused ID
    private String nextFreeId() {
        while (incidents.containsKey(String.format("INC%03d", nextIdNumber))) {
            nextIdNumber++;
        }
        return String.format("INC%03d", nextIdNumber);
    }

    //teams

    //usse case "Register Rescue Team"
    public synchronized void registerTeam(RescueTeam team) throws InvalidTeamException {
        if (team == null) {
            throw new InvalidTeamException("Team information is missing.");
        }
        if (teams.containsKey(team.getTeamId())) {
            throw new InvalidTeamException("Team ID " + team.getTeamId() + " already exists.");
        }
        teams.put(team.getTeamId(), team);
    }

    //a copy of the team list, so a search can run without blocking other threads
    private synchronized List<RescueTeam> getTeamSnapshot() {
        return new ArrayList<>(teams.values());
    }

    //matching and assigning

    // Use case "Search Available Teams".
    // NOT synchronized: the search works on a copy of the team list, so many
    // searches can run at the same time. The result is only a suggestion -
    // assignTeam() checks the team again before assigning it.
    public List<RescueTeam> findTeams(String incidentId) throws InvalidIncidentDataException {
        RescueIncident incident = getIncident(incidentId);
        return teamMatcher.findSuitableTeams(incident, getTeamSnapshot());
    }

    //usse case "Assign Rescue Team".
    //two threads can never assign the same team past its maximum workload
    public synchronized void assignTeam(String incidentId, String teamId) throws RescueSystemException {
        RescueIncident incident = getIncident(incidentId);
        RescueTeam team = teams.get(teamId);
        if (team == null) {
            throw new InvalidTeamException("Team " + teamId + " does not exist.");
        }
        incident.assignTeam(team);   //runs the availability, skill and equipment checks
    }

    //usse case "Update Incident Status"
    public synchronized void updateStatus(String incidentId, IncidentStatus status)
            throws InvalidIncidentDataException {
        getIncident(incidentId).updateStatus(status);
    }

    //quarie

    public synchronized RescueIncident getIncident(String incidentId) throws InvalidIncidentDataException {
        RescueIncident incident = (incidentId == null) ? null : incidents.get(incidentId.trim());
        if (incident == null) {
            throw new InvalidIncidentDataException("Incident " + incidentId + " was not found.");
        }
        return incident;
    }

    //pending incidents, most urgent first
    public synchronized List<RescueIncident> getPendingIncidents() throws InvalidPriorityException {
        List<RescueIncident> pending = new ArrayList<>();
        for (RescueIncident incident : incidents.values()) {
            if (incident.isPending()) {
                if (incident.getPriority() == null) {
                    priorityCalculator.assess(incident);   //loaded without a priority
                }
                pending.add(incident);
            }
        }
        priorityCalculator.sortByUrgency(pending);
        return pending;
    }

    public synchronized List<RescueIncident> getAllIncidents() {
        return Collections.unmodifiableList(new ArrayList<>(incidents.values()));
    }

    public synchronized List<RescueTeam> getAllTeams() {
        return Collections.unmodifiableList(new ArrayList<>(teams.values()));
    }
}
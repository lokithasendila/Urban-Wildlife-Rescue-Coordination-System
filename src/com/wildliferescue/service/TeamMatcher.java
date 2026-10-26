package com.wildliferescue.service;

import com.wildliferescue.exception.InvalidIncidentDataException;
import com.wildliferescue.model.RescueIncident;
import com.wildliferescue.model.RescueTeam;
import com.wildliferescue.model.TeamStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

//finds the rescue teams that suit an incident and ranks them best first.
public class TeamMatcher {

    //returns the suitable teams like most suitable first.
    //an empty list means no team fits and so the incident stays pending.
    public List<RescueTeam> findSuitableTeams(RescueIncident incident, List<RescueTeam> teams)
            throws InvalidIncidentDataException {

        if (incident == null) {
            throw new InvalidIncidentDataException("Cannot search for teams: incident is missing.");
        }
        if (teams == null) {
            throw new InvalidIncidentDataException("Cannot search for teams: team list is missing.");
        }
        if (!incident.isPending()) {
            throw new InvalidIncidentDataException("Incident " + incident.getIncidentId()
                    + " is already " + incident.getStatus() + ".");
        }

        List<RescueTeam> suitable = new ArrayList<>();

        for (RescueTeam team : teams) {
            if (isSuitable(incident, team)) {
                suitable.add(team);
            }
        }

        //highest match score first and if tied sort by team ID so the order is predictable
        suitable.sort(Comparator
                .comparingInt((RescueTeam t) -> calculateMatchScore(t)).reversed()
                .thenComparing(RescueTeam::getTeamId));

        return suitable;
    }

    //returns the best team  or an empty Optional if there is none
    public Optional<RescueTeam> findBestTeam(RescueIncident incident, List<RescueTeam> teams)
            throws InvalidIncidentDataException {

        List<RescueTeam> suitable = findSuitableTeams(incident, teams);
        if (suitable.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(suitable.get(0));
    }

    //a team is suitable only if it passes all four checks
    private boolean isSuitable(RescueIncident incident, RescueTeam team) {
        return team != null
                && team.isAvailable()
                && team.canHandle(incident.getAnimal().getAnimalType())
                && team.hasSkill(incident.getRequiredSkill())
                && team.hasEquipment(incident.getRequiredEquipment());
    }

    //more spare capacity is better and a team with no work gets a bonus
    private int calculateMatchScore(RescueTeam team) {
        int score = team.getRemainingCapacity() * 2;
        if (team.getStatus() == TeamStatus.AVAILABLE) {
            score += 3;
        }
        return score;
    }
}
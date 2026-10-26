package com.wildliferescue;

import com.wildliferescue.exception.RescueSystemException;
import com.wildliferescue.model.Animal;
import com.wildliferescue.model.AnimalCondition;
import com.wildliferescue.model.Equipment;
import com.wildliferescue.model.Reporter;
import com.wildliferescue.model.RescueIncident;
import com.wildliferescue.model.RescueTeam;
import com.wildliferescue.model.TeamStatus;
import com.wildliferescue.service.TeamMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {

        TeamMatcher matcher = new TeamMatcher();

        try {
            // ----- Build five teams -----
            List<RescueTeam> teams = new ArrayList<>();
            teams.add(makeTeam("T001", 2, "Bird", "Bird Handling", true));     //perfect no work
            teams.add(makeTeam("T002", 2, "Bird", "Bird Handling", false));    //no ladder
            teams.add(makeTeam("T003", 2, "Mammal", "Mammal Handling", true)); //wrong animal
            RescueTeam offDuty = makeTeam("T004", 2, "Bird", "Bird Handling", true);
            offDuty.setStatus(TeamStatus.UNAVAILABLE);                         //off the duty
            teams.add(offDuty);
            RescueTeam busy = makeTeam("T005", 3, "Bird", "Bird Handling", true);
            busy.addIncident();                                                // already has 1 incident
            teams.add(busy);

            Reporter reporter = new Reporter("Nimal Perera", "0771234567", "nimal@gmail.com");

            // ----- Test 1: bird on a roof needing a ladder -----
            System.out.println("--- Test 1: trapped bird needing a ladder ---");
            RescueIncident birdIncident = new RescueIncident("INC001",
                    Animal.create("Bird", AnimalCondition.TRAPPED), "Roof, Nugegoda", true, 1, true, reporter);
            birdIncident.addRequiredEquipment(new Equipment("Ladder"));

            List<RescueTeam> suitable = matcher.findSuitableTeams(birdIncident, teams);
            for (RescueTeam t : suitable) {
                System.out.println("Suitable: " + t.getTeamId() + " (workload "
                        + t.getCurrentIncidents() + "/" + t.getMaxIncidents() + ")");
            }

            Optional<RescueTeam> best = matcher.findBestTeam(birdIncident, teams);
            System.out.println("Best team: " + (best.isPresent() ? best.get().getTeamId() : "none"));

            // ----- Test 2: no team can handle reptiles -----
            System.out.println("\n--- Test 2: reptile, no suitable team ---");
            RescueIncident reptileIncident = new RescueIncident("INC002",
                    Animal.create("Reptile", AnimalCondition.TRAPPED), "Drain, Borella", false, 1, false, reporter);

            List<RescueTeam> none = matcher.findSuitableTeams(reptileIncident, teams);
            System.out.println("Suitable teams found: " + none.size());
            System.out.println("Incident status: " + reptileIncident.getStatus());

            // ----- Test 3: assign the best team (uses Step 4 checks) -----
            System.out.println("\n--- Test 3: assign the best team ---");
            birdIncident.assignTeam(best.get());
            System.out.println(birdIncident);

            // ----- Test 4: searching for an incident that is already assigned -----
            System.out.println("\n--- Test 4: incident already assigned ---");
            matcher.findSuitableTeams(birdIncident, teams);

        } catch (RescueSystemException e) {
            System.out.println("ERROR [" + e.getClass().getSimpleName() + "]: " + e.getMessage());
        }
    }

    // Helper that builds a team quickly
    private static RescueTeam makeTeam(String id, int max, String animal, String skill, boolean hasLadder)
            throws RescueSystemException {
        RescueTeam team = new RescueTeam(id, max);
        team.addAnimalCategory(animal);
        team.addSkill(skill);
        if (hasLadder) {
            team.addEquipment(new Equipment("Ladder"));
        }
        return team;
    }
}
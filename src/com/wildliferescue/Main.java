package com.wildliferescue;

import com.wildliferescue.exception.InvalidAnimalTypeException;
import com.wildliferescue.model.Animal;
import com.wildliferescue.model.AnimalCondition;
import com.wildliferescue.model.Equipment;
import com.wildliferescue.model.Reporter;
import com.wildliferescue.model.RescueIncident;
import com.wildliferescue.model.RescueTeam;
import com.wildliferescue.model.TeamMember;

public class Main {

    public static void main(String[] args) {

        // ----- Create a rescue team -----
        RescueTeam team = new RescueTeam("T001", 2);
        team.addMember(new TeamMember("M001", "Kasun Fernando", "0759876543"));
        team.addAnimalCategory("Bird");
        team.addSkill("Bird Handling");
        team.addSkill("Climbing");
        team.addEquipment(new Equipment("E001", "Ladder"));
        team.addEquipment(new Equipment("E002", "Animal Carrier"));
        System.out.println("Team: " + team);

        // ----- Create an incident -----
        try {
            Reporter reporter = new Reporter("Nimal Perera", "0771234567", "nimal@gmail.com");
            Animal bird = Animal.create("Bird", AnimalCondition.TRAPPED);

            RescueIncident incident = new RescueIncident("INC001", bird, "Kandy Road, Colombo",
                    true, 1, true, reporter);
            incident.addRequiredEquipment(new Equipment("ladder"));
            System.out.println("Incident: " + incident);

            // ----- Check if the team suits the incident -----
            System.out.println("Can handle animal? " + team.canHandle(bird.getAnimalType()));
            System.out.println("Has skill? " + team.hasSkill(incident.getRequiredSkill()));
            System.out.println("Has equipment? " + team.hasEquipment(incident.getRequiredEquipment()));
            System.out.println("Is available? " + team.isAvailable());

            // ----- Assign the team -----
            incident.assignTeam(team);
            team.addIncident();
            System.out.println("After assigning: " + incident);
            System.out.println("Team now: " + team);

        } catch (InvalidAnimalTypeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
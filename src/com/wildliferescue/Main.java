package com.wildliferescue;
    
import com.wildliferescue.exception.RescueSystemException;
import com.wildliferescue.model.Animal;
import com.wildliferescue.model.AnimalCondition;
import com.wildliferescue.model.Equipment;
import com.wildliferescue.model.IncidentStatus;
import com.wildliferescue.model.Reporter;
import com.wildliferescue.model.RescueIncident;
import com.wildliferescue.model.RescueTeam;
import com.wildliferescue.service.IncidentManager;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        IncidentManager manager = new IncidentManager();

        try {
            // ----- Register two teams -----
            RescueTeam birdTeam = new RescueTeam("T001", 2);
            birdTeam.addAnimalCategory("Bird");
            birdTeam.addSkill("Bird Handling");
            birdTeam.addEquipment(new Equipment("Ladder"));
            manager.registerTeam(birdTeam);

            RescueTeam mammalTeam = new RescueTeam("T002", 1);
            mammalTeam.addAnimalCategory("Mammal");
            mammalTeam.addSkill("Mammal Handling");
            manager.registerTeam(mammalTeam);

            // ----- Report three incidents (IDs are generated automatically) -----
            Reporter reporter = new Reporter("Nimal Perera", "0771234567", "nimal@gmail.com");

            RescueIncident a = manager.reportIncident(Animal.create("Mammal", AnimalCondition.DISPLACED),
                    "Garden, Nugegoda", false, 1, false, reporter, null);
            RescueIncident b = manager.reportIncident(Animal.create("Bird", AnimalCondition.INJURED),
                    "Highway, Kadawatha", true, 1, true, reporter, List.of(new Equipment("Ladder")));
            RescueIncident c = manager.reportIncident(Animal.create("Reptile", AnimalCondition.TRAPPED),
                    "Drain, Borella", false, 3, false, reporter, null);

            System.out.println("--- Pending incidents, most urgent first ---");
            for (RescueIncident i : manager.getPendingIncidents()) {
                System.out.println(i.getIncidentId() + " | " + i.getPriority() + " (score " + i.getPriorityScore() + ")");
            }

            // ----- Find and assign teams -----
            System.out.println("\n--- Teams for " + b.getIncidentId() + " ---");
            for (RescueTeam t : manager.findTeams(b.getIncidentId())) {
                System.out.println("Suitable: " + t.getTeamId());
            }
            manager.assignTeam(b.getIncidentId(), "T001");
            System.out.println("Assigned: " + manager.getIncident(b.getIncidentId()));

            System.out.println("\n--- Teams for " + c.getIncidentId() + " (reptile) ---");
            System.out.println("Suitable teams: " + manager.findTeams(c.getIncidentId()).size()
                    + " -> stays " + c.getStatus());

            // ----- Update status -----
            manager.updateStatus(b.getIncidentId(), IncidentStatus.COMPLETED);
            System.out.println("\nAfter completion: " + b.getStatus() + ", team workload "
                    + birdTeam.getCurrentIncidents() + "/" + birdTeam.getMaxIncidents());

            System.out.println("\nStill pending: " + manager.getPendingIncidents().size());

        } catch (RescueSystemException e) {
            System.out.println("ERROR [" + e.getClass().getSimpleName() + "]: " + e.getMessage());
        }

        // ===== Error tests =====
        System.out.println("\n--- Error tests ---");
        try {
            RescueTeam duplicate = new RescueTeam("T001", 1);
            manager.registerTeam(duplicate);
        } catch (RescueSystemException e) {
            System.out.println("ERROR [" + e.getClass().getSimpleName() + "]: " + e.getMessage());
        }
        try {
            Reporter r = new Reporter("Amali", "0712345678", "amali@gmail.com");
            manager.addIncident(new RescueIncident("INC001", Animal.create("Bird", AnimalCondition.INJURED),
                    "Colombo 7", false, 1, false, r));
        } catch (RescueSystemException e) {
            System.out.println("ERROR [" + e.getClass().getSimpleName() + "]: " + e.getMessage());
        }
        try {
            manager.getIncident("INC999");
        } catch (RescueSystemException e) {
            System.out.println("ERROR [" + e.getClass().getSimpleName() + "]: " + e.getMessage());
        }
        try {
            manager.assignTeam("INC003", "T002");
        } catch (RescueSystemException e) {
            System.out.println("ERROR [" + e.getClass().getSimpleName() + "]: " + e.getMessage());
        }
    }
}
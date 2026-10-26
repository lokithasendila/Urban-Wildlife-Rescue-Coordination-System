package com.wildliferescue;

import com.wildliferescue.exception.RescueSystemException;
import com.wildliferescue.model.Animal;
import com.wildliferescue.model.AnimalCondition;
import com.wildliferescue.model.Reporter;
import com.wildliferescue.model.RescueIncident;
import com.wildliferescue.service.PriorityCalculator;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        PriorityCalculator calculator = new PriorityCalculator();
        List<RescueIncident> incidents = new ArrayList<>();

        try {
            Reporter r = new Reporter("Nimal Perera", "0771234567", "nimal@gmail.com");

            // INC001: injured bird, danger, high-risk location, 1 animal
            incidents.add(new RescueIncident("INC001", Animal.create("Bird", AnimalCondition.INJURED),
                    "Highway, Kadawatha", true, 1, true, r));
            // INC002: displaced mammal, no danger, safe place, 1 animal
            incidents.add(new RescueIncident("INC002", Animal.create("Mammal", AnimalCondition.DISPLACED),
                    "Garden, Nugegoda", false, 1, false, r));
            // INC003: trapped reptile, no danger, 3 animals
            incidents.add(new RescueIncident("INC003", Animal.create("Reptile", AnimalCondition.TRAPPED),
                    "Drain, Borella", false, 3, false, r));
            // INC004: abandoned birds, 6 animals, danger
            incidents.add(new RescueIncident("INC004", Animal.create("Bird", AnimalCondition.ABANDONED),
                    "Roof, Dehiwala", false, 6, true, r));

            for (RescueIncident incident : incidents) {
                calculator.assess(incident);
            }

            System.out.println("--- Before sorting ---");
            for (RescueIncident i : incidents) {
                System.out.println(i.getIncidentId() + " score " + i.getPriorityScore() + " -> " + i.getPriority());
            }

            calculator.sortByUrgency(incidents);

            System.out.println("\n--- After sorting (most urgent first) ---");
            for (RescueIncident i : incidents) {
                System.out.println(i.getIncidentId() + " score " + i.getPriorityScore() + " -> " + i.getPriority());
            }

            // ----- Invalid priority tests -----
            System.out.println("\n--- Invalid priority tests ---");
            try {
                calculator.classify(-5);
            } catch (RescueSystemException e) {
                System.out.println("ERROR [" + e.getClass().getSimpleName() + "]: " + e.getMessage());
            }
            try {
                calculator.assess(null);
            } catch (RescueSystemException e) {
                System.out.println("ERROR [" + e.getClass().getSimpleName() + "]: " + e.getMessage());
            }

        } catch (RescueSystemException e) {
            System.out.println("ERROR [" + e.getClass().getSimpleName() + "]: " + e.getMessage());
        }
    }
}
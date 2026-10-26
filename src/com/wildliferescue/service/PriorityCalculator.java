package com.wildliferescue.service;

import com.wildliferescue.exception.InvalidPriorityException;
import com.wildliferescue.model.AnimalCondition;
import com.wildliferescue.model.PriorityLevel;
import com.wildliferescue.model.RescueIncident;

import java.util.Comparator;
import java.util.List;

// Calculates a priority score for an incident and classifies it as crtical high medium or low
public class PriorityCalculator {

    // Score thresholds
    private static final int CRITICAL_MIN = 12;
    private static final int HIGH_MIN = 9;
    private static final int MEDIUM_MIN = 6;

    private static final int DANGER_POINTS = 4;
    private static final int HIGH_RISK_LOCATION_POINTS = 2;
    //work out the score
    public int calculateScore(RescueIncident incident) throws InvalidPriorityException {
        if (incident == null) {
            throw new InvalidPriorityException("Cannot calculate priority: incident is missing.");
        }
        if (incident.getAnimal().getCondition() == null) {
            throw new InvalidPriorityException("Incident " + incident.getIncidentId()
                    + ": animal condition is missing.");
        }

        int score = 0;

        score += conditionPoints(incident.getAnimal().getCondition());   
        score += incident.getAnimal().getRiskFactor();                   
        score += animalCountPoints(incident.getNumberOfAnimals());       

        if (incident.isImmediateDanger()) {
            score += DANGER_POINTS;                                     
        }
        if (incident.isHighRiskLocation()) {
            score += HIGH_RISK_LOCATION_POINTS;                       
        }
        return score;
    }

    //turn the score into a category
    public PriorityLevel classify(int score) throws InvalidPriorityException {
        if (score < 0) {
            throw new InvalidPriorityException("Priority score cannot be negative (got " + score + ").");
        }
        if (score >= CRITICAL_MIN) {
            return PriorityLevel.CRITICAL;
        } else if (score >= HIGH_MIN) {
            return PriorityLevel.HIGH;
        } else if (score >= MEDIUM_MIN) {
            return PriorityLevel.MEDIUM;
        }
        return PriorityLevel.LOW;
    }

    //does both steps and stores the result inside the incident
    public void assess(RescueIncident incident) throws InvalidPriorityException {
        int score = calculateScore(incident);
        incident.setPriority(score, classify(score));
    }

    //sorts a list so the most urgent incidents come first
    public void sortByUrgency(List<RescueIncident> incidents) {
        incidents.sort(Comparator
                .comparing(RescueIncident::getPriority)                       // CRITICAL first
                .thenComparing(RescueIncident::getPriorityScore, Comparator.reverseOrder()));
    }

    //private helper methods 

    private int conditionPoints(AnimalCondition condition) {
        switch (condition) {
            case INJURED:   return 4;
            case TRAPPED:   return 3;
            case DISPLACED: return 2;
            case ABANDONED: return 2;
            default:        return 0;
        }
    }

    private int animalCountPoints(int count) {
        if (count >= 5) {
            return 3;
        } else if (count >= 2) {
            return 2;
        }
        return 1;
    }
}
package com.wildliferescue.exception;

/**
 * Thrown when a reported animal type is not recognised by the system.
 * TODO (Part 4): add more custom exceptions here (DuplicateIncidentIdException,
 * MissingLocationException, InvalidTeamAssignmentException, etc.)
 */
public class InvalidAnimalTypeException extends Exception {
    public InvalidAnimalTypeException(String message) {
        super(message);
    }
}

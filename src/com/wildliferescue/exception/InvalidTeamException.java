package com.wildliferescue.exception;

// Thrown when rescue team details are invalid, or a team does not suit an incident.
public class InvalidTeamException extends RescueSystemException {

    public InvalidTeamException(String message) {
        super(message);
    }
}
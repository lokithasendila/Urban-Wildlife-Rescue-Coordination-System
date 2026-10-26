package com.wildliferescue.exception;

// Thrown when trying to assign a team that is unavailable or already full.
public class TeamUnavailableException extends RescueSystemException {

    public TeamUnavailableException(String message) {
        super(message);
    }
}
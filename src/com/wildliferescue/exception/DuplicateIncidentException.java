package com.wildliferescue.exception;

//thrown when an incident ID already exists in the system.
public class DuplicateIncidentException extends RescueSystemException {

    public DuplicateIncidentException(String message) {
        super(message);
    }
}
package com.wildliferescue.exception;

// Thrown when incident details are invalid (e.g. 0 animals, bad phone number, wrong status change).
public class InvalidIncidentDataException extends RescueSystemException {

    public InvalidIncidentDataException(String message) {
        super(message);
    }
}
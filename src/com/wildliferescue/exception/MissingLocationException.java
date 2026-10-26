package com.wildliferescue.exception;

// Thrown when an incident is reported without a location.
public class MissingLocationException extends RescueSystemException {

    public MissingLocationException(String message) {
        super(message);
    }
}
package com.wildliferescue.exception;

// Thrown when priority information is invalid.
//Invalid priority information
public class InvalidPriorityException extends RescueSystemException {

    public InvalidPriorityException(String message) {
        super(message);
    }
}
package com.wildliferescue.exception;

// Thrown when trying to assign a team that does not have the required equipment.
public class MissingEquipmentException extends RescueSystemException {

    public MissingEquipmentException(String message) {
        super(message);
    }
}
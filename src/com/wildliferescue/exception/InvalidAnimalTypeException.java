package com.wildliferescue.exception;

//error handling when the animal type is not a Bird mamael or reptile
public class InvalidAnimalTypeException extends RescueSystemException {

    public InvalidAnimalTypeException(String message) {
        super(message);
    }
}   
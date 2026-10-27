package com.wildliferescue.exception;

//thrown when the data inside a CSV file is invalid
public class InvalidCsvDataException extends RescueSystemException {

    public InvalidCsvDataException(String message) {
        super(message);
    }
}
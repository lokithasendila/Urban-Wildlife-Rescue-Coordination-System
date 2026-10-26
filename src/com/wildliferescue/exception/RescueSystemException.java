package com.wildliferescue.exception;

//this is the parent class for the all custome exceptions
//this is for catching the rescusystemexpception
public class RescueSystemException extends Exception {

    public RescueSystemException(String message) {
        super(message);
    }
}
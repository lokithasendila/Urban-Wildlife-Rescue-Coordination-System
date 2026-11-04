package com.wildliferescue.exception;

//thrown when concurrent processing fails or is interrupted.
public class ProcessingException extends RescueSystemException {

    public ProcessingException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
package com.wildliferescue.exception;

//thrown when a file cannot be read or written
public class FileOperationException extends RescueSystemException {

    public FileOperationException(String message, Throwable cause) {
        super(message);
        initCause(cause);   //keeps the original IOException so the real reason is not lost
    }
}
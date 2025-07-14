package com.pipemasters.server.exceptions.trainSchedule;

public class TrainParsingException extends RuntimeException {
    public TrainParsingException(String message) {
        super(message);
    }

    public TrainParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
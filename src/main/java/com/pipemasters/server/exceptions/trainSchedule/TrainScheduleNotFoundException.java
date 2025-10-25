package com.pipemasters.server.exceptions.trainSchedule;

public class TrainScheduleNotFoundException extends RuntimeException {
    public TrainScheduleNotFoundException(String message) {
        super(message);
    }
}

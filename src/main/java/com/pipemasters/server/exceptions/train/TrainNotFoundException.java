package com.pipemasters.server.exceptions.train;

import com.pipemasters.server.exceptions.ResourceNotFoundException;

public class TrainNotFoundException extends ResourceNotFoundException {
    public TrainNotFoundException(String message) {
        super(message);
    }

    public TrainNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

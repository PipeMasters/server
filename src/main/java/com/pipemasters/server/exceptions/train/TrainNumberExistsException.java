package com.pipemasters.server.exceptions.train;

import com.pipemasters.server.exceptions.ResourceNotFoundException;

public class TrainNumberExistsException extends ResourceNotFoundException {
    public TrainNumberExistsException(String message) {
        super(message);
    }

    public TrainNumberExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

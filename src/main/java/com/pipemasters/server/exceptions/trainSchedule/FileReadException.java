package com.pipemasters.server.exceptions.trainSchedule;

import java.io.IOException;

public class FileReadException extends IOException {
    public FileReadException(String message) {
        super(message);
    }

    public FileReadException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.pipemasters.server.exceptions.file;

import org.apache.kafka.common.errors.ResourceNotFoundException;

public class MediaFileNotFoundException extends ResourceNotFoundException {
    public MediaFileNotFoundException(String message) {
        super(message);
    }

    public MediaFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

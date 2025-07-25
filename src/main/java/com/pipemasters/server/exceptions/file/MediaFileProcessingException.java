package com.pipemasters.server.exceptions.file;

public class MediaFileProcessingException extends RuntimeException {
    public MediaFileProcessingException(String message) {
        super(message);
    }

    public MediaFileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

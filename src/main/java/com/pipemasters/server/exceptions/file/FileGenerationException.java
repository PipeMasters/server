package com.pipemasters.server.exceptions.file;

public class FileGenerationException extends RuntimeException {
    public FileGenerationException(String message) {
        super(message);
    }

    public FileGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.pipemasters.server.exceptions.file;

import com.pipemasters.server.exceptions.ResourceNotFoundException;

public class UploadBatchNotFoundException extends ResourceNotFoundException {
    public UploadBatchNotFoundException(String message) {
        super(message);
    }

    public UploadBatchNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

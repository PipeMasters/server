package com.pipemasters.server.exceptions.branch;

public class BranchHasChildrenException extends RuntimeException {

    public BranchHasChildrenException(String message) {
        super(message);
    }

    public BranchHasChildrenException(String message, Throwable cause) {
        super(message, cause);
    }
}

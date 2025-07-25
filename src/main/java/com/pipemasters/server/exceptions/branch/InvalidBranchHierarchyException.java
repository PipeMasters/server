package com.pipemasters.server.exceptions.branch;

public class InvalidBranchHierarchyException extends IllegalArgumentException {
    public InvalidBranchHierarchyException(String message) {
        super(message);
    }

    public InvalidBranchHierarchyException(String message, Throwable cause) {
        super(message, cause);
    }
}

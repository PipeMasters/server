package com.pipemasters.server.exceptions.branch;

public class InvalidBranchLevelException extends RuntimeException {
    public InvalidBranchLevelException(String message) {
        super(message);
    }

  public InvalidBranchLevelException(String message, Throwable cause) {
    super(message, cause);
  }
}

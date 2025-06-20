package com.pipemasters.server.exceptions.branch;

import com.pipemasters.server.exceptions.ResourceNotFoundException;

public class BranchNotFoundException extends ResourceNotFoundException {
  public BranchNotFoundException(String message) {
    super(message);
  }

  public BranchNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}

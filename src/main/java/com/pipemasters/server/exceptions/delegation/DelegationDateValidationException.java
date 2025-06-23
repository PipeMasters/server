package com.pipemasters.server.exceptions.delegation;

public class DelegationDateValidationException extends IllegalArgumentException {
  public DelegationDateValidationException(String message) {
    super(message);
  }

  public DelegationDateValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}

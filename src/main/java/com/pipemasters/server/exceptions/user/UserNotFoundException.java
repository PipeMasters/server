package com.pipemasters.server.exceptions.user;

import com.pipemasters.server.exceptions.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {
  public UserNotFoundException(String message) {
    super(message);
  }

  public UserNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}

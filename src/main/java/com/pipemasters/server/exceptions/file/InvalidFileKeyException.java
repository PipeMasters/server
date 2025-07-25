package com.pipemasters.server.exceptions.file;

public class InvalidFileKeyException extends IllegalArgumentException {
  public InvalidFileKeyException(String message) {
    super(message);
  }

  public InvalidFileKeyException(String message, Throwable cause) {
    super(message, cause);
  }
}

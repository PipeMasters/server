package com.pipemasters.server.exceptions.imotio;

public class ImotioProcessingException extends RuntimeException {
  public ImotioProcessingException(String message) {
    super(message);
  }

  public ImotioProcessingException(String message, Throwable cause) {
    super(message, cause);
  }
}

package com.pipemasters.server.exceptions.imotio;

public class ImotioResponseParseException extends ImotioProcessingException {
  public ImotioResponseParseException(String message) {
    super(message);
  }

  public ImotioResponseParseException(String message, Throwable cause) {
    super(message, cause);
  }
}

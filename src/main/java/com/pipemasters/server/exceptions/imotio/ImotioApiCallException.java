package com.pipemasters.server.exceptions.imotio;

import org.springframework.http.HttpStatusCode;

public class ImotioApiCallException extends ImotioProcessingException {
    private final HttpStatusCode statusCode;
    private final String responseBody;

    public ImotioApiCallException(String message, HttpStatusCode statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public ImotioApiCallException(String message, Throwable cause, HttpStatusCode statusCode, String responseBody) {
        super(message, cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}

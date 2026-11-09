package org.heigit.ors.benchmark.exceptions;

public class RequestBodyCreationException extends RuntimeException {
    public RequestBodyCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestBodyCreationException(String message) {
        super(message);
    }
}

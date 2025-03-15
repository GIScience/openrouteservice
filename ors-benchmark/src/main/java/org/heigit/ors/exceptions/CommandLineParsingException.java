package org.heigit.ors.exceptions;

public class CommandLineParsingException extends RuntimeException {
    public CommandLineParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
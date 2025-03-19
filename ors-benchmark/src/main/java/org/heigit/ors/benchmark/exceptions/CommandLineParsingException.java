package org.heigit.ors.benchmark.exceptions;

public class CommandLineParsingException extends RuntimeException {
    public CommandLineParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
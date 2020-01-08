package org.heigit.ors.exceptions;

import org.heigit.ors.common.StatusCode;

public class UnknownParameterException extends StatusCodeException {
    private static final long serialVersionUID = 4866998272349837464L;

    public UnknownParameterException(int errorCode, String paramName)
    {
        super(StatusCode.BAD_REQUEST, errorCode, "Unknown parameter '" + paramName + "'.");
    }
}

package heigit.ors.exceptions;

import heigit.ors.common.StatusCode;

public class IncompatableParameterException extends StatusCodeException {
    private static final long serialVersionUID = 1527301848566685803L;

    public IncompatableParameterException(int errorCode, String paramName, String paramValue, String invalidWithName, String invalidWithValue)
    {
        super(StatusCode.BAD_REQUEST, errorCode, paramName + " - " + paramValue + " is not valid with " + invalidWithName + " - " + invalidWithValue);
    }
}

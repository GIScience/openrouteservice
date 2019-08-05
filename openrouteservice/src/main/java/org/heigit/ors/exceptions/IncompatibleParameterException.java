package heigit.ors.exceptions;

import heigit.ors.common.StatusCode;

public class IncompatibleParameterException extends StatusCodeException {
    private static final long serialVersionUID = 1527301848566685803L;

    public IncompatibleParameterException(int errorCode, String paramName, String paramValue, String invalidWithName, String invalidWithValue)
    {
        super(StatusCode.BAD_REQUEST, errorCode, paramName + " - " + paramValue + " is not valid with " + invalidWithName + " - " + invalidWithValue);
    }

    public IncompatibleParameterException(int errorCode, String param1, String param2)
    {
        super(StatusCode.BAD_REQUEST, errorCode, "Parameter '" + param1 + "' is incompatible with parameter '" + param2 + "'.");
    }
}

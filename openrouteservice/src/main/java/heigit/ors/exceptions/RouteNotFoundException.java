package heigit.ors.exceptions;

import heigit.ors.common.StatusCode;

public class RouteNotFoundException extends StatusCodeException {

    private static final long serialVersionUID = 3965768339351489620L;

    public RouteNotFoundException(int errorCode, String message) {
        super(StatusCode.NOT_FOUND, errorCode, "Route could not be found - " + message);
    }

    public RouteNotFoundException(int errorCode) {
        this(errorCode, "");
    }

    public RouteNotFoundException()
    {
        this(0);
    }
}

package org.heigit.ors.exceptions;

import org.heigit.ors.common.StatusCode;
import org.heigit.ors.routing.RoutingErrorCodes;

public class PointNotFoundException extends StatusCodeException {

    public PointNotFoundException(String message) {
        super(StatusCode.NOT_FOUND, RoutingErrorCodes.POINT_NOT_FOUND, message);
    }

    public PointNotFoundException(String message, int errorCode) {
        super(StatusCode.NOT_FOUND, errorCode, message);
    }
}

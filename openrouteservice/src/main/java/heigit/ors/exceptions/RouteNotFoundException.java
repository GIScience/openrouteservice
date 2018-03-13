package heigit.ors.exceptions;

public class RouteNotFoundException extends StatusCodeException {

    private static final long serialVersionUID = 3965768339351489620L;

    public RouteNotFoundException(int errorCode, String message) {
        // TODO: need to change the 404 to reference the error code constant when merge for geocoding error is done
        super(404, errorCode, "Route could not be found - " + message);
    }

    public RouteNotFoundException(int errorCode) {
        this(errorCode, "");
    }

    public RouteNotFoundException()
    {
        this(0);
    }
}

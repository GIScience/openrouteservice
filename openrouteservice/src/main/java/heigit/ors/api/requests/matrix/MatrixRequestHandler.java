package heigit.ors.api.requests.matrix;

import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.matrix.MatrixErrorCodes;
import heigit.ors.matrix.MatrixRequest;
import heigit.ors.matrix.MatrixResult;
import heigit.ors.routing.RoutingProfileManager;

public class MatrixRequestHandler {
    public static MatrixResult generateRouteFromRequest(SpringMatrixRequest request) throws StatusCodeException {
        MatrixRequest matrixRequest = convertMatrixRequest(request);
        try {
            MatrixResult result = RoutingProfileManager.getInstance().computeMatrix(matrixRequest);
            return result;
        } catch (Exception e) {
            if (e instanceof StatusCodeException)
                throw (StatusCodeException) e;

            throw new StatusCodeException(MatrixErrorCodes.UNKNOWN);
        }
    }

    private static MatrixRequest convertMatrixRequest(SpringMatrixRequest request) {
        //TODO Next
        MatrixRequest matrixRequest = new MatrixRequest();

        return null;
    }
}

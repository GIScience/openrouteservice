package org.heigit.ors.api.requests.centrality;

import com.google.common.primitives.Doubles;
import com.graphhopper.util.shapes.BBox;
import org.heigit.ors.api.requests.common.GenericHandler;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.centrality.CentralityResult;
import org.heigit.ors.centrality.CentralityErrorCodes;
import org.heigit.ors.routing.RoutingProfileManager;

import java.util.List;

public class CentralityRequestHandler extends GenericHandler {
    public CentralityRequestHandler() {
        super();
        this.errorCodes.put("UNKNOWN_PARAMETER", CentralityErrorCodes.UNKNOWN_PARAMETER);
    }

    public CentralityResult generateCentralityFromRequest(CentralityRequest request) throws StatusCodeException {
        org.heigit.ors.centrality.CentralityRequest centralityRequest = convertCentralityRequest(request);

        try {
            return RoutingProfileManager.getInstance().computeCentrality(centralityRequest);
        } catch (StatusCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new StatusCodeException(StatusCode.INTERNAL_SERVER_ERROR, CentralityErrorCodes.UNKNOWN);
        }
    }

    private org.heigit.ors.centrality.CentralityRequest convertCentralityRequest(CentralityRequest request) throws StatusCodeException {
        org.heigit.ors.centrality.CentralityRequest centralityRequest = new org.heigit.ors.centrality.CentralityRequest();

        if  (request.hasId())
            centralityRequest.setId(request.getId());

        int profileType = -1;

        try {
            profileType = convertRouteProfileType(request.getProfile());
            centralityRequest.setProfileType(profileType);
        } catch (Exception e) {
            throw new ParameterValueException(CentralityErrorCodes.INVALID_PARAMETER_VALUE, CentralityRequest.PARAM_PROFILE);
        }

        centralityRequest.setBoundingBox(convertBBox(request.getBbox()));

        centralityRequest.setMode(request.getMode().toString());

        if (request.hasExcludeNodes()) {
            centralityRequest.setExcludeNodes(request.getExcludeNodes());
        }

        return centralityRequest;
    }

    BBox convertBBox(List<List<Double>> coordinates) throws ParameterValueException {
        if (coordinates.size() != 2) {
            throw new ParameterValueException(CentralityErrorCodes.INVALID_PARAMETER_VALUE, CentralityRequest.PARAM_BBOX);
        }

        double[] coords = {};

        for (List<Double> coord : coordinates) {
            coords = Doubles.concat(coords, convertSingleCoordinate(coord));
        }

        return new BBox(coords);
    }

    private double[] convertSingleCoordinate(List<Double> coordinate) throws ParameterValueException {
        if (coordinate.size() != 2) {
            throw new ParameterValueException(CentralityErrorCodes.INVALID_PARAMETER_VALUE, CentralityRequest.PARAM_BBOX);
        }

        return new double[]{coordinate.get(0), coordinate.get(1)};
    }

}

package org.heigit.ors.api.requests.export;

import com.google.common.primitives.Doubles;
import com.graphhopper.util.shapes.BBox;
import org.heigit.ors.api.requests.common.GenericHandler;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.export.ExportErrorCodes;
import org.heigit.ors.export.ExportResult;
import org.heigit.ors.routing.RoutingProfileManager;

import java.util.List;

public class ExportRequestHandler extends GenericHandler {
    public ExportRequestHandler() {
        super();
        this.errorCodes.put("UNKNOWN_PARAMETER", ExportErrorCodes.UNKNOWN_PARAMETER);
    }

    public ExportResult generateExportFromRequest(ExportRequest request) throws StatusCodeException {
        org.heigit.ors.export.ExportRequest exportRequest = convertExportRequest(request);

        try {
            return RoutingProfileManager.getInstance().computeExport(exportRequest);
        } catch (StatusCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new StatusCodeException(StatusCode.INTERNAL_SERVER_ERROR, ExportErrorCodes.UNKNOWN);
        }
    }

    private org.heigit.ors.export.ExportRequest convertExportRequest(ExportRequest request) throws StatusCodeException {
        org.heigit.ors.export.ExportRequest exportRequest = new org.heigit.ors.export.ExportRequest();

        if  (request.hasId())
            exportRequest.setId(request.getId());

        int profileType = -1;

        try {
            profileType = convertRouteProfileType(request.getProfile());
            exportRequest.setProfileType(profileType);
        } catch (Exception e) {
            throw new ParameterValueException(ExportErrorCodes.INVALID_PARAMETER_VALUE, ExportRequest.PARAM_PROFILE);
        }

        exportRequest.setBoundingBox(convertBBox(request.getBbox()));

        return exportRequest;
    }

    BBox convertBBox(List<List<Double>> coordinates) throws ParameterValueException {
        if (coordinates.size() != 2) {
            throw new ParameterValueException(ExportErrorCodes.INVALID_PARAMETER_VALUE, ExportRequest.PARAM_BBOX);
        }

        double[] coords = {};

        for (List<Double> coord : coordinates) {
            coords = Doubles.concat(coords, convertSingleCoordinate(coord));
        }

        return new BBox(coords);
    }

    private double[] convertSingleCoordinate(List<Double> coordinate) throws ParameterValueException {
        if (coordinate.size() != 2) {
            throw new ParameterValueException(ExportErrorCodes.INVALID_PARAMETER_VALUE, ExportRequest.PARAM_BBOX);
        }

        return new double[]{coordinate.get(0), coordinate.get(1)};
    }

}

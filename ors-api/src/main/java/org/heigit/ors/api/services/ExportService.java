package org.heigit.ors.api.services;

import com.google.common.primitives.Doubles;
import com.graphhopper.util.shapes.BBox;
import org.heigit.ors.api.config.ApiEngineProperties;
import org.heigit.ors.api.config.EndpointsProperties;
import org.heigit.ors.api.requests.export.ExportApiRequest;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.export.ExportErrorCodes;
import org.heigit.ors.export.ExportResult;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExportService extends ApiService {

    @Autowired
    public ExportService(EndpointsProperties endpointsProperties, ApiEngineProperties apiEngineProperties) {
        this.endpointsProperties = endpointsProperties;
        this.apiEngineProperties = apiEngineProperties;
    }

    public ExportResult generateExportFromRequest(ExportApiRequest exportApiRequest) throws StatusCodeException {
        org.heigit.ors.export.ExportRequest exportRequest = this.convertExportRequest(exportApiRequest);

        try {
            RoutingProfile rp = RoutingProfileManager.getInstance().getRoutingProfile(exportRequest.getProfileName());
            if (rp == null)
                throw new InternalServerException(ExportErrorCodes.UNKNOWN, "Unable to find an appropriate routing profile.");
            return exportRequest.computeExport(rp);
        } catch (StatusCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new StatusCodeException(StatusCode.INTERNAL_SERVER_ERROR, ExportErrorCodes.UNKNOWN);
        }
    }

    private org.heigit.ors.export.ExportRequest convertExportRequest(ExportApiRequest exportApiRequest) throws StatusCodeException {
        org.heigit.ors.export.ExportRequest exportRequest = new org.heigit.ors.export.ExportRequest();
        exportRequest.setProfileName(exportApiRequest.getProfileName());
        if (exportApiRequest.hasId())
            exportRequest.setId(exportApiRequest.getId());

        int profileType = -1;

        try {
            profileType = convertRouteProfileType(exportApiRequest.getProfile());
            exportRequest.setProfileType(profileType);
        } catch (Exception e) {
            throw new ParameterValueException(ExportErrorCodes.INVALID_PARAMETER_VALUE, ExportApiRequest.PARAM_PROFILE);
        }

        exportRequest.setBoundingBox(convertBBox(exportApiRequest.getBbox()));
        exportRequest.setDebug(exportApiRequest.debug());

        return exportRequest;
    }

    BBox convertBBox(List<List<Double>> coordinates) throws ParameterValueException {
        if (coordinates.size() != 2) {
            throw new ParameterValueException(ExportErrorCodes.INVALID_PARAMETER_VALUE, ExportApiRequest.PARAM_BBOX);
        }

        double[] coords = {};

        for (List<Double> coord : coordinates) {
            coords = Doubles.concat(coords, convertSingleCoordinate(coord));
        }

        return new BBox(coords);
    }

    private double[] convertSingleCoordinate(List<Double> coordinate) throws ParameterValueException {
        if (coordinate.size() != 2) {
            throw new ParameterValueException(ExportErrorCodes.INVALID_PARAMETER_VALUE, ExportApiRequest.PARAM_BBOX);
        }

        return new double[]{coordinate.get(0), coordinate.get(1)};
    }

}

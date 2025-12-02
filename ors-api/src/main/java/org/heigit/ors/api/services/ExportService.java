package org.heigit.ors.api.services;

import com.google.common.primitives.Doubles;
import com.graphhopper.util.shapes.BBox;
import org.heigit.ors.api.APIEnums;
import org.heigit.ors.api.config.ApiEngineProperties;
import org.heigit.ors.api.config.EndpointsProperties;
import org.heigit.ors.api.requests.export.ExportApiRequest;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.export.ExportErrorCodes;
import org.heigit.ors.export.ExportRequest;
import org.heigit.ors.export.ExportResult;
import org.heigit.ors.routing.RoutingProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExportService extends ApiService {

    @Autowired
    public ExportService(EngineService engineService, EndpointsProperties endpointsProperties, ApiEngineProperties apiEngineProperties) {
        super(engineService);
        this.endpointsProperties = endpointsProperties;
        this.apiEngineProperties = apiEngineProperties;
    }

    public ExportResult generateExportFromRequest(ExportApiRequest exportApiRequest) throws StatusCodeException {
        try {
            ExportRequest exportRequest = this.parseExportRequest(exportApiRequest);
            return exportRequest.computeExport();
        } catch (StatusCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new StatusCodeException(StatusCode.INTERNAL_SERVER_ERROR, ExportErrorCodes.UNKNOWN);
        }
    }

    private ExportRequest parseExportRequest(ExportApiRequest exportApiRequest) throws StatusCodeException {
        ExportRequest exportRequest = new ExportRequest();
        exportRequest.setProfile(parseRoutingProfile(exportApiRequest.getProfileName()));
        if (exportApiRequest.hasId())
            exportRequest.setId(exportApiRequest.getId());

        exportRequest.setProfileType(parseProfileType(exportApiRequest.getProfile()));
        exportRequest.setBoundingBox(convertBBox(exportApiRequest.getBbox()));
        exportRequest.setAdditionalEdgeInfo(exportApiRequest.additionalInfo());
        exportRequest.setTopoJson(exportApiRequest.getResponseType().equals(APIEnums.ExportResponseType.TOPOJSON));
        exportRequest.setUseRealGeometry(exportApiRequest.getGeometry());

        return exportRequest;
    }

    private RoutingProfile parseRoutingProfile(String profileName) throws InternalServerException {
        RoutingProfile rp = engineService.waitForActiveRoutingProfileManager().getRoutingProfile(profileName);
        if (rp == null)
            throw new InternalServerException(ExportErrorCodes.UNKNOWN, "Unable to find an appropriate routing profile.");
        return rp;
    }

    private static int parseProfileType(APIEnums.Profile profile) throws ParameterValueException {
        try {
            return convertRouteProfileType(profile);
        } catch (Exception e) {
            throw new ParameterValueException(ExportErrorCodes.INVALID_PARAMETER_VALUE, ExportApiRequest.PARAM_PROFILE);
        }
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

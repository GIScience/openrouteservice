/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */


package heigit.ors.api.controllers;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import heigit.ors.api.errors.IsochronesResponseEntityExceptionHandler;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.api.requests.isochrones.IsochronesRequest;
import heigit.ors.api.requests.isochrones.IsochronesRequestHandler;
import heigit.ors.api.responses.isochrones.GeoJSONIsochronesResponseObjects.GeoJSONIsochronesResponse;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.UnknownParameterException;
import heigit.ors.isochrones.IsochroneMapCollection;
import heigit.ors.isochrones.IsochronesErrorCodes;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(value = "/v2/isochrones", description = "Get an Isochrone Calculation")
@RequestMapping("/v2/isochrones")
public class IsochronesAPI {

    @PostMapping(value = "/{profile}/geojson", produces = "application/geo+json;charset=UTF-8")
    @ApiOperation(value = "Get isochrones from the specified profile", httpMethod = "POST", consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "GeoJSON Response", response = GeoJSONIsochronesResponse.class)
    })
    public GeoJSONIsochronesResponse getGeoJsonMime(
            @ApiParam(value = "Specifies the route profile.", required = true) @PathVariable APIEnums.Profile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody IsochronesRequest request) throws Exception {
        request.setProfile(profile);
        request.setResponseType(APIEnums.RouteResponseType.GEOJSON);

        IsochronesRequestHandler handler = new IsochronesRequestHandler();
        handler.generateIsochronesFromRequest(request);
        IsochroneMapCollection isoMaps = handler.getIsoMaps();
        return new GeoJSONIsochronesResponse(request, isoMaps);
    }

    // Errors generated from the reading of the request (before entering the routing system). Normally these are where
    // parameters have been entered incorrectly in the request
    @ExceptionHandler
    public ResponseEntity<Object> handleError(final HttpMessageNotReadableException e) {
        final Throwable cause = e.getCause();
        final IsochronesResponseEntityExceptionHandler h = new IsochronesResponseEntityExceptionHandler();
        if (cause instanceof UnrecognizedPropertyException) {
            return h.handleUnknownParameterException(new UnknownParameterException(IsochronesErrorCodes.UNKNOWN_PARAMETER, ((UnrecognizedPropertyException) cause).getPropertyName()));
        } else if (cause instanceof InvalidFormatException) {
            return h.handleStatusCodeException(new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, ((InvalidFormatException) cause).getValue().toString()));
        } else if (cause instanceof InvalidDefinitionException) {
            return h.handleStatusCodeException(new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, ((InvalidDefinitionException) cause).getPath().get(0).getFieldName()));
        } else if (cause instanceof MismatchedInputException) {
            return h.handleStatusCodeException(new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, ((MismatchedInputException) cause).getPath().get(0).getFieldName()));
        } else {
            return h.handleGenericException(e);
        }
    }
}

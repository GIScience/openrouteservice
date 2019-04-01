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
import heigit.ors.api.errors.CommonResponseEntityExceptionHandler;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.api.requests.isochrones.IsochronesRequest;
import heigit.ors.api.requests.isochrones.IsochronesRequestHandler;
import heigit.ors.api.responses.isochrones.GeoJSONIsochronesResponseObjects.GeoJSONIsochronesResponse;
import heigit.ors.exceptions.*;
import heigit.ors.isochrones.IsochroneMapCollection;
import heigit.ors.isochrones.IsochronesErrorCodes;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@Api(value = "Isochrones Service", description = "Obtain areas of reachability from given locations", tags = "Isochrones")
@RequestMapping("/v2/isochrones")
@ApiResponses({
        @ApiResponse(code = 400, message = "The request is incorrect and therefore can not be processed."),
        @ApiResponse(code = 404, message = "An element could not be found. If possible, a more detailed error code is provided."),
        @ApiResponse(code = 405, message = "The specified HTTP method is not supported. For more details, refer to the EndPoint documentation."),
        @ApiResponse(code = 413, message = "The request is larger than the server is able to process, the data provided in the request exceeds the capacity limit."),
        @ApiResponse(code = 500, message = "An unexpected error was encountered and a more detailed error code is provided."),
        @ApiResponse(code = 501, message = "Indicates that the server does not support the functionality needed to fulfill the request."),
        @ApiResponse(code = 503, message = "The server is currently unavailable due to overload or maintenance.")
})
public class IsochronesAPI {
    static final CommonResponseEntityExceptionHandler errorHandler = new CommonResponseEntityExceptionHandler(IsochronesErrorCodes.BASE);

    // generic catch methods - when extra info is provided in the url, the other methods are accessed.
    @GetMapping
    @ApiOperation(value = "", hidden = true)
    public void getGetMapping() throws MissingParameterException {
        throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, "profile");
    }

    @PostMapping
    @ApiOperation(value = "", hidden = true)
    public String getPostMapping(@RequestBody IsochronesRequest request) throws MissingParameterException {
        throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, "profile");
    }

    // Matches any response type that has not been defined
    @PostMapping(value="/{profile}/*")
    @ApiOperation(value = "", hidden = true)
    public void getInvalidResponseType() throws StatusCodeException {
        throw new StatusCodeException(HttpServletResponse.SC_NOT_ACCEPTABLE, IsochronesErrorCodes.UNSUPPORTED_EXPORT_FORMAT, "This response format is not supported");
    }

    // Functional request methods
    @PostMapping(value = "/{profile}", produces = "application/geo+json;charset=UTF-8")
    @ApiOperation(value = "Isochrones Service", notes = "The Isochrone Service supports time and distance analyses for one single or multiple locations.\n" +
            "You may also specify the isochrone interval or provide multiple exact isochrone range values.\n" +
            "This service allows the same range of profile options as the /directions endpoint,\n" +
            "which help you to further customize your request to obtain a more detailed reachability area response.", httpMethod = "POST", consumes = "application/geo+json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Standard response for successfully processed requests. Returns GeoJSON.", response = GeoJSONIsochronesResponse.class)
    })
    public GeoJSONIsochronesResponse getDefaultIsochrones(
            @ApiParam(value = "Specifies the route profile.", required = true, example = "driving-car") @PathVariable APIEnums.Profile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody IsochronesRequest request) throws Exception {
        return getGeoJsonIsochrones(profile, request);
    }

    @PostMapping(value = "/{profile}/geojson", produces = "application/geo+json;charset=UTF-8")
    @ApiOperation(value = "The Isochrone Service supports time and distance analyses for one single or multiple locations.\n" +
            "You may also specify the isochrone interval or provide multiple exact isochrone range values.\n" +
            "This service allows the same range of profile options as the /directions endpoint,\n" +
            "which help you to further customize your request to obtain a more detailed reachability area response.", httpMethod = "POST", consumes = "application/geo+json", hidden = true)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Standard response for successfully processed requests. Returns GeoJSON.", response = GeoJSONIsochronesResponse.class)
    })
    public GeoJSONIsochronesResponse getGeoJsonIsochrones(
            @ApiParam(value = "Specifies the route profile.", required = true, example = "driving-car") @PathVariable APIEnums.Profile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody IsochronesRequest request) throws Exception {
        request.setProfile(profile);
        request.setResponseType(APIEnums.RouteResponseType.GEOJSON);

        IsochronesRequestHandler handler = new IsochronesRequestHandler();
        handler.generateIsochronesFromRequest(request);
        IsochroneMapCollection isoMaps = handler.getIsoMaps();
        return new GeoJSONIsochronesResponse(request, isoMaps);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingParams(final MissingServletRequestParameterException e) {
        return errorHandler.handleStatusCodeException(new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, e.getParameterName()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMessageConversionException.class})
    public ResponseEntity<Object> handleReadingBodyException(final Exception e) {
        final Throwable cause = e.getCause();
        if (cause instanceof UnrecognizedPropertyException) {
            return errorHandler.handleUnknownParameterException(new UnknownParameterException(IsochronesErrorCodes.UNKNOWN_PARAMETER, ((UnrecognizedPropertyException) cause).getPropertyName()));
        } else if (cause instanceof InvalidFormatException) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, ((InvalidFormatException) cause).getValue().toString()));
        } else if (cause instanceof InvalidDefinitionException) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, ((InvalidDefinitionException) cause).getPath().get(0).getFieldName()));
        } else if (cause instanceof MismatchedInputException) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, ((MismatchedInputException) cause).getPath().get(0).getFieldName()));
        } else if (cause instanceof InvalidDefinitionException) {
            return errorHandler.handleStatusCodeException((new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, ((InvalidDefinitionException) cause).getPath().get(0).getFieldName())));
        } else {
            // Check if we are missing the body as a whole
            if (e.getLocalizedMessage().startsWith("Required request body is missing")) {
                return errorHandler.handleStatusCodeException(new EmptyElementException(IsochronesErrorCodes.MISSING_PARAMETER, "Request body could not be read"));
            }
            return errorHandler.handleGenericException(e);
        }
    }

    @ExceptionHandler(StatusCodeException.class)
    public ResponseEntity<Object> handleException(final StatusCodeException e) {
        return errorHandler.handleStatusCodeException(e);
    }
}

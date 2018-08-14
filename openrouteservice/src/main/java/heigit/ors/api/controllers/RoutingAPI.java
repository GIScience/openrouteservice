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
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.errors.CommonResponseEntityExceptionHandler;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.requests.routing.RouteRequestHandler;
import heigit.ors.api.responses.routing.GPXRouteResponseObjects.GPXRouteResponse;
import heigit.ors.api.responses.routing.GeoJSONRouteResponseObjects.GeoJSONRouteResponse;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONRouteResponse;
import heigit.ors.exceptions.*;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RoutingErrorCodes;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@Api(value = "Directions Service", description = "et directions for different modes of transport")
@RequestMapping("/v2/directions")
public class RoutingAPI {
    final static CommonResponseEntityExceptionHandler errorHandler = new CommonResponseEntityExceptionHandler(RoutingErrorCodes.BASE);

    // generic catch methods - when extra info is provided in the url, the other methods are accessed.
    @GetMapping
    @ApiOperation(value = "", hidden = true)
    public void getGetMapping() throws MissingParameterException {
        throw new MissingParameterException(RoutingErrorCodes.MISSING_PARAMETER, "profile");
    }

    @PostMapping
    @ApiOperation(value = "", hidden = true)
    public String getPostMapping(@RequestBody RouteRequest request) throws MissingParameterException {
        throw new MissingParameterException(RoutingErrorCodes.MISSING_PARAMETER, "profile");
    }

    // Matches any response type that has not been defined
    @PostMapping(value="/{profile}/*")
    @ApiOperation(value = "", hidden = true)
    public void getInvalidResponseType() throws StatusCodeException {
        throw new StatusCodeException(HttpServletResponse.SC_NOT_ACCEPTABLE, RoutingErrorCodes.UNSUPPORTED_EXPORT_FORMAT, "This response format is not supported");
    }

    // Functional request methods

    @GetMapping(value = "/{profile}", produces = {"application/geo+json;charset=UTF-8"})
    @ApiOperation(value = "Get a basic route between two points with the profile provided. Returned response is in GeoJSON format", httpMethod = "GET")
    @ApiResponses(
            @ApiResponse(code = 200, message = "GeoJSON Response", response = GeoJSONRouteResponse.class)
    )
    public GeoJSONRouteResponse getSimpleGeoJsonRoute(@ApiParam(value = "Specifies the route profile.") @PathVariable APIEnums.Profile profile,
                                                      @ApiParam(value = "Start coordinate of the route", required = true) @RequestParam Coordinate start,
                                                      @ApiParam(value = "Destination coordinate of the route", required = true) @RequestParam Coordinate end) throws StatusCodeException{
        RouteRequest request = new RouteRequest(start, end);
        request.setProfile(profile);

        RouteResult result = new RouteRequestHandler().generateRouteFromRequest(request);

        return new GeoJSONRouteResponse(new RouteResult[] { result }, request);
    }

    @PostMapping(value = "/{profile}")
    @ApiOperation(value = "Returns a route between two or more locations for a selected profile and its settings as JSON", httpMethod = "POST", consumes = "application/json", produces = "application/json")
    @ApiResponses(
            @ApiResponse(code = 200, message = "JSON Response", response = JSONRouteResponse.class)
    )
    public JSONRouteResponse getDefault(@ApiParam(value = "Specifies the route profile.") @PathVariable APIEnums.Profile profile,
                                        @ApiParam(value = "The request payload", required = true) @RequestBody RouteRequest request) throws Exception {
        return getJsonRoute(profile, request);
    }

    @PostMapping(value = "/{profile}/json", produces = {"application/json;charset=UTF-8"})
    @ApiOperation(value = "Returns a route between two or more locations for a selected profile and its settings as JSON", httpMethod = "POST", consumes = "application/json", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "JSON Response", response = JSONRouteResponse.class)
    })
    public JSONRouteResponse getJsonRoute(
            @ApiParam(value = "Specifies the route profile.", required = true) @PathVariable APIEnums.Profile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody RouteRequest request) throws StatusCodeException {
        request.setProfile(profile);
        request.setResponseType(APIEnums.RouteResponseType.JSON);

        RouteResult result = new RouteRequestHandler().generateRouteFromRequest(request);

        return new JSONRouteResponse(new RouteResult[]{result}, request);
    }

    @PostMapping(value = "/{profile}/gpx", produces = "application/gpx+xml;charset=UTF-8")
    @ApiOperation(value = "Returns a route between two or more locations for a selected profile and its settings as GPX", httpMethod = "POST", consumes = "application/json", produces = "application/gpx+xml")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "GPX Response", response = GPXRouteResponse.class)
    })
    public GPXRouteResponse getGPXRoute(
            @ApiParam(value = "Specifies the route profile.", required = true) @PathVariable APIEnums.Profile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody RouteRequest request) throws Exception {
        request.setProfile(profile);
        request.setResponseType(APIEnums.RouteResponseType.GPX);

        RouteResult result = new RouteRequestHandler().generateRouteFromRequest(request);

        return new GPXRouteResponse(new RouteResult[]{result}, request);

    }

    @PostMapping(value = "/{profile}/geojson", produces = "application/geo+json;charset=UTF-8")
    @ApiOperation(value = "Returns a route between two or more locations for a selected profile and its settings as GeoJSON", httpMethod = "POST", consumes = "application/json", produces = "application/geo+json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "GeoJSON Response", response = GeoJSONRouteResponse.class)
    })
    public GeoJSONRouteResponse getGeoJsonRoute(
            @ApiParam(value = "Specifies the route profile.", required = true) @PathVariable APIEnums.Profile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody RouteRequest request) throws Exception {
        request.setProfile(profile);
        request.setResponseType(APIEnums.RouteResponseType.GEOJSON);

        RouteResult result = new RouteRequestHandler().generateRouteFromRequest(request);

        return new GeoJSONRouteResponse(new RouteResult[]{result}, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingParams(final MissingServletRequestParameterException e) {
        return errorHandler.handleStatusCodeException(new MissingParameterException(RoutingErrorCodes.MISSING_PARAMETER, e.getParameterName()));
    }


    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMessageConversionException.class, Exception.class})
    public ResponseEntity<Object> handleReadingBodyException(final Exception e) {
        final Throwable cause = e.getCause();
        if (cause instanceof UnrecognizedPropertyException) {
            return errorHandler.handleUnknownParameterException(new UnknownParameterException(RoutingErrorCodes.UNKNOWN_PARAMETER, ((UnrecognizedPropertyException) cause).getPropertyName()));
        } else if (cause instanceof InvalidFormatException) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, ((InvalidFormatException) cause).getValue().toString()));
        } else if (cause instanceof InvalidDefinitionException) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, ((InvalidDefinitionException) cause).getPath().get(0).getFieldName()));
        } else if (cause instanceof MismatchedInputException) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, ((MismatchedInputException) cause).getPath().get(0).getFieldName()));
        } else {
            // Check if we are missing the body as a whole
            if (e.getLocalizedMessage().startsWith("Required request body is missing")) {
                return errorHandler.handleStatusCodeException(new EmptyElementException(RoutingErrorCodes.MISSING_PARAMETER, "Request body could not be read"));
            }
            return errorHandler.handleGenericException(e);
        }
    }

    @ExceptionHandler(StatusCodeException.class)
    public ResponseEntity<Object> handleException(final StatusCodeException e) {
        return errorHandler.handleStatusCodeException(e);
    }
}

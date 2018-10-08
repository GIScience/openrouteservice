package heigit.ors.api.controllers;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import heigit.ors.api.errors.RestResponseEntityExceptionHandler;
import heigit.ors.api.requests.routing.APIRoutingEnums;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.GPXRouteResponseObjects.GPXRouteResponse;
import heigit.ors.api.responses.routing.GeoJSONRouteResponseObjects.GeoJSONRouteResponse;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONRouteResponse;
import heigit.ors.exceptions.*;
import heigit.ors.routing.RouteRequestHandler;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RoutingErrorCodes;
import io.swagger.annotations.*;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(value="/v2/routes", description = "Get a route")
@RequestMapping("/v2/routes")
public class RoutingAPI {

    @PostMapping
    public String getPostMapping(@RequestBody RouteRequest request) throws MissingParameterException {
        throw new MissingParameterException(RoutingErrorCodes.MISSING_PARAMETER, "profile");
    }


    @PostMapping(value = "/{profile}", produces = "application/gpx+xml;charset=UTF-8")
    @ApiOperation(value = "Get a route from the specified profile", httpMethod = "POST", consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "GPX Response", response = GPXRouteResponse.class)
    })
    public GPXRouteResponse getGPXMime(
            @ApiParam(value = "Specifies the route profile.") @PathVariable APIRoutingEnums.RoutingProfile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody RouteRequest request) throws Exception {
        request.setProfile(profile);
        request.setResponseType(APIRoutingEnums.RouteResponseType.GPX);

        RouteResult result = RouteRequestHandler.generateRouteFromRequest(request);

        return new GPXRouteResponse(new RouteResult[] { result }, request);

    }

    @PostMapping(value = "/{profile}", produces = "application/geo+json;charset=UTF-8")
    @ApiOperation(value = "Get a route from the specified profile", httpMethod = "POST", consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "GeoJSON Response", response = GeoJSONRouteResponse.class)
    })
    public GeoJSONRouteResponse getGeoJsonMime(
            @ApiParam(value = "Specifies the route profile.") @PathVariable APIRoutingEnums.RoutingProfile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody RouteRequest request) throws Exception {
        request.setProfile(profile);
        request.setResponseType(APIRoutingEnums.RouteResponseType.GEOJSON);

        RouteResult result = RouteRequestHandler.generateRouteFromRequest(request);

        return new GeoJSONRouteResponse(new RouteResult[] { result }, request);
    }

    @PostMapping(value = "/{profile}", produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "Get a route from the specified profile", httpMethod = "POST", consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "JSON Response", response = JSONRouteResponse.class)
    })
    public JSONRouteResponse getJsonMime(
            @ApiParam(value = "Specifies the route profile.") @PathVariable APIRoutingEnums.RoutingProfile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody RouteRequest request) throws StatusCodeException {
        request.setProfile(profile);
        request.setResponseType(APIRoutingEnums.RouteResponseType.JSON);

        RouteResult result = RouteRequestHandler.generateRouteFromRequest(request);

        return new JSONRouteResponse(new RouteResult[] { result }, request);
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleError(final HttpMessageNotReadableException e) {
        final Throwable cause = e.getCause();
        final RestResponseEntityExceptionHandler h = new RestResponseEntityExceptionHandler();
        if(cause instanceof UnrecognizedPropertyException) {
            return h.handleUnknownParameterException(new UnknownParameterException(RoutingErrorCodes.UNKNOWN_PARAMETER, ((UnrecognizedPropertyException)cause).getPropertyName()));
        } else if(cause instanceof InvalidFormatException) {
            return h.handleStatusCodeException(new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, ((InvalidFormatException)cause).getValue().toString()));
        } else if(cause instanceof InvalidDefinitionException) {
            return h.handleStatusCodeException(new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, ""));
        } else if(cause instanceof MismatchedInputException) {
            // TODO: Need to get the attribute that has a problem
            return h.handleStatusCodeException(new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, ""));
        } else {
            return h.handleGenericException(e);
        }
    }
}

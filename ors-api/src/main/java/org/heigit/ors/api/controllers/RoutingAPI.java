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

package org.heigit.ors.api.controllers;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.heigit.ors.api.EndpointsProperties;
import org.heigit.ors.api.SystemMessageProperties;
import org.heigit.ors.api.errors.CommonResponseEntityExceptionHandler;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.api.responses.routing.geojson.GeoJSONRouteResponse;
import org.heigit.ors.api.responses.routing.gpx.GPXRouteResponse;
import org.heigit.ors.api.responses.routing.json.JSONRouteResponse;
import org.heigit.ors.api.services.RoutingService;
import org.heigit.ors.api.util.AppConfigMigration;
import org.heigit.ors.exceptions.*;
import org.heigit.ors.api.APIEnums;
import org.heigit.ors.routing.RouteResult;
import org.heigit.ors.routing.RoutingErrorCodes;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Directions Service", description = "Get directions for different modes of transport")
@RequestMapping("/v2/directions")
@ApiResponse(responseCode = "400", description = "The request is incorrect and therefore can not be processed.")
@ApiResponse(responseCode = "404", description = "An element could not be found. If possible, a more detailed error code is provided.")
@ApiResponse(responseCode = "405", description = "The specified HTTP method is not supported. For more details, refer to the EndPoint documentation.")
@ApiResponse(responseCode = "413", description = "The request is larger than the server is able to process, the data provided in the request exceeds the capacity limit.")
@ApiResponse(responseCode = "500", description = "An unexpected error was encountered and a more detailed error code is provided.")
@ApiResponse(responseCode = "501", description = "Indicates that the server does not support the functionality needed to fulfill the request.")
@ApiResponse(responseCode = "503", description = "The server is currently unavailable due to overload or maintenance.")
public class RoutingAPI {
    static final CommonResponseEntityExceptionHandler errorHandler = new CommonResponseEntityExceptionHandler(RoutingErrorCodes.BASE);

    private final EndpointsProperties endpointsProperties;
    private final SystemMessageProperties systemMessageProperties;
    private final RoutingService routingService;

    public RoutingAPI(EndpointsProperties endpointsProperties, SystemMessageProperties systemMessageProperties, RoutingService routingService) {
        this.endpointsProperties = AppConfigMigration.overrideEndpointsProperties(endpointsProperties);
        this.systemMessageProperties = systemMessageProperties;
        this.routingService = routingService;
    }

    // generic catch methods - when extra info is provided in the url, the other methods are accessed.
    @GetMapping
    @Operation(hidden = true)
    public void getGetMapping() throws MissingParameterException {
        throw new MissingParameterException(RoutingErrorCodes.MISSING_PARAMETER, "profile");
    }

    @PostMapping
    @Operation(hidden = true)
    public String getPostMapping(@RequestBody RouteRequest request) throws MissingParameterException {
        throw new MissingParameterException(RoutingErrorCodes.MISSING_PARAMETER, "profile");
    }

    // Matches any response type that has not been defined
    @PostMapping(value = "/{profile}/*")
    @Operation(hidden = true)
    public void getInvalidResponseType() throws StatusCodeException {
        throw new StatusCodeException(HttpServletResponse.SC_NOT_ACCEPTABLE, RoutingErrorCodes.UNSUPPORTED_EXPORT_FORMAT, "This response format is not supported");
    }

    // Functional request methods

    @GetMapping(value = "/{profile}", produces = {"application/geo+json;charset=UTF-8"})
    @Operation(
            method = "GET",
            description = """
                    Get a basic route between two points with the profile provided. Returned response is in GeoJSON format. \
                    This method does not accept any request body or parameters other than profile, start coordinate, and end coordinate.\
                    """, summary = "Directions Service")
    @ApiResponse(
            responseCode = "200",
            description = "Standard response for successfully processed requests. Returns GeoJSON. The decoded values of the extra information can be found [here](https://giscience.github.io/openrouteservice/api-reference/endpoints/directions/extra-info/).",
            content = {@Content(
                    mediaType = "application/geo+json",
                    schema = @Schema(implementation = GeoJSONRouteResponse.class)
            )
            })
    public GeoJSONRouteResponse getSimpleGeoJsonRoute(@Parameter(description = "Specifies the route profile.", required = true, example = "driving-car") @PathVariable APIEnums.Profile profile,
                                                      @Parameter(description = "Start coordinate of the route in `longitude,latitude` format.", required = true, example = "8.681495,49.41461") @RequestParam Coordinate start,
                                                      @Parameter(description = "Destination coordinate of the route in `longitude,latitude` format.", required = true, example = "8.687872,49.420318") @RequestParam Coordinate end) throws StatusCodeException {
        RouteRequest request = new RouteRequest(start, end);
        request.setProfile(profile);

        RouteResult[] result = routingService.generateRouteFromRequest(request);

        return new GeoJSONRouteResponse(result, request, systemMessageProperties, endpointsProperties);
    }

    @PostMapping(value = "/{profile}")
    @Operation(
            description = "Returns a route between two or more locations for a selected profile and its settings as JSON",
            summary = "Directions Service"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Standard response for successfully processed requests. Returns JSON. The decoded values of the extra information can be found [here](https://giscience.github.io/openrouteservice/api-reference/endpoints/directions/extra-info/).",
            content = {@Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = JSONRouteResponse.class)
            )
            })
    public JSONRouteResponse getDefault(@Parameter(description = "Specifies the route profile.", required = true, example = "driving-car") @PathVariable APIEnums.Profile profile,
                                        @Parameter(description = "The request payload", required = true) @RequestBody RouteRequest request) throws StatusCodeException {
        return getJsonRoute(profile, request);
    }

    @PostMapping(value = "/{profile}/json", produces = {"application/json;charset=UTF-8"})
    @Operation(
            description = "Returns a route between two or more locations for a selected profile and its settings as JSON",
            summary = "Directions Service JSON"
    )
    @ApiResponse(responseCode = "200",
            description = "JSON Response",
            content = {@Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = JSONRouteResponse.class)
            )
            })
    public JSONRouteResponse getJsonRoute(
            @Parameter(description = "Specifies the route profile.", required = true, example = "driving-car") @PathVariable APIEnums.Profile profile,
            @Parameter(description = "The request payload", required = true) @RequestBody RouteRequest request) throws StatusCodeException {
        request.setProfile(profile);
        request.setResponseType(APIEnums.RouteResponseType.JSON);

        RouteResult[] result = routingService.generateRouteFromRequest(request);

        return new JSONRouteResponse(result, request, systemMessageProperties, endpointsProperties);
    }

    @PostMapping(value = "/{profile}/gpx", produces = "application/gpx+xml;charset=UTF-8")
    @Operation(
            description = "Returns a route between two or more locations for a selected profile and its settings as GPX. The schema can be found [here](https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v1/ors-gpx.xsd)",
            summary = "Directions Service GPX"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Standard response for successfully processed requests. Returns GPX.",
            content = {@Content(
                    mediaType = "application/gpx+xml",
                    schema = @Schema(implementation = GPXRouteResponse.class)
            )
            })
    public GPXRouteResponse getGPXRoute(
            @Parameter(description = "Specifies the route profile.", required = true, example = "driving-car") @PathVariable APIEnums.Profile profile,
            @Parameter(description = "The request payload", required = true) @RequestBody RouteRequest request) throws StatusCodeException {
        request.setProfile(profile);
        request.setResponseType(APIEnums.RouteResponseType.GPX);

        RouteResult[] result = routingService.generateRouteFromRequest(request);

        return new GPXRouteResponse(result, request, systemMessageProperties, endpointsProperties);

    }

    @PostMapping(value = "/{profile}/geojson", produces = "application/geo+json;charset=UTF-8")
    @Operation(
            description = "Returns a route between two or more locations for a selected profile and its settings as GeoJSON",
            summary = "Directions Service GeoJSON"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Standard response for successfully processed requests. Returns GeoJSON. The decoded values of the extra information can be found [here](https://giscience.github.io/openrouteservice/api-reference/endpoints/directions/extra-info/).",
            content = {@Content(
                    mediaType = "application/geo+json",
                    schema = @Schema(implementation = GeoJSONRouteResponse.class)
            )
            })
    public GeoJSONRouteResponse getGeoJsonRoute(
            @Parameter(description = "Specifies the route profile.", required = true, example = "driving-car") @PathVariable APIEnums.Profile profile,
            @Parameter(description = "The request payload", required = true) @RequestBody RouteRequest request) throws StatusCodeException {
        request.setProfile(profile);
        request.setResponseType(APIEnums.RouteResponseType.GEOJSON);

        RouteResult[] result = routingService.generateRouteFromRequest(request);

        return new GeoJSONRouteResponse(result, request, systemMessageProperties, endpointsProperties);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingParams(final MissingServletRequestParameterException e) {
        return errorHandler.handleStatusCodeException(new MissingParameterException(RoutingErrorCodes.MISSING_PARAMETER, e.getParameterName()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, ConversionFailedException.class, HttpMessageConversionException.class, Exception.class})
    public ResponseEntity<Object> handleReadingBodyException(final Exception e) {
        final Throwable cause = e.getCause();
        if (cause instanceof UnrecognizedPropertyException exception) {
            return errorHandler.handleUnknownParameterException(new UnknownParameterException(RoutingErrorCodes.UNKNOWN_PARAMETER, exception.getPropertyName()));
        } else if (cause instanceof InvalidFormatException exception) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, "" + exception.getValue()));
        } else if (cause instanceof ConversionFailedException exception) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "" + exception.getValue()));
        } else if (cause instanceof InvalidDefinitionException exception) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, exception.getPath().get(0).getFieldName()));
        } else if (cause instanceof MismatchedInputException exception) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, exception.getPath().get(0).getFieldName()));
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

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
import org.heigit.ors.api.requests.snapping.SnappingApiRequest;
import org.heigit.ors.api.responses.snapping.geojson.GeoJSONSnappingResponse;
import org.heigit.ors.api.responses.snapping.json.JsonSnappingResponse;
import org.heigit.ors.api.services.SnappingService;
import org.heigit.ors.api.util.AppConfigMigration;
import org.heigit.ors.exceptions.*;
import org.heigit.ors.api.APIEnums;
import org.heigit.ors.snapping.SnappingErrorCodes;
import org.heigit.ors.snapping.SnappingResult;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Snapping Service", description = "Snap coordinates to the graph edges.")
@RequestMapping("/v2/snap")
@ApiResponse(responseCode = "400", description = "The request is incorrect and therefore can not be processed.")
@ApiResponse(responseCode = "404", description = "An element could not be found. If possible, a more detailed error code is provided.")
@ApiResponse(responseCode = "405", description = "The specified HTTP method is not supported. For more details, refer to the EndPoint documentation.")
@ApiResponse(responseCode = "413", description = "The request is larger than the server is able to process, the data provided in the request exceeds the capacity limit.")
@ApiResponse(responseCode = "500", description = "An unexpected error was encountered and a more detailed error code is provided.")
@ApiResponse(responseCode = "501", description = "Indicates that the server does not support the functionality needed to fulfill the request.")
@ApiResponse(responseCode = "503", description = "The server is currently unavailable due to overload or maintenance.")
public class SnappingAPI {
    static final CommonResponseEntityExceptionHandler errorHandler = new CommonResponseEntityExceptionHandler(SnappingErrorCodes.BASE);

    private final EndpointsProperties endpointsProperties;
    private final SystemMessageProperties systemMessageProperties;
    private final SnappingService snappingService;

    public SnappingAPI(EndpointsProperties endpointsProperties, SystemMessageProperties systemMessageProperties, SnappingService snappingService) {
        this.endpointsProperties = AppConfigMigration.overrideEndpointsProperties(endpointsProperties);
        this.systemMessageProperties = systemMessageProperties;
        this.snappingService = snappingService;
    }

    // generic catch methods - when extra info is provided in the url, the other methods are accessed.
    @GetMapping
    @Operation(hidden = true)
    public void getGetMapping() throws MissingParameterException {
        throw new MissingParameterException(SnappingErrorCodes.MISSING_PARAMETER, "profile");
    }

    @PostMapping
    @Operation(hidden = true)
    public String getPostMapping(@RequestBody SnappingApiRequest request) throws MissingParameterException {
        throw new MissingParameterException(SnappingErrorCodes.MISSING_PARAMETER, "profile");
    }

    // Matches any response type that has not been defined
    @PostMapping(value = "/{profile}/*")
    @Operation(hidden = true)
    public void getInvalidResponseType() throws StatusCodeException {
        throw new StatusCodeException(HttpServletResponse.SC_NOT_ACCEPTABLE, SnappingErrorCodes.UNSUPPORTED_EXPORT_FORMAT, "This response format is not supported");
    }

    // Functional request methods
    @PostMapping(value = "/{profile}")
    @Operation(
            description = """
                    Returns a list of points snapped to the nearest edge in the graph. In case an appropriate
                    snapping point cannot be found within the specified search radius, "null" is returned.
                    """,
            summary = "Snapping Service"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Standard response for successfully processed requests. Returns JSON.",
            content = {@Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = JsonSnappingResponse.class)
            )
            })
    public JsonSnappingResponse getDefault(@Parameter(description = "Specifies the route profile.", required = true, example = "driving-car") @PathVariable APIEnums.Profile profile,
                                         @Parameter(description = "The request payload", required = true) @RequestBody SnappingApiRequest request) throws StatusCodeException {
        return getJsonSnapping(profile, request);
    }

    @PostMapping(value = "/{profile}/json", produces = {"application/json;charset=UTF-8"})
    @Operation(
            description = """
                    Returns a list of points snapped to the nearest edge in the graph. In case an appropriate
                    snapping point cannot be found within the specified search radius, "null" is returned.
                    """,
            summary = "Snapping Service JSON"
    )
    @ApiResponse(
            responseCode = "200",
            description = "JSON Response.",
            content = {@Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = JsonSnappingResponse.class)
            )
            })
    public JsonSnappingResponse getJsonSnapping(
            @Parameter(description = "Specifies the profile.", required = true, example = "driving-car") @PathVariable APIEnums.Profile profile,
            @Parameter(description = "The request payload", required = true) @RequestBody SnappingApiRequest request) throws StatusCodeException {
        request.setProfile(profile);
        request.setResponseType(APIEnums.SnappingResponseType.JSON);

        SnappingResult result = snappingService.generateSnappingFromRequest(request);

        return new JsonSnappingResponse(result, request, systemMessageProperties, endpointsProperties);
    }

@PostMapping(value = "/{profile}/geojson", produces = {"application/json;charset=UTF-8"})
    @Operation(
            description = """
                    Returns a GeoJSON FeatureCollection of points snapped to the nearest edge in the graph.
                    In case an appropriate snapping point cannot be found within the specified search radius,
                    it is omitted from the features array. The features provide the 'source_id' property, to match
                    the results with the input location array (IDs start at 0).
                    """,
            summary = "Snapping Service GeoJSON"
    )
    @ApiResponse(
            responseCode = "200",
            description = "GeoJSON Response",
            content = {@Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = GeoJSONSnappingResponse.class)
            )
            })
    public GeoJSONSnappingResponse getGeoJSONSnapping(
            @Parameter(description = "Specifies the profile.", required = true, example = "driving-car") @PathVariable APIEnums.Profile profile,
            @Parameter(description = "The request payload", required = true) @RequestBody SnappingApiRequest request) throws StatusCodeException {
        request.setProfile(profile);
        request.setResponseType(APIEnums.SnappingResponseType.GEOJSON);

        SnappingResult result = snappingService.generateSnappingFromRequest(request);

        return new GeoJSONSnappingResponse(result, request, systemMessageProperties, endpointsProperties);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingParams(final MissingServletRequestParameterException e) {
        return errorHandler.handleStatusCodeException(new MissingParameterException(SnappingErrorCodes.MISSING_PARAMETER, e.getParameterName()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMessageConversionException.class, Exception.class})
    public ResponseEntity<Object> handleReadingBodyException(final Exception e) {
        final Throwable cause = e.getCause();
        if (cause instanceof UnrecognizedPropertyException exception) {
            return errorHandler.handleUnknownParameterException(new UnknownParameterException(SnappingErrorCodes.UNKNOWN_PARAMETER, exception.getPropertyName()));
        } else if (cause instanceof InvalidFormatException exception) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(SnappingErrorCodes.INVALID_PARAMETER_FORMAT, exception.getValue().toString()));
        } else if (cause instanceof InvalidDefinitionException exception) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(SnappingErrorCodes.INVALID_PARAMETER_VALUE, exception.getPath().get(0).getFieldName()));
        } else if (cause instanceof MismatchedInputException exception) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(SnappingErrorCodes.INVALID_PARAMETER_FORMAT, exception.getPath().get(0).getFieldName()));
        } else if (cause instanceof ConversionFailedException exception) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(SnappingErrorCodes.INVALID_PARAMETER_VALUE, (String) exception.getValue()));
        } else {
            // Check if we are missing the body as a whole
            if (e.getLocalizedMessage().startsWith("Required request body is missing")) {
                return errorHandler.handleStatusCodeException(new EmptyElementException(SnappingErrorCodes.MISSING_PARAMETER, "Request body could not be read"));
            }
            return errorHandler.handleGenericException(e);
        }
    }

    @ExceptionHandler(StatusCodeException.class)
    public ResponseEntity<Object> handleException(final StatusCodeException e) {
        return errorHandler.handleStatusCodeException(e);
    }
}

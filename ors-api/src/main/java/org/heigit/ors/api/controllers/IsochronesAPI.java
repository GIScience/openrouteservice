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
import org.heigit.ors.api.requests.isochrones.IsochronesRequest;
import org.heigit.ors.api.responses.isochrones.geojson.GeoJSONIsochronesResponse;
import org.heigit.ors.api.services.IsochronesService;
import org.heigit.ors.api.util.AppConfigMigration;
import org.heigit.ors.exceptions.*;
import org.heigit.ors.isochrones.IsochroneMapCollection;
import org.heigit.ors.isochrones.IsochronesErrorCodes;
import org.heigit.ors.api.APIEnums;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Isochrones Service", description = "Obtain areas of reachability from given locations")
@RequestMapping("/v2/isochrones")
@ApiResponse(responseCode = "400", description = "The request is incorrect and therefore can not be processed.")
@ApiResponse(responseCode = "404", description = "An element could not be found. If possible, a more detailed error code is provided.")
@ApiResponse(responseCode = "405", description = "The specified HTTP method is not supported. For more details, refer to the EndPoint documentation.")
@ApiResponse(responseCode = "413", description = "The request is larger than the server is able to process, the data provided in the request exceeds the capacity limit.")
@ApiResponse(responseCode = "500", description = "An unexpected error was encountered and a more detailed error code is provided.")
@ApiResponse(responseCode = "501", description = "Indicates that the server does not support the functionality needed to fulfill the request.")
@ApiResponse(responseCode = "503", description = "The server is currently unavailable due to overload or maintenance.")
public class IsochronesAPI {
    static final CommonResponseEntityExceptionHandler errorHandler = new CommonResponseEntityExceptionHandler(IsochronesErrorCodes.BASE);
    private final EndpointsProperties endpointsProperties;
    private final SystemMessageProperties systemMessageProperties;
    private final IsochronesService isochronesService;

    public IsochronesAPI(EndpointsProperties endpointsProperties, SystemMessageProperties systemMessageProperties, IsochronesService isochronesService) {
        this.endpointsProperties = AppConfigMigration.overrideEndpointsProperties(endpointsProperties);
        this.systemMessageProperties = systemMessageProperties;
        this.isochronesService = isochronesService;
    }

    // generic catch methods - when extra info is provided in the url, the other methods are accessed.
    @GetMapping
    @Operation(hidden = true)
    public void getGetMapping() throws MissingParameterException {
        throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, "profile");
    }

    @PostMapping
    @Operation(hidden = true)
    public String getPostMapping(@RequestBody IsochronesRequest request) throws MissingParameterException {
        throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, "profile");
    }

    // Matches any response type that has not been defined
    @PostMapping(value = "/{profile}/*")
    @Operation(hidden = true)
    public void getInvalidResponseType() throws StatusCodeException {
        throw new StatusCodeException(HttpServletResponse.SC_NOT_ACCEPTABLE, IsochronesErrorCodes.UNSUPPORTED_EXPORT_FORMAT, "This response format is not supported");
    }

    // Functional request methods
    @PostMapping(value = "/{profile}", produces = "application/geo+json;charset=UTF-8")
    @Operation(
            description = """
                    The Isochrone Service supports time and distance analyses for one single or multiple locations.
                    You may also specify the isochrone interval or provide multiple exact isochrone range values.
                    This service allows the same range of profile options as the /directions endpoint,
                    which help you to further customize your request to obtain a more detailed reachability area response.""",
            summary = "Isochrones Service"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Standard response for successfully processed requests. Returns GeoJSON.",
            content = {@Content(
                    mediaType = "application/geo+json",
                    schema = @Schema(implementation = GeoJSONIsochronesResponse.class)
            )
            })
    public GeoJSONIsochronesResponse getDefaultIsochrones(
            @Parameter(description = "Specifies the route profile.", required = true, example = "driving-car") @PathVariable APIEnums.Profile profile,
            @Parameter(description = "The request payload", required = true) @RequestBody IsochronesRequest request) throws Exception {
        return getGeoJsonIsochrones(profile, request);
    }

    @PostMapping(value = "/{profile}/geojson", produces = "application/geo+json;charset=UTF-8")
    @Operation(
            description = """
                    The Isochrone Service supports time and distance analyses for one single or multiple locations.
                    You may also specify the isochrone interval or provide multiple exact isochrone range values.
                    This service allows the same range of profile options as the /directions endpoint,
                    which help you to further customize your request to obtain a more detailed reachability area response.""",
            summary = "Isochrones Service",
            hidden = true
    )

    @ApiResponse(
            responseCode = "200",
            description = "Standard response for successfully processed requests. Returns GeoJSON.",
            content = {@Content(
                    mediaType = "application/geo+json",
                    schema = @Schema(implementation = GeoJSONIsochronesResponse.class)
            )
            })
    public GeoJSONIsochronesResponse getGeoJsonIsochrones(
            @Parameter(description = "Specifies the route profile.", required = true, example = "driving-car") @PathVariable APIEnums.Profile profile,
            @Parameter(description = "The request payload", required = true) @RequestBody IsochronesRequest request) throws Exception {
        request.setProfile(profile);
        request.setResponseType(APIEnums.RouteResponseType.GEOJSON);

        isochronesService.generateIsochronesFromRequest(request);
        IsochroneMapCollection isoMaps = request.getIsoMaps();
        return new GeoJSONIsochronesResponse(request, isoMaps, systemMessageProperties, endpointsProperties);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingParams(final MissingServletRequestParameterException e) {
        return errorHandler.handleStatusCodeException(new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, e.getParameterName()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, ConversionFailedException.class, HttpMessageConversionException.class, Exception.class})
    public ResponseEntity<Object> handleReadingBodyException(final Exception e) {
        final Throwable cause = e.getCause();
        if (cause instanceof UnrecognizedPropertyException exception) {
            return errorHandler.handleUnknownParameterException(new UnknownParameterException(IsochronesErrorCodes.UNKNOWN_PARAMETER, exception.getPropertyName()));
        } else if (cause instanceof InvalidFormatException exception) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, "" + exception.getValue()));
        } else if (cause instanceof ConversionFailedException exception) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "" + exception.getValue()));
        } else if (cause instanceof InvalidDefinitionException exception) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, exception.getPath().get(0).getFieldName()));
        } else if (cause instanceof MismatchedInputException exception) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, exception.getPath().get(0).getFieldName()));
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

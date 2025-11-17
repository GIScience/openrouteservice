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
import org.heigit.ors.api.APIEnums;
import org.heigit.ors.api.errors.CommonResponseEntityExceptionHandler;
import org.heigit.ors.api.requests.matching.MatchingApiRequest;
import org.heigit.ors.api.responses.matching.MatchingResponse;
import org.heigit.ors.api.services.MatchingService;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.exceptions.*;
import org.heigit.ors.matching.MatchingErrorCodes;
import org.heigit.ors.matching.MatchingRequest;
import org.json.simple.JSONObject;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Matching service", description = "Match geometries to graph edges")
@RequestMapping("/v2/match")
public class MatchingAPI {
    static final CommonResponseEntityExceptionHandler errorHandler = new CommonResponseEntityExceptionHandler(MatchingErrorCodes.BASE);

    private final MatchingService matchingService;

    public MatchingAPI(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    // generic catch methods - when extra info is provided in the url, the other methods are accessed.
    @GetMapping
    @Operation(hidden = true)
    public void getGetMapping() throws MissingParameterException {
        throw new MissingParameterException(MatchingErrorCodes.MISSING_PARAMETER, "profile");
    }

    @PostMapping
    @Operation(hidden = true)
    public String getPostMapping(@RequestBody MatchingApiRequest request) throws MissingParameterException {
        throw new MissingParameterException(MatchingErrorCodes.MISSING_PARAMETER, "profile");
    }

    // Matches any response type that has not been defined
    @PostMapping(value = "/{profile}/*")
    @Operation(hidden = true)
    public void getInvalidResponseType(@PathVariable String profile) throws StatusCodeException {
        throw new StatusCodeException(HttpServletResponse.SC_NOT_ACCEPTABLE, MatchingErrorCodes.UNSUPPORTED_EXPORT_FORMAT, "Response format is not supported");
    }

    // Functional request methods
    @GetMapping(value = "/{profile}")
    @Operation(
            method = "GET",
            description = "Provide information about the graph used for matching.",
            summary = "Matching Service information"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Standard response for successfully processed requests. Returns JSON.",
            content = {@Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MatchingResponse.class)
            )
            })
    public ResponseEntity<String> getMatching(@Parameter(description = "Specifies the route profile.", required = true, example = "driving-car") @PathVariable String profile) throws StatusCodeException {
        MatchingService.MatchingInfo result = matchingService.generateMatchingInformation(profile);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("graph_timestamp", result.graphTimestamp());

        return new ResponseEntity<>(jsonResponse.toJSONString(), headers, HttpStatus.OK);
    }

    @PostMapping(value = "/{profile}")
    @Operation(
            description = """
                    Matches point, linestring and polygon geometries to edge IDs of the graph.
                    Note that matchings are invalidated when rebuilding the graph because the edge 
                    IDs may change.
                    """,
            summary = "Matching Service"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Standard response for successfully processed requests. Returns JSON.",
            content = {@Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MatchingResponse.class)
            )
            })
    public ResponseEntity<String> postMatching(@Parameter(description = "Specifies the route profile.", required = true, example = "driving-car") @PathVariable String profile,
                                               @Parameter(description = "The request payload", required = true) @RequestBody MatchingApiRequest request) throws StatusCodeException {
        request.setProfile(getProfileEnum(profile));
        request.setProfileName(profile);

        MatchingRequest.MatchingResult result = matchingService.generateMatchingFromRequest(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("graph_timestamp", result.graphTimestamp());
        jsonResponse.put("edge_ids", result.matched());

        return new ResponseEntity<>(jsonResponse.toJSONString(), headers, HttpStatus.OK);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMessageConversionException.class, Exception.class})
    public ResponseEntity<Object> handleReadingBodyException(final Exception e) {
        final Throwable cause = e.getCause();
        if (cause instanceof UnrecognizedPropertyException exception) {
            return errorHandler.handleUnknownParameterException(new UnknownParameterException(MatchingErrorCodes.UNKNOWN_PARAMETER, exception.getPropertyName()));
        } else if (cause instanceof InvalidFormatException exception) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(MatchingErrorCodes.INVALID_PARAMETER_FORMAT, exception.getValue().toString()));
        } else if (cause instanceof InvalidDefinitionException exception) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(MatchingErrorCodes.INVALID_PARAMETER_VALUE, exception.getPath().get(0).getFieldName()));
        } else if (cause instanceof MismatchedInputException exception) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(MatchingErrorCodes.INVALID_PARAMETER_FORMAT, exception.getPath().get(0).getFieldName()));
        } else if (cause instanceof ConversionFailedException exception) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(MatchingErrorCodes.INVALID_PARAMETER_VALUE, (String) exception.getValue()));
        } else {
            // Check if we are missing the body as a whole
            if (e.getLocalizedMessage() != null && e.getLocalizedMessage().startsWith("Required request body is missing")) {
                return errorHandler.handleStatusCodeException(new EmptyElementException(MatchingErrorCodes.MISSING_PARAMETER, "Request body could not be read"));
            }
            return errorHandler.handleGenericException(e);
        }
    }

    @ExceptionHandler(StatusCodeException.class)
    public ResponseEntity<Object> handleException(final StatusCodeException e) {
        return errorHandler.handleStatusCodeException(e);
    }

    private APIEnums.Profile getProfileEnum(String profile) throws ParameterValueException {
        EncoderNameEnum encoderForProfile = matchingService.getEncoderForProfile(profile);
        return APIEnums.Profile.forValue(encoderForProfile.getEncoderName());
    }
}

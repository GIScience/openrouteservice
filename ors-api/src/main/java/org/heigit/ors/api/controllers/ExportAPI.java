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
import io.swagger.annotations.*;
import org.heigit.ors.api.errors.CommonResponseEntityExceptionHandler;
import org.heigit.ors.api.requests.export.ExportRequest;
import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.api.responses.export.json.JsonExportResponse;
import org.heigit.ors.export.ExportErrorCodes;
import org.heigit.ors.export.ExportResult;
import org.heigit.ors.exceptions.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@Api(value = "Export Service", description = "Export the base graph for different modes of transport", tags = "Export")
@RequestMapping("/v2/export")
@ApiResponses({
        @ApiResponse(code = 400, message = "The request is incorrect and therefore can not be processed."),
        @ApiResponse(code = 404, message = "An element could not be found. If possible, a more detailed error code is provided."),
        @ApiResponse(code = 405, message = "The specified HTTP method is not supported. For more details, refer to the EndPoint documentation."),
        @ApiResponse(code = 413, message = "The request is larger than the server is able to process, the data provided in the request exceeds the capacity limit."),
        @ApiResponse(code = 500, message = "An unexpected error was encountered and a more detailed error code is provided."),
        @ApiResponse(code = 501, message = "Indicates that the server does not support the functionality needed to fulfill the request."),
        @ApiResponse(code = 503, message = "The server is currently unavailable due to overload or maintenance.")
})
public class ExportAPI {
    static final CommonResponseEntityExceptionHandler errorHandler = new CommonResponseEntityExceptionHandler(ExportErrorCodes.BASE);

    // generic catch methods - when extra info is provided in the url, the other methods are accessed.
    @GetMapping
    @ApiOperation(value = "", hidden = true)
    public void getGetMapping() throws MissingParameterException {
        throw new MissingParameterException(ExportErrorCodes.MISSING_PARAMETER, "profile");
    }

    @PostMapping
    @ApiOperation(value = "", hidden = true)
    public String getPostMapping(@RequestBody ExportRequest request) throws MissingParameterException {
        throw new MissingParameterException(ExportErrorCodes.MISSING_PARAMETER, "profile");
    }

    // Matches any response type that has not been defined
    @PostMapping(value="/{profile}/*")
    @ApiOperation(value = "", hidden = true)
    public void getInvalidResponseType() throws StatusCodeException {
        throw new StatusCodeException(HttpServletResponse.SC_NOT_ACCEPTABLE, ExportErrorCodes.UNSUPPORTED_EXPORT_FORMAT, "This response format is not supported");
    }

    // Functional request methods
    @PostMapping(value = "/{profile}")
    @ApiOperation(notes = "Returns a list of points, edges and weights within a given bounding box for a selected profile as JSON", value = "Export Service (POST)", httpMethod = "POST", consumes = "application/json", produces = "application/json")
    @ApiResponses(
            @ApiResponse(code = 200,
                    message = "Standard response for successfully processed requests. Returns JSON.", //TODO: add docs
                    response = JsonExportResponse.class)
    )
    public JsonExportResponse getDefault(@ApiParam(value = "Specifies the route profile.", required = true, example = "driving-car") @PathVariable APIEnums.Profile profile,
                                             @ApiParam(value = "The request payload", required = true) @RequestBody ExportRequest request) throws StatusCodeException {
        return getJsonExport(profile, request);
    }

    @PostMapping(value = "/{profile}/json", produces = {"application/json;charset=UTF-8"})
    @ApiOperation(notes = "Returns a list of points, edges and weights within a given bounding box for a selected profile JSON", value = "Export Service JSON (POST)", httpMethod = "POST", consumes = "application/json", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "JSON Response", response = JsonExportResponse.class)
    })
    public JsonExportResponse getJsonExport(
            @ApiParam(value = "Specifies the profile.", required = true, example = "driving-car") @PathVariable APIEnums.Profile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody ExportRequest request) throws StatusCodeException {
        request.setProfile(profile);
        request.setResponseType(APIEnums.CentralityResponseType.JSON);

        ExportResult result = request.generateExportFromRequest();

        return new JsonExportResponse(result);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingParams(final MissingServletRequestParameterException e) {
        return errorHandler.handleStatusCodeException(new MissingParameterException(ExportErrorCodes.MISSING_PARAMETER, e.getParameterName()));
    }


    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMessageConversionException.class, Exception.class})
    public ResponseEntity<Object> handleReadingBodyException(final Exception e) {
        final Throwable cause = e.getCause();
        if (cause instanceof UnrecognizedPropertyException) {
            return errorHandler.handleUnknownParameterException(new UnknownParameterException(ExportErrorCodes.UNKNOWN_PARAMETER, ((UnrecognizedPropertyException) cause).getPropertyName()));
        } else if (cause instanceof InvalidFormatException) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(ExportErrorCodes.INVALID_PARAMETER_FORMAT, ((InvalidFormatException) cause).getValue().toString()));
        } else if (cause instanceof InvalidDefinitionException) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(ExportErrorCodes.INVALID_PARAMETER_VALUE, ((InvalidDefinitionException) cause).getPath().get(0).getFieldName()));
        } else if (cause instanceof MismatchedInputException) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(ExportErrorCodes.MISMATCHED_INPUT, ((MismatchedInputException) cause).getPath().get(0).getFieldName()));
        } else {
            // Check if we are missing the body as a whole
            if (e.getLocalizedMessage().startsWith("Required request body is missing")) {
                return errorHandler.handleStatusCodeException(new EmptyElementException(ExportErrorCodes.MISSING_PARAMETER, "Request body could not be read"));
            }
            return errorHandler.handleGenericException(e);
        }
    }

    @ExceptionHandler(StatusCodeException.class)
    public ResponseEntity<Object> handleException(final StatusCodeException e) {
        return errorHandler.handleStatusCodeException(e);
    }
}

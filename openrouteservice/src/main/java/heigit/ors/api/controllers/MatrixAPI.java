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
import heigit.ors.api.requests.matrix.MatrixRequest;
import heigit.ors.api.requests.matrix.MatrixRequestHandler;
import heigit.ors.api.responses.matrix.JSONMatrixResponseObjects.JSONMatrixResponse;
import heigit.ors.exceptions.MissingParameterException;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterException;
import heigit.ors.matrix.MatrixErrorCodes;
import heigit.ors.matrix.MatrixResult;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Api(value = "/v2/matrix", description = "Get a Matrix calculation")
@RequestMapping("/v2/matrix")
public class MatrixAPI {
    final static CommonResponseEntityExceptionHandler errorHandler = new CommonResponseEntityExceptionHandler(MatrixErrorCodes.BASE);

    // generic catch methods - when extra info is provided in the url, the other methods are accessed.
    @GetMapping
    @ApiOperation(value = "", hidden = true)
    public void getGetMapping() throws MissingParameterException {
        throw new MissingParameterException(MatrixErrorCodes.MISSING_PARAMETER, "profile");
    }

    @PostMapping
    @ApiOperation(value = "", hidden = true)
    public String getPostMapping() throws MissingParameterException {
        throw new MissingParameterException(MatrixErrorCodes.MISSING_PARAMETER, "profile");
    }

    // Matches any response type that has not been defined
    @PostMapping(value="/{profile}/*")
    public void getInvalidResponseType() throws StatusCodeException {
        throw new StatusCodeException(HttpServletResponse.SC_NOT_ACCEPTABLE, MatrixErrorCodes.UNSUPPORTED_EXPORT_FORMAT, "This response format is not supported");
    }

    // Functional request methods

    @PostMapping(value = "/{profile}")
    public JSONMatrixResponse getDefault(@ApiParam(value = "Specifies the matrix profile.") @PathVariable APIEnums.Profile profile,
                                         @ApiParam(value = "The request payload", required = true) @RequestBody MatrixRequest request) throws Exception {
        return getJsonMime(profile, request);
    }

    @PostMapping(value = "/{profile}/json", produces = {"application/json;charset=UTF-8"})
    @ApiOperation(value = "Get a matrix calculation from the specified profile", httpMethod = "POST", consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "JSON Response", response = JSONMatrixResponse.class)
    })
    public JSONMatrixResponse getJsonMime(
            //TODO Flexible mode???
            @ApiParam(value = "Specifies the matrix profile.", required = true) @PathVariable APIEnums.Profile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody MatrixRequest originalRequest) throws StatusCodeException {
        originalRequest.setProfile(profile);
        originalRequest.setResponseType(APIEnums.MatrixResponseType.JSON);
        List<heigit.ors.matrix.MatrixRequest> matrixRequests = MatrixRequestHandler.convertMatrixRequest(originalRequest);
        List<MatrixResult> matrixResults = MatrixRequestHandler.generateRouteFromRequests(matrixRequests);

        return new JSONMatrixResponse(matrixResults, matrixRequests, originalRequest);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingParams(final MissingServletRequestParameterException e) {
        return errorHandler.handleStatusCodeException(new MissingParameterException(MatrixErrorCodes.MISSING_PARAMETER, e.getParameterName()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMessageConversionException.class, Exception.class})
    public ResponseEntity<Object> handleReadingBodyException(final Exception e) {
        final Throwable cause = e.getCause();
        if (cause instanceof UnrecognizedPropertyException) {
            return errorHandler.handleUnknownParameterException(new UnknownParameterException(MatrixErrorCodes.UNKNOWN_PARAMETER, ((UnrecognizedPropertyException) cause).getPropertyName()));
        } else if (cause instanceof InvalidFormatException) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, ((InvalidFormatException) cause).getValue().toString()));
        } else if (cause instanceof InvalidDefinitionException) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, ((InvalidDefinitionException) cause).getPath().get(0).getFieldName()));
        } else if (cause instanceof MismatchedInputException) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, ((MismatchedInputException) cause).getPath().get(0).getFieldName()));
        } else {
            return errorHandler.handleGenericException(e);
        }
    }

    @ExceptionHandler(StatusCodeException.class)
    public ResponseEntity<Object> handleException(final StatusCodeException e) {
        return errorHandler.handleStatusCodeException(e);
    }
}

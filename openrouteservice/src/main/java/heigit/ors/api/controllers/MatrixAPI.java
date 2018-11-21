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
import heigit.ors.api.errors.MatrixResponseEntityExceptionHandler;
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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(value = "/v2/matrix", description = "Get a Matrix calculation")
@RequestMapping("/v2/matrix")
public class MatrixAPI {

    @PostMapping
    @ApiOperation(value = "", hidden = true)
    public String getPostMapping(@RequestBody MatrixRequest request) throws MissingParameterException {
        throw new MissingParameterException(MatrixErrorCodes.MISSING_PARAMETER, "profile");
    }

    @PostMapping(value = "/{profile}")
    public JSONMatrixResponse getDefault(@ApiParam(value = "Specifies the matrix profile.") @PathVariable APIEnums.MatrixProfile profile,
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
            @ApiParam(value = "Specifies the matrix profile.", required = true) @PathVariable APIEnums.MatrixProfile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody MatrixRequest originalRequest) throws StatusCodeException {
        originalRequest.setProfile(profile);
        originalRequest.setResponseType(APIEnums.MatrixResponseType.JSON);
        List<heigit.ors.matrix.MatrixRequest> matrixRequests = MatrixRequestHandler.convertMatrixRequest(originalRequest);
        List<MatrixResult> matrixResults = MatrixRequestHandler.generateRouteFromRequests(matrixRequests);

        return new JSONMatrixResponse(matrixResults, matrixRequests, originalRequest);
    }

    // Errors generated from the reading of the request (before entering the routing system). Normally these are where
    // parameters have been entered incorrectly in the request
    @ExceptionHandler
    public ResponseEntity handleError(final HttpMessageNotReadableException e) {
        final Throwable cause = e.getCause();
        final MatrixResponseEntityExceptionHandler h = new MatrixResponseEntityExceptionHandler();
        if (cause instanceof UnrecognizedPropertyException) {
            return h.handleUnknownParameterException(new UnknownParameterException(MatrixErrorCodes.UNKNOWN, ((UnrecognizedPropertyException) cause).getPropertyName()));
        } else if (cause instanceof InvalidFormatException) {
            return h.handleStatusCodeException(new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, ((InvalidFormatException) cause).getValue().toString()));
        } else if (cause instanceof InvalidDefinitionException) {
            return h.handleStatusCodeException(new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, ((InvalidDefinitionException) cause).getPath().get(0).getFieldName()));
        } else if (cause instanceof MismatchedInputException) {
            return h.handleStatusCodeException(new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, ((MismatchedInputException) cause).getPath().get(0).getFieldName()));
        } else {
            return h.handleGenericException(e);
        }
    }
}

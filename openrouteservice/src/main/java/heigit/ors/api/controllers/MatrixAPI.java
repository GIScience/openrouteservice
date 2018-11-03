package heigit.ors.api.controllers;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import heigit.ors.api.errors.RoutingResponseEntityExceptionHandler;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.api.requests.matrix.SpringMatrixRequest;
import heigit.ors.api.requests.matrix.MatrixRequestHandler;
import heigit.ors.api.responses.matrix.JSONMatrixResponseObjects.JSONMatrixResponse;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONRouteResponse;
import heigit.ors.exceptions.*;
import heigit.ors.matrix.MatrixErrorCodes;
import heigit.ors.matrix.MatrixResult;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(value = "/v2/matrix", description = "Get a Matrix calculation")
@RequestMapping("/v2/matrix")
public class MatrixAPI {
    @PostMapping
    @ApiOperation(value = "", hidden = true)
    public String getPostMapping(@RequestBody SpringMatrixRequest request) throws MissingParameterException {
        throw new MissingParameterException(MatrixErrorCodes.MISSING_PARAMETER, "profile");
    }

    @PostMapping(value = "/{profile}")
    public JSONMatrixResponse getDefault(@ApiParam(value = "Specifies the matrix profile.") @PathVariable APIEnums.MatrixProfile profile,
                                         @ApiParam(value = "The request payload", required = true) @RequestBody SpringMatrixRequest request) throws Exception {
        return getJsonMime(profile, request);
    }

    @PostMapping(value = "/{profile}/json", produces = {"application/json;charset=UTF-8"})
    @ApiOperation(value = "Get a matrix calculation from the specified profile", httpMethod = "POST", consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "JSON Response", response = JSONMatrixResponse.class)
    })
    public JSONMatrixResponse getJsonMime(
            @ApiParam(value = "Specifies the matrix profile.", required = true) @PathVariable APIEnums.MatrixProfile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody SpringMatrixRequest request) throws StatusCodeException {
        request.setProfile(profile);
        request.setResponseType(APIEnums.MatrixResponseType.JSON);

        MatrixResult result = MatrixRequestHandler.generateRouteFromRequest(request);

        return new JSONMatrixResponse(new MatrixResult[]{result}, request);
        // Todo End
    }

    // Errors generated from the reading of the request (before entering the routing system). Normally these are where
    // parameters have been entered incorrectly in the request
    @ExceptionHandler
    public ResponseEntity handleError(final HttpMessageNotReadableException e) {
        final Throwable cause = e.getCause();
        final RoutingResponseEntityExceptionHandler h = new RoutingResponseEntityExceptionHandler();
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

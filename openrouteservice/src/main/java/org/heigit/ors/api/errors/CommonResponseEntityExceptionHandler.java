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

package heigit.ors.api.errors;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterException;
import heigit.ors.isochrones.IsochronesErrorCodes;
import heigit.ors.util.AppInfo;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@RestController
public class CommonResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String EXCEPTION_MESSAGE = "Exception";
    protected static Logger LOCAL_LOGGER = Logger.getLogger(CommonResponseEntityExceptionHandler.class.getName());

    final int errorCodeBase;

    public CommonResponseEntityExceptionHandler() {
        errorCodeBase = 0;
    }

    public CommonResponseEntityExceptionHandler(int errorCodeBase) {
        this.errorCodeBase = errorCodeBase;
    }

    public ResponseEntity handleStatusCodeException(StatusCodeException exception) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (LOCAL_LOGGER.isDebugEnabled()) {
            // Log also the stack trace
            LOCAL_LOGGER.error(EXCEPTION_MESSAGE, exception);
        } else {
            // Log only the error message
            LOCAL_LOGGER.error(exception);
        }

        return new ResponseEntity(constructErrorBody(exception), headers, convertOrsToSpringHttpCode(exception.getStatusCode()));
    }

    public ResponseEntity handleUnknownParameterException(UnknownParameterException exception) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (LOCAL_LOGGER.isDebugEnabled()) {
            // Log also the stack trace
            LOCAL_LOGGER.error(EXCEPTION_MESSAGE, exception);
        } else {
            // Log only the error message
            LOCAL_LOGGER.error(exception);
        }

        return new ResponseEntity(constructErrorBody(exception), headers, convertOrsToSpringHttpCode(exception.getStatusCode()));
    }

    public ResponseEntity handleGenericException(Exception exception) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (LOCAL_LOGGER.isDebugEnabled()) {
            // Log also the stack trace
            LOCAL_LOGGER.error(EXCEPTION_MESSAGE, exception);
        } else {
            // Log only the error message
            LOCAL_LOGGER.error(exception);
        }

        Throwable cause = exception.getCause();
        if (cause instanceof InvalidDefinitionException) {
            InvalidDefinitionException e = (InvalidDefinitionException) cause;
            if (e.getCause() instanceof StatusCodeException) {
                StatusCodeException origExc = (StatusCodeException) e.getCause();
                return new ResponseEntity(constructErrorBody(origExc), headers, HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity(constructErrorBody(new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "")), headers, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(constructGenericErrorBody(exception), headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpStatus convertOrsToSpringHttpCode(int statusCode) {
        return HttpStatus.valueOf(statusCode);
    }

    private String constructErrorBody(StatusCodeException exception) {
        int errorCode = exception.getInternalCode();
        if (errorCode < 100) {
            errorCode = errorCodeBase + errorCode;
        }

        JSONObject json = constructJsonBody(exception, errorCode);

        return json.toString();
    }

    private JSONObject constructJsonBody(Exception exception, int internalErrorCode) {
        JSONObject json = new JSONObject();

        JSONObject jError = new JSONObject();
        jError.put("message", exception.getMessage());

        if (internalErrorCode > 0) {
            jError.put("code", internalErrorCode);
        }

        json.put("error", jError);

        JSONObject jInfo = new JSONObject();
        jInfo.put("engine", AppInfo.getEngineInfo());
        jInfo.put("timestamp", System.currentTimeMillis());
        json.put("info", jInfo);

        return json;
    }

    private String constructGenericErrorBody(Exception exception) {
        return constructJsonBody(exception, -1).toString();
    }
}

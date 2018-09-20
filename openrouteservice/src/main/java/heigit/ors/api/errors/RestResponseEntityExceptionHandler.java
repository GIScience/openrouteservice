package heigit.ors.api.errors;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.util.AppInfo;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@RestController
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    protected static Logger LOGGER = Logger.getLogger(RestResponseEntityExceptionHandler.class.getName());

    @ExceptionHandler(value = InvalidDefinitionException.class)
    protected ResponseEntity handleInvalidDefinitionException(InvalidDefinitionException exception) {
        return handleStatusCodeException((StatusCodeException) exception.getCause());
    }

    @ExceptionHandler(value = StatusCodeException.class)
    protected ResponseEntity handleStatusCodeException(StatusCodeException exception) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if(LOGGER.isDebugEnabled()) {
            // Log also the stack trace
            LOGGER.error("Exception", exception);
        } else {
            // Log only the error message
            LOGGER.error(exception);
        }

        return new ResponseEntity(constructErrorBody(exception), headers, convertStatus(exception.getStatusCode()));
    }

    @ExceptionHandler(value = Exception.class)
    protected ResponseEntity handleGenericException(Exception exception) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if(LOGGER.isDebugEnabled()) {
            // Log also the stack trace
            LOGGER.error("Exception", exception);
        } else {
            // Log only the error message
            LOGGER.error(exception);
        }

        return new ResponseEntity(constructGenericErrorBody(exception), headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpStatus convertStatus(int statusCode) {
        return HttpStatus.valueOf(statusCode);
    }

    private String constructErrorBody(StatusCodeException exception) {
        JSONObject json = constructJsonBody(exception);

        int errorCode = -1;

        errorCode = exception.getInternalCode();

        if (errorCode > 0)
        {
            json.put("code", errorCode);
        }

        return json.toString();
    }

    private JSONObject constructJsonBody(Exception exception) {
        JSONObject json = new JSONObject();

        JSONObject jError = new JSONObject();
        jError.put("message", exception.getMessage());
        json.put("error", jError);

        JSONObject jInfo = new JSONObject();
        jInfo.put("engine", AppInfo.getEngineInfo());
        jInfo.put("timestamp", System.currentTimeMillis());
        json.put("info", jInfo);

        return json;
    }

    private String constructGenericErrorBody(Exception exception) {
        return constructJsonBody(exception).toString();
    }
}

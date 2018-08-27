package heigit.ors.controllersOld.converters2;

import heigit.ors.controllersOld.DataTransferObjects2.RouteResponseDTO;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;

public class RouteResponseConverter extends AbstractHttpMessageConverter<RouteResponseDTO> {
    public RouteResponseConverter() {
        super(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML);
    }

    @Override
    protected boolean supports(Class<?> inClass) {
        if(inClass.isAssignableFrom(RouteResponseDTO.class))
            return true;
        else
            return false;
    }

    @Override
    protected RouteResponseDTO readInternal(Class<? extends RouteResponseDTO> arg0, HttpInputMessage arg1)
            throws IOException, HttpMessageNotReadableException
    {

        return null;

    }

    @Override
    protected void writeInternal(RouteResponseDTO routeObject, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException
    {
        RouteResponseMessageConverter messageConverter = null;

        switch(routeObject.getType()) {
            case GPX:
                outputMessage.getHeaders().setContentType(MediaType.APPLICATION_XML);
                messageConverter = new RouteResponseToGPXConverter();
                break;
            case JSON:
            case GEOJSON:
                outputMessage.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                messageConverter = new RouteResponseToJSONConverter();
                break;

        }

        if(messageConverter == null)
            throw new IOException();

        String messageBody = messageConverter.generateResponseBody(routeObject);

        outputMessage.getBody().write(messageBody.getBytes());
    }
}

package heigit.ors.controllersOld.converters2;

import heigit.ors.controllersOld.DataTransferObjects2.RouteResponseDTO;

import java.io.IOException;

public interface RouteResponseMessageConverter {
    String generateResponseBody(RouteResponseDTO routeObject) throws IOException;
}

package heigit.ors.controllersOld.converters2;

import com.fasterxml.jackson.databind.ObjectMapper;
import heigit.ors.controllersOld.DataTransferObjects2.RouteResponseDTO;

import java.io.IOException;

public class RouteResponseToJSONConverter implements RouteResponseMessageConverter {

    @Override
    public String generateResponseBody(RouteResponseDTO routeObject) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonStr = mapper.writeValueAsString(routeObject);

        return jsonStr;
    }
}

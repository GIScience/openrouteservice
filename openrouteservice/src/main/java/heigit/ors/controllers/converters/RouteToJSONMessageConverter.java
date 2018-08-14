package heigit.ors.controllers.converters;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import heigit.ors.controllers.responseHolders.GPXRouteResponse;
import heigit.ors.controllers.responseHolders.JSONRouteResponse;
import heigit.ors.controllers.responseHolders.Route;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;

public class RouteToJSONMessageConverter extends AbstractHttpMessageConverter<JSONRouteResponse> {
    public RouteToJSONMessageConverter() {
        super(MediaType.ALL);
    }

    @Override
    protected boolean supports(Class<?> inClass) {
        if(inClass.isAssignableFrom(JSONRouteResponse.class))
            return true;
        else
            return false;
    }

    @Override
    protected JSONRouteResponse readInternal(Class<? extends JSONRouteResponse> inputObject, HttpInputMessage responseInput)
            throws IOException, HttpMessageNotReadableException
    {
        return new JSONRouteResponse();

    }



    @Override
    protected void writeInternal(JSONRouteResponse routeObject, HttpOutputMessage responseOutput)
            throws IOException, HttpMessageNotWritableException
    {
        responseOutput.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Route route = routeObject.getRoute();
        JSONObject json = new JSONObject();
        json.put("start", route.getStartPoint().toString());
        json.put("destination", route.getDestinationPoint().toString());

        json.put("route", route.getRouteGeom());

        responseOutput.getBody().write(json.toString().getBytes());
    }
}

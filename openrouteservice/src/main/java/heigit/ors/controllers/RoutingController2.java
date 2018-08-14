package heigit.ors.controllers;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.controllers.converters.CoordinateSequenceWrapper;
import heigit.ors.controllers.responseHolders.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/routes")
public class RoutingController2 {

    @GetMapping
    public RouteResponse getRouteResponse(
            @RequestParam(name="type") String type,
            @RequestParam(name="start") Coordinate startCoords,
            @RequestParam(name="dest") Coordinate destinationCoords,
            @RequestParam(name="via", required = false) CoordinateSequenceWrapper viaPoints) {
        RouteResponse rr;


        Route route = new Route(startCoords, destinationCoords, viaPoints.getCoordinates().toArray(new Coordinate[viaPoints.getCoordinates().size()]));

        if(type.equalsIgnoreCase("gpx"))
            rr = new GPXRouteResponse();
        else if(type.equalsIgnoreCase("geojson"))
            rr = new GeoJSONRouteResponse();
        else
            rr = new JSONRouteResponse();

        rr.setRoute(route);

        return rr;
    }
}


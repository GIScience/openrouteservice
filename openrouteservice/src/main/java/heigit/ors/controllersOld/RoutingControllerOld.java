package heigit.ors.controllersOld;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.controllersOld.converters2.CoordinateSequenceWrapper;
import heigit.ors.controllersOld.responseHolders2.*;
import heigit.ors.routing.*;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/routes3")
public class RoutingControllerOld {

    @GetMapping
    public RouteResponse getRouteResponse(
            @RequestParam(name="type") String type,
            @RequestParam(name="start") Coordinate startCoords,
            @RequestParam(name="dest") Coordinate destinationCoords,
            @RequestParam(name="via", required = false) CoordinateSequenceWrapper viaPoints,
            @RequestParam(name="profile") String profile) {

        RouteResponse rr;

        RoutingRequest request = new RoutingRequest();

        ArrayList<Coordinate> coords = new ArrayList<>();
        coords.add(startCoords);
        if(viaPoints != null)
            coords.addAll(viaPoints.getCoordinates());
        coords.add(destinationCoords);


        RouteResult result;
        try {
            RouteSearchParameters params = new RouteSearchParameters();
            params.setProfileType(RoutingProfileType.getFromString(profile));

            request.setSearchParameters(params);

            request.setCoordinates(coords.toArray(new Coordinate[coords.size()]));

            result = RoutingProfileManager.getInstance().computeRoute(request);
        } catch (Exception e) {
            return null;
        }

        if(type.equalsIgnoreCase("gpx"))
            rr = new GPXRouteResponse();
        else if(type.equalsIgnoreCase("geojson"))
            rr = new GeoJSONRouteResponse();
        else
            rr = new JSONRouteResponse();

        rr.setRouteResult(result);

        return rr;
    }

    @PostMapping
    public String generateRouteFromPost(
            @RequestParam(name="start") Coordinate startCoords,
            @RequestParam(name="dest") Coordinate destination,
            @RequestBody RoutingRequest request) {
        return "test";
    }

}


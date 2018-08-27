package heigit.ors.controllersOld;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.controllersOld.DataTransferObjects2.*;
import heigit.ors.routing.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/routes2")
public class RoutingController2Old {

    @GetMapping
    public RouteResponseDTO getSimpleRoute(
            @RequestParam("profile") String profile,
            @RequestParam("start") Coordinate start,
            @RequestParam("end") Coordinate end) {
        RouteRequestDTO request = new RouteRequestDTO();
        request.setRoutingProfile(profile);

        RouteResponseDTO response = new RouteResponseDTO(request);
        response.setType(RouteResponseType.GEOJSON);

        RouteObjectDTO route = new RouteObjectDTO(request);
        response.addRoute(route);

        return response;
    }

    @PostMapping
    public RouteResponseDTO getRoute(
            @RequestParam(value = "profile") String profile,
            @RequestBody RouteRequestDTO routeRequest) {
        routeRequest.setRoutingProfile(profile);

        RoutingRequest request = new RoutingRequest();

        RouteResult result;
        try {
            RouteSearchParameters params = new RouteSearchParameters();
            params.setProfileType(RoutingProfileType.getFromString(profile));

            request.setSearchParameters(params);

            request.setCoordinates(routeRequest.getCoordinates().getAsCoordinateArray());

            result = RoutingProfileManager.getInstance().computeRoute(request);
        } catch (Exception e) {
            return null;
        }

        RouteResponseDTO response = new RouteResponseDTO(routeRequest);

        response.setType(routeRequest.getType());

        RouteObjectDTO route = new RouteObjectDTO(routeRequest);

        //route.setRoute(result);
        //route.setSummary(new RouteSummary(1234, 567));

        response.addRoute(route);
        return response;
    }
}
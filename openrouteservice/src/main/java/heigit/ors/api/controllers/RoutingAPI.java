package heigit.ors.api.controllers;

import heigit.ors.api.dataTransferObjects.RouteRequestDTO;
import heigit.ors.api.dataTransferObjects.RouteResponseDTO;
import heigit.ors.api.dataTransferObjects.RouteResponseFactory;
import heigit.ors.api.dataTransferObjects.RouteResponseGeometryType;
import heigit.ors.api.responses.routing.GPXRouteResponse;
import heigit.ors.api.responses.routing.JSONRouteResponse;
import heigit.ors.api.responses.routing.RouteResponse;
import heigit.ors.routing.*;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@Api(value="/routes", description = "Get a route", consumes = "application/json")
@RequestMapping("/routes")
public class RoutingAPI {
    /*@GetMapping("/gpx")
    public RouteResponse getGpx() {
        RouteResponse resp = new GPXRouteResponse(new RouteResponseDTO());

        return resp;
    }

    @GetMapping("/json")
    public RouteResponse getJSON(@RequestParam("type") String type) {
        RouteRequestDTO request = new RouteRequestDTO();
        request.

        RouteResult result = null;
        try {
            result = RoutingProfileManager.getInstance().computeRoute(new RoutingRequest());

        } catch (Exception e) {

        }

        return new JSONRouteResponse(result, RouteResponseGeometryType.ENCODED_POLYLINE);
    }*/

    @PostMapping("/{profile}")
    @ApiOperation(value = "Get a route from the specified profile", httpMethod = "POST")
    public RouteResponse getPost(
            @ApiParam(value = "Name of the profile to use for routing") @PathVariable String profile,
            @RequestBody RouteRequestDTO request) {
        request.setProfile(profile);
        return buildResponse(request);

    }

    private RouteResponse buildResponse(RouteRequestDTO request) {
        RouteResult result = null;




        try {
            RoutingRequest routingRequest = new RoutingRequest();
            routingRequest.setCoordinates(request.getCoordinates());

            RouteSearchParameters params = new RouteSearchParameters();
            params.setProfileType(RoutingProfileType.getFromString(request.getProfile()));

            routingRequest.setSearchParameters(params);
            result = RoutingProfileManager.getInstance().computeRoute(routingRequest);
        } catch (Exception e) {

        }

        return RouteResponseFactory.constructResponse(result, request);
    }
}

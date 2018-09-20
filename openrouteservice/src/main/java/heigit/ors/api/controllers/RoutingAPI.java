package heigit.ors.api.controllers;

import heigit.ors.api.requests.routing.APIRoutingEnums;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.GPXRouteResponseObjects.GPXRouteResponse;
import heigit.ors.api.responses.routing.GeoJSONRouteResponseObjects.GeoJSONRouteResponse;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONRouteResponse;
import heigit.ors.api.responses.routing.RouteResponseFactory;
import heigit.ors.api.responses.routing.RouteResponse;
import heigit.ors.routing.*;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;

import java.awt.*;

@RestController
@Api(value="/routes", description = "Get a route", consumes = "application/json")
@RequestMapping("/routes")
public class RoutingAPI {

    @GetMapping("/{profile}")
    public String getMapping(
            @ApiParam(value = "Specifies the route profile.") @PathVariable APIRoutingEnums.RoutingProfile profile
    ) {
        return "Hello " + profile;
    }

    @PostMapping(value = "/{profile}/json", produces = "application/json")
    @ApiOperation(value = "Get a route from the specified profile", httpMethod = "POST")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "JSON Response", response = JSONRouteResponse.class)
    })
    public RouteResponse getJSON(
            @ApiParam(value = "Specifies the route profile.") @PathVariable APIRoutingEnums.RoutingProfile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody RouteRequest request) throws Exception {
        request.setProfile(profile);
        request.setResponseType(APIRoutingEnums.RouteResponseType.JSON);

        //throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "profile", "test");
        return buildResponse(request);

    }

    @PostMapping(value = "/{profile}/gpx", produces = "application/xml")
    @ApiOperation(value = "Get a route from the specified profile", httpMethod = "POST")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "JSON Response", response = GPXRouteResponse.class)
    })
    public RouteResponse getGPX(
            @ApiParam(value = "Specifies the route profile.") @PathVariable APIRoutingEnums.RoutingProfile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody RouteRequest request) throws Exception {
        request.setProfile(profile);
        request.setResponseType(APIRoutingEnums.RouteResponseType.GPX);


        return buildResponse(request);

    }

    @PostMapping(value = "/{profile}", produces = "application/xml")
    @ApiOperation(value = "Get a route from the specified profile", httpMethod = "POST", consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "GPX Response", response = GPXRouteResponse.class)
    })
    public RouteResponse getGPXMime(
            @ApiParam(value = "Specifies the route profile.") @PathVariable APIRoutingEnums.RoutingProfile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody RouteRequest request) throws Exception {
        request.setProfile(profile);
        request.setResponseType(APIRoutingEnums.RouteResponseType.GPX);


        return buildResponse(request);

    }

    @PostMapping(value = "/{profile}", produces = "application/geo+json")
    @ApiOperation(value = "Get a route from the specified profile", httpMethod = "POST", consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "GeoJSON Response", response = GeoJSONRouteResponse.class)
    })
    public RouteResponse getGeoJsonMime(
            @ApiParam(value = "Specifies the route profile.") @PathVariable APIRoutingEnums.RoutingProfile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody RouteRequest request) throws Exception {
        request.setProfile(profile);
        request.setResponseType(APIRoutingEnums.RouteResponseType.GEOJSON);


        return buildResponse(request);
    }

    @PostMapping(value = "/{profile}", produces = "application/json")
    @ApiOperation(value = "Get a route from the specified profile", httpMethod = "POST", consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "JSON Response", response = JSONRouteResponse.class)
    })
    public RouteResponse getJsonMime(
            @ApiParam(value = "Specifies the route profile.") @PathVariable APIRoutingEnums.RoutingProfile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody RouteRequest request) throws Exception {
        request.setProfile(profile);
        request.setResponseType(APIRoutingEnums.RouteResponseType.JSON);


        return buildResponse(request);
    }

    private RouteResponse buildResponse(RouteRequest request) throws Exception {
        RouteResult result = RouteRequestHandler.generateRouteFromRequest(request);

        return RouteResponseFactory.constructResponse(new RouteResult[] { result }, request);
    }

    /*@ExceptionHandler(InvalidDefinitionException.class)
    public void handleException(InvalidDefinitionException e) throws ParameterValueException {
        // Throw the root exception

        ParameterValueException p = (ParameterValueException) e.getCause();
        return;
        //throw p;
    }*/
}

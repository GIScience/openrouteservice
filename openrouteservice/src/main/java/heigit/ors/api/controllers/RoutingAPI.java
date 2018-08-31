package heigit.ors.api.controllers;

import heigit.ors.api.requests.routing.APIRoutingEnums;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.RouteResponseFactory;
import heigit.ors.api.responses.routing.RouteResponse;
import heigit.ors.routing.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(value="/routes", description = "Get a route", consumes = "application/json")
@RequestMapping("/routes")
public class RoutingAPI {

    @PostMapping("/{profile}")
    @ApiOperation(value = "Get a route from the specified profile", httpMethod = "POST")
    public RouteResponse getPost(
            @ApiParam(value = "Specifies the route profile.") @PathVariable APIRoutingEnums.RoutingProfile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody RouteRequest request) throws Exception {
        request.setProfile(profile);

        //throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "profile", "test");
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

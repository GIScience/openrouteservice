package heigit.ors.controllersOld.DataTransferObjects2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class RouteResponseDTO {
    @JsonProperty(value = "request")
    private RouteRequestDTO routeRequest;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private ArrayList<RouteObjectDTO> routes;

    @JsonIgnore
    private RouteResponseType type;

    public RouteResponseDTO(RouteRequestDTO request) {
        this.routeRequest = request;
        this.routes = new ArrayList<>();
    }

    public void addRoute(RouteObjectDTO route) {
        this.routes.add(route);
    }

    public ArrayList getRoutes() {
        return this.routes;
    }

    public void setType(RouteResponseType type) {
        this.type = type;
    }

    public RouteResponseType getType() {
        return type;
    }

    public RouteRequestDTO getRouteRequest() {
        return routeRequest;
    }
}

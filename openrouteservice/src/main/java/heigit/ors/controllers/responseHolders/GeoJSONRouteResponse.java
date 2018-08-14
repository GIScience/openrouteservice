package heigit.ors.controllers.responseHolders;

public class GeoJSONRouteResponse extends RouteResponse {
    public GeoJSONRouteResponse() {
        super();
    }

    public String getType() {
        return "GeoJSON";
    }
}
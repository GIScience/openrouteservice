package heigit.ors.controllersOld.DataTransferObjects2;

import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.services.routing.RouteObject;

public class RouteObjectDTO {
    public RouteObject getRoute() {
        return route;
    }

    public void setRoute(RouteObject route) {
        this.route = route;
    }

    @JsonProperty(value = "geometry", access = JsonProperty.Access.READ_ONLY)
    private RouteGeometryDTO routeGeometry;

    private RouteObject route;

    private RouteSummary summary;

    public RouteObjectDTO(RouteGeometryFormat format) {
        setGeometryFormat(format);
    }

    public RouteObjectDTO(RouteObject route) {
        this.route = route;
    }

    public RouteObjectDTO(RouteRequestDTO request) {
        setGeometryFormat(request.getGeometryFormat());
    }

    private void setGeometryFormat(RouteGeometryFormat geometryFormat) {
        switch(geometryFormat) {
            case GEOJSON:
                routeGeometry = new RouteGeometryGeoJSONDTO();
                break;
            case JSON:
                routeGeometry = new RouteGeometryJSONDTO();
                break;
        }
    }

    public RouteGeometryDTO getRouteGeometry() {
        return this.routeGeometry;
    }

    public void setSummary(RouteSummary summary) {
        this.summary = summary;
    }

    public RouteSummary getSummary() {
        return summary;
    }


}

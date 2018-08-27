package heigit.ors.api.responses.routing;

import heigit.ors.api.dataTransferObjects.RouteResponseDTO;
import heigit.ors.api.dataTransferObjects.RouteResponseGeometryType;
import heigit.ors.routing.RouteResult;

import javax.xml.bind.annotation.XmlRootElement;

public class JSONRouteResponse extends RouteResponse {

    private double[] bbox;

    private GeometryResponse geomResponse;

    public JSONRouteResponse() {
        super();
    }

    public JSONRouteResponse(RouteResult routeResult, RouteResponseGeometryType geomType) {

        super(routeResult);
        geomResponse = GeometryResponseFactory.createGeometryResponse(routeResult.getGeometry(), false, geomType);
    }

    public double[] getBbox() {
        return bbox;
    }

    public void setBbox(double[] bbox) {
        this.bbox = bbox;
    }

    public GeometryResponse getGeomResponse() {
        return geomResponse;
    }

    public void setGeomResponse(GeometryResponse geomResponse) {
        this.geomResponse = geomResponse;
    }
}

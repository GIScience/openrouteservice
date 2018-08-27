package heigit.ors.api.dataTransferObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.routing.RoutingProfileType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;

@ApiModel(value = "RouteRequest")
public class RouteRequestDTO {
    @ApiModelProperty(value = "The start location of the route", required = true, position = 0)
    private double[] start;
    @ApiModelProperty(value = "The destination location of the route", required = true, position = 1)
    private double[] end;

    @ApiModelProperty(value = "Via points to use in the route", position = 2)
    private double[][] via;

    @ApiModelProperty(hidden = true)
    private String profile;

    @ApiModelProperty(name = "format",
            value = "The type of response to be returned")
    @JsonProperty("format")
    private RouteResponseType responseType;

    @ApiModelProperty(name = "geometry_type",
            value = "The type of geometry to be used in the response")
    @JsonProperty("geometry_type")
    private RouteResponseGeometryType geometryType;

    @ApiModelProperty(name = "options",
            value = "Additional options for the route request")
    @JsonProperty("options")
    private RouteRequestOptions routeOptions;

    public double[] getStart() {
        return start;
    }

    public void setStart(double[] start) {
        this.start = start;
    }

    public double[] getEnd() {
        return end;
    }

    public void setEnd(double[] end) {
        this.end = end;
    }

    public double[][] getVia() {
        return via;
    }

    public void setVia(double[][] via) {
        this.via = via;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public RouteResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(RouteResponseType responseType) {
        this.responseType = responseType;
    }

    public RouteResponseGeometryType getGeometryType() {
        return geometryType;
    }

    public void setGeometryType(RouteResponseGeometryType geometryType) {
        this.geometryType = geometryType;
    }

    public RouteRequestOptions getRouteOptions() {
        return routeOptions;
    }

    public void setRouteOptions(RouteRequestOptions routeOptions) {
        this.routeOptions = routeOptions;
    }

    @ApiIgnore
    @ApiModelProperty(hidden = true)
    public Coordinate[] getCoordinates() {
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(new Coordinate(start[0], start[1]));
        coordinates.add(new Coordinate(end[0], end[1]));

        return coordinates.toArray(new Coordinate[coordinates.size()]);
    }
}

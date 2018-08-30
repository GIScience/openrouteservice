package heigit.ors.api.requests.routing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.json.simple.JSONObject;

@ApiModel(value = "Route Options", description = "Advanced options for routing", parent = RouteRequest.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RouteRequestOptions {
    @ApiModelProperty(name = "avoid_features", value = "List of features to avoid.")
    @JsonProperty("avoid_features")
    private APIRoutingEnums.AvoidFeatures[] avoidFeatures;
    @JsonIgnore
    private boolean hasAvoidFeatures = false;

    @ApiModelProperty(name = "maximum_speed", value = "Specifies a maximum travel speed restriction in km/h.", example = "100")
    @JsonProperty("maximum_speed")
    private double maximumSpeed;
    @JsonIgnore
    private boolean hasMaximumSpeed = false;

    @ApiModelProperty(value = "\"all\" for no border crossing. \"controlled\" to cross open borders but avoid controlled ones. Only for driving-* profiles.")
    @JsonProperty("avoid_borders")
    private APIRoutingEnums.AvoidBorders avoidBorders;
    @JsonIgnore
    private boolean hasAvoidBorders = false;

    @ApiModelProperty(value = "List of countries to exclude from routing with driving-* profiles. Can be used together with \"avoid_borders\": \"controlled\". " +
            "[ 11, 193 ] would exclude Austria and Switzerland. List of countries and application examples can be found here.", example = "[ 11, 193 ]")
    @JsonProperty("avoid_countries")
    private int[] avoidCountries;
    @JsonIgnore
    private boolean hasAvoidCountries = false;

    @ApiModelProperty(value = "(for profile=driving-hgv only): hgv,bus,agricultural,delivery,forestry and goods. It is needed for vehicle restrictions to work.")
    @JsonProperty(value = "vehicle_type", defaultValue = "UNKNOWN")
    private APIRoutingEnums.VehicleType vehicleType = APIRoutingEnums.VehicleType.UNKNOWN;
    @JsonIgnore
    private boolean hasVehicleType = false;

    @ApiModelProperty(value = " Specifies additional routing parameters.")
    @JsonProperty("profile_params")
    private RequestProfileParams profileParams;
    @JsonIgnore
    private boolean hasProfileParams = false;

    @ApiModelProperty(name = "avoid_polygons", value = "Comprises areas to be avoided for the route. Formatted in GeoJSON as either a Polygon or Multipolygon object.")
    @JsonProperty("avoid_polygons")
    private JSONObject avoidPolygonFeatures;
    @JsonIgnore
    private boolean hasAvoidPolygonFeatures = false;

    public APIRoutingEnums.AvoidFeatures[] getAvoidFeatures() {
        return avoidFeatures;
    }

    public void setAvoidFeatures(APIRoutingEnums.AvoidFeatures[] avoidFeatures) {
        this.avoidFeatures = avoidFeatures;
        hasAvoidFeatures = true;
    }

    public double getMaximumSpeed() {
        return maximumSpeed;
    }

    public void setMaximumSpeed(double maximumSpeed) {
        this.maximumSpeed = maximumSpeed;
        hasMaximumSpeed = true;
    }

    public APIRoutingEnums.AvoidBorders getAvoidBorders() {
        return avoidBorders;
    }

    public void setAvoidBorders(APIRoutingEnums.AvoidBorders avoidBorders) {
        this.avoidBorders = avoidBorders;
        hasAvoidBorders = true;
    }

    public int[] getAvoidCountries() {
        return avoidCountries;
    }

    public void setAvoidCountries(int[] avoidCountries) {
        this.avoidCountries = avoidCountries;
        hasAvoidCountries = true;
    }

    public APIRoutingEnums.VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(APIRoutingEnums.VehicleType vehicleType) {
        this.vehicleType = vehicleType;
        hasVehicleType = true;
    }

    public RequestProfileParams getProfileParams() {
        return profileParams;
    }

    public void setProfileParams(RequestProfileParams profileParams) {
        this.profileParams = profileParams;
        hasProfileParams = true;
    }

    public JSONObject getAvoidPolygonFeatures() {
        return avoidPolygonFeatures;
    }

    public void setAvoidPolygonFeatures(JSONObject avoidPolygonFeatures) {
        this.avoidPolygonFeatures = avoidPolygonFeatures;
        hasAvoidPolygonFeatures = true;
    }

    public boolean hasAvoidFeatures() {
        return hasAvoidFeatures;
    }

    public boolean hasMaximumSpeed() {
        return hasMaximumSpeed;
    }

    public boolean hasAvoidBorders() {
        return hasAvoidBorders;
    }

    public boolean hasAvoidCountries() {
        return hasAvoidCountries;
    }

    public boolean hasVehicleType() {
        return hasVehicleType;
    }

    public boolean hasProfileParams() {
        return hasProfileParams;
    }

    public boolean hasAvoidPolygonFeatures() {
        return hasAvoidPolygonFeatures;
    }
}

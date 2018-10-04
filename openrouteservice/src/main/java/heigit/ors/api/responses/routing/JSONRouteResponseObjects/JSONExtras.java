package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.api.requests.routing.APIRoutingEnums;
import heigit.ors.common.DistanceUnit;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.routing.RouteExtraInfo;
import heigit.ors.routing.RouteResult;
import heigit.ors.util.DistanceUnitUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class JSONExtras {
    private JSONExtra waycategory;
    private JSONExtra surface;
    private JSONExtra steepness;
    private JSONExtra suitability;
    private JSONExtra waytype;
    private JSONExtra tollways;
    private JSONExtra traildifficulty;
    private JSONExtra osmid;

    private Map<String, JSONExtra> extras;

    @JsonIgnore
    private DistanceUnit units;
    @JsonIgnore
    private double routeLength;

    public JSONExtras(RouteResult result, APIRoutingEnums.Units units) throws StatusCodeException {
        this.routeLength = result.getSummary().getDistance();
        this.units = DistanceUnitUtil.getFromString(units.toString(), DistanceUnit.Unknown);

        List<RouteExtraInfo> extras = result.getExtraInfo();
        if(extras != null) {
            /*for (RouteExtraInfo extraInfo : extras) {
                addExtra(extraInfo);
            }*/
        }
    }

    private void addExtra(RouteExtraInfo extraInfo) throws StatusCodeException {
        try {
            Field extra = this.getClass().getDeclaredField(extraInfo.getName());
            extra.set(this, new JSONExtra(extraInfo.getSegments(), extraInfo.getSummary(units, routeLength, true)));
        } catch (NoSuchFieldException nfe) {

        } catch (IllegalAccessException iae) {

        }
        /*if(extraInfo.getName().equals("waycategory")) {
            waycategory = new JSONExtra(extraInfo.getSegments(), extraInfo.getSummary(units, routeLength, true));
        }
        if(extraInfo.getName().equals("surface")) {
            surface = new JSONExtra(extraInfo.getSegments(), extraInfo.getSummary(units, routeLength, true));
        }*/
    }

    @JsonProperty("waycategory")
    public JSONExtra getWayCategory() {
        return waycategory;
    }

    @JsonProperty("surface")
    public JSONExtra getSurface() {
        return surface;
    }
}

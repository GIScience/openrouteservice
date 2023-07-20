package org.heigit.ors.api.requests.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.routing.GenericErrorCodes;
import org.heigit.ors.api.requests.routing.RequestProfileParamsRestrictions;
import org.heigit.ors.api.requests.routing.RequestProfileParamsWeightings;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.config.AppConfig;
import org.heigit.ors.exceptions.*;
import org.heigit.ors.geojson.GeometryJSON;
import org.heigit.ors.routing.*;
import org.heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import org.heigit.ors.routing.graphhopper.extensions.VehicleLoadCharacteristicsFlags;
import org.heigit.ors.routing.graphhopper.extensions.WheelchairTypesEncoder;
import org.heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersReader;
import org.heigit.ors.routing.parameters.ProfileParameters;
import org.heigit.ors.routing.parameters.VehicleParameters;
import org.heigit.ors.routing.parameters.WheelchairParameters;
import org.heigit.ors.routing.pathprocessors.BordersExtractor;
import org.heigit.ors.util.DistanceUnitUtil;
import org.heigit.ors.util.GeomUtility;
import org.heigit.ors.util.StringUtility;
import org.json.simple.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class APIRequest {
    public static final String PARAM_ID = "id";
    public static final String PARAM_PROFILE = "profile";

    @Schema(name = PARAM_ID, description = "Arbitrary identification string of the request reflected in the meta information.",
            example = "my_request")
    @JsonProperty(PARAM_ID)
    protected String id;
    @JsonIgnore
    private boolean hasId = false;

    @Schema(name = PARAM_PROFILE, hidden = true)
    protected APIEnums.Profile profile;

    public boolean hasId() {
        return hasId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.hasId = true;
    }

    public APIEnums.Profile getProfile() {
        return profile;
    }

    public void setProfile(APIEnums.Profile profile) {
        this.profile = profile;
    }

}

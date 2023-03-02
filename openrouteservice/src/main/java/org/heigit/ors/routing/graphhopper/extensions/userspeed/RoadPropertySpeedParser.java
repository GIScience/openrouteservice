package org.heigit.ors.routing.graphhopper.extensions.userspeed;

import org.heigit.ors.exceptions.*;
import org.heigit.ors.routing.RoutingErrorCodes;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class RoadPropertySpeedParser {
    public RoadPropertySpeedMap parse(String input) throws StatusCodeException {
        return parse(new JSONObject(input));
    }

    public RoadPropertySpeedMap parse(org.json.simple.JSONObject input) throws StatusCodeException {
        return parse(input.toJSONString());
    }

    public RoadPropertySpeedMap parse(JSONObject json) throws StatusCodeException {
        RoadPropertySpeedMap rsm = new RoadPropertySpeedMap();

        // test that only valid keys are present:
        List<String> validKeys = Arrays.asList("unit", "roadSpeeds", "surfaceSpeeds");
        for (String key : json.keySet()) {
            if (!validKeys.contains(key)) {
                throw new UnknownParameterException(RoutingErrorCodes.UNKNOWN_PARAMETER, key);
            }
        }

        // parse units
        String unit = "kmh";
        if (json.has("unit")) {
            unit = json.getString("unit");
        }

        double unitFactor;
        if (unit.equals("kmh")) {
            unitFactor = 1.0;
        } else if (unit.equals("mph")) {
            unitFactor = 1.60934;
        } else {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "unit", unit);
        }

        // parse road speeds
        JSONObject roadSpeeds;
        if (json.has("roadSpeeds")) {
            roadSpeeds = json.getJSONObject("roadSpeeds");
            for (String roadType : roadSpeeds.keySet()) {
                try {
                    rsm.addRoadPropertySpeed(roadType, roadSpeeds.getDouble(roadType) * unitFactor);
                } catch (IllegalArgumentException e) {
                    if (e.getMessage().contains("must be")) {
                        throw new ParameterOutOfRangeException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "speed");
                    } else {
                        throw new UnknownParameterException(RoutingErrorCodes.UNKNOWN_PARAMETER, e.getMessage().substring(23));
                    }
                }
            }
        }

        // parse surface speeds
        JSONObject surfaceSpeeds;
        if (json.has("surfaceSpeeds")) {
           surfaceSpeeds = json.getJSONObject("surfaceSpeeds");
            for (String surfaceType : surfaceSpeeds.keySet()) {
                try {
                    rsm.addRoadPropertySpeed(surfaceType, surfaceSpeeds.getDouble(surfaceType) * unitFactor);
                } catch (IllegalArgumentException e) {
                    if (e.getMessage().contains("must be")) {
                        throw new ParameterOutOfRangeException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "speed");
                    } else {
                        throw new UnknownParameterException(RoutingErrorCodes.UNKNOWN_PARAMETER, e.getMessage().substring(23));
                    }
                }
            }
        }

        return rsm;
    }
}

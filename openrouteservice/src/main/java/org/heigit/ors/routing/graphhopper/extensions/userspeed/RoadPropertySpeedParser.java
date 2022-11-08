package org.heigit.ors.routing.graphhopper.extensions.userspeed;

import org.json.JSONObject;

public class RoadPropertySpeedParser {
    public RoadPropertySpeedMap parse(String input) {
        return parse(new JSONObject(input));
    }

    public RoadPropertySpeedMap parse(org.json.simple.JSONObject input) {
        return parse(input.toJSONString());
    }

    public RoadPropertySpeedMap parse(JSONObject json) {
        RoadPropertySpeedMap rsm = new RoadPropertySpeedMap();

        // parse units
        String unit = "kmh";
        if (json.has("unit")) {
            unit = json.getString("unit");
        }
        double unitFactor = 1.0;
        if (unit.equals("mph")) {
            unitFactor = 1.60934;
        }

        // parse road speeds
        JSONObject roadSpeeds;
        if (json.has("roadSpeeds")) {
            roadSpeeds = json.getJSONObject("roadSpeeds");
            for (String roadType : roadSpeeds.keySet()) {
                rsm.addRoadPropertySpeed(roadType, roadSpeeds.getDouble(roadType)*unitFactor);
            }
        }

        // parse surface speeds
        JSONObject surfaceSpeeds;
        if (json.has("surfaceSpeeds")) {
           surfaceSpeeds = json.getJSONObject("surfaceSpeeds");
            for (String surfaceType : surfaceSpeeds.keySet()) {
                rsm.addRoadPropertySpeed(surfaceType, surfaceSpeeds.getDouble(surfaceType)*unitFactor);
            }
        }

        return rsm;
    }
}

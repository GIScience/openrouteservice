package org.heigit.ors.apitests.utils;

import org.json.JSONArray;

public class HelperFunctions {
    /**
     * This function creates a {@link JSONArray} with fake coordinates.
     * The size depends on maximumSize.
     *
     * @param maximumSize number of maximum coordinates in the {@link JSONArray}
     * @return {@link JSONArray}
     */
    public static JSONArray fakeJSONLocations(int maximumSize) {
        JSONArray overloadedLocations = new JSONArray();
        for (int i = 0; i < maximumSize; i++) {
            JSONArray location = new JSONArray();
            location.put(0.0);
            location.put(0.0);
            overloadedLocations.put(location);
        }
        return overloadedLocations;
    }

    /**
     * This function creates a {@link JSONArray} with coordinates.
     *
     * @param coordString coordinates string
     * @return {@link JSONArray}
     */
    public static JSONArray constructCoords(String coordString) {
        JSONArray coordinates = new JSONArray();
        String[] coordPairs = coordString.split("\\|");
        for (String pair : coordPairs) {
            JSONArray coord = new JSONArray();
            String[] pairCoords = pair.split(",");
            coord.put(Double.parseDouble(pairCoords[0]));
            coord.put(Double.parseDouble(pairCoords[1]));
            coordinates.put(coord);
        }

        return coordinates;
    }
}

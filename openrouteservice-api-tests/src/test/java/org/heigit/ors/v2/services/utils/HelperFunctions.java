package heigit.ors.v2.services.utils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

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
     * This function creates a {@link JSONArray} with fake coordinates.
     * The size depends on maximumSize.
     *
     * @param maximumSize number of maximum coordinates in the {@link JSONArray}
     * @return {@link JSONArray}
     */
    public List<List<Double>> fakeListLocations(int maximumSize) {
        List<List<Double>> listOfBareCoordinatesList = new ArrayList<>();
        for (int i = 0; i < maximumSize; i++) {
            List<Double> bareCoordinatesList = new ArrayList<>();
            bareCoordinatesList.add(8.681495);
            bareCoordinatesList.add(49.41461);
            listOfBareCoordinatesList.add(bareCoordinatesList);
        }
        return listOfBareCoordinatesList;
    }
}

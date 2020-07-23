package org.heigit.ors.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;

public class HelperFunctions {
    /**
     * This function creates a {@link List<List<Double>>} with fake coordinates.
     * The size depends on maximumSize.
     *
     * @param maximumSize number of maximum coordinates in the {@link  List<List<Double>>}
     * @param coordDimension defines the size of the coords 1. only x; 2. x,y; 3. x,y,z
     * @return {@link List<List<Double>>}
     */
    public static List<List<Double>> fakeListLocations(int maximumSize, int coordDimension) {
        List<List<Double>> listOfBareCoordinatesList = new ArrayList<>();
        if (coordDimension < 1 || coordDimension > 3) {
            return listOfBareCoordinatesList;
        }
        for (int i = 0; i < maximumSize; i++) {
            List<Double> bareCoordinatesList = new ArrayList<>();
            switch (coordDimension) {
                case 1:
                    bareCoordinatesList.add(8.681495);
                    break;
                case 2:
                    bareCoordinatesList.add(8.681495);
                    bareCoordinatesList.add(49.41461);
                    break;
                case 3:
                    bareCoordinatesList.add(8.681495);
                    bareCoordinatesList.add(49.41461);
                    bareCoordinatesList.add(123.0);
                    break;
            }
            listOfBareCoordinatesList.add(bareCoordinatesList);
        }
        return listOfBareCoordinatesList;
    }

    /**
     * This function creates a {@link ArrayList} with fake coordinates.
     * The size depends on maximumSize.
     *
     * @param maximumSize    number of maximum coordinates in the {@link ArrayList}
     * @param coordDimension defines the size of the coords 1. only x; 2. x,y; 3. x,y,z
     * @return {@link ArrayList}
     */
    public static Double[][] fakeArrayLocations(int maximumSize, int coordDimension) {
        Double[][] arrayOfDoubleLocationsArray = new Double[maximumSize][];
        if (coordDimension < 1 || coordDimension > 3) {
            return arrayOfDoubleLocationsArray;
        }
        for (int i = 0; i < maximumSize; i++) {
            Double[] doubleLocationsArray;
            switch (coordDimension) {
                case 1:
                    doubleLocationsArray = new Double[1];
                    doubleLocationsArray[0] = 8.681495;
                    arrayOfDoubleLocationsArray[i] = doubleLocationsArray;
                    break;
                case 2:
                    doubleLocationsArray = new Double[2];
                    doubleLocationsArray[0] = 8.681495;
                    doubleLocationsArray[1] = 49.41461;
                    arrayOfDoubleLocationsArray[i] = doubleLocationsArray;
                    break;
                case 3:
                    doubleLocationsArray = new Double[3];
                    doubleLocationsArray[0] = 8.681495;
                    doubleLocationsArray[1] = 49.41461;
                    doubleLocationsArray[2] = 123.0;
                    arrayOfDoubleLocationsArray[i] = doubleLocationsArray;
                    break;
            }
        }
        return arrayOfDoubleLocationsArray;
    }

    /**
     * This function creates a {@link JSONArray} with fake coordinates.
     * The size depends on maximumSize.
     *
     * @param maximumSize number of maximum coordinates in the {@link JSONArray}
     * @param coordDimension defines the size of the coords 1. only x; 2. x,y; 3. x,y,z
     * @return {@link JSONArray}
     */
    public static JSONArray fakeJSONLocations(int maximumSize, int coordDimension) {
        JSONArray overloadedLocations = new JSONArray();
        if (coordDimension < 1 || coordDimension > 3) {
            return overloadedLocations;
        }
        for (int i = 0; i < maximumSize; i++) {
            JSONArray location;
            switch (coordDimension) {
                case 1:
                    location = new JSONArray();
                    location.put(0.0);
                    overloadedLocations.put(location);
                    break;
                case 2:
                    location = new JSONArray();
                    location.put(0.0);
                    location.put(0.0);
                    overloadedLocations.put(location);
                    break;
                case 3:
                    location = new JSONArray();
                    location.put(0.0);
                    location.put(0.0);
                    location.put(0.0);
                    overloadedLocations.put(location);
                    break;
            }
        }
        return overloadedLocations;
    }

    public static Coordinate[] convertCoordinateArray(double[][] input) {
        return Arrays.stream(input)
            .map(coords -> new Coordinate(coords[0], coords[1]))
            .toArray(Coordinate[]::new);
    }

    public static CoordinateSequence convertCoordinateArrayToSequence(double[][] input) {
        return CoordinateArraySequenceFactory.instance().create(convertCoordinateArray(input));
    }
}

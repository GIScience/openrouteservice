package org.heigit.ors.routing.graphhopper.extensions.flagencoders;

public class FlagEncoderNames {
    private static final String ORS_SUFFIX = "_ors";

    public static final String UNKNOWN = "UNKNOWN";

    public static final String CAR_ORS = "car" + ORS_SUFFIX;
    public static final String HEAVYVEHICLE = "heavyvehicle";
    public static final String EMERGENCY = "emergency";
    public static final String EVEHICLE = "evehicle";       // NOT IMPLEMENTED
    public static final String RUNNING = "running";        // NOT IMPLEMENTED

    public static final String WHEELCHAIR = "wheelchair";

    public static final String BIKE_ORS = "bike" + ORS_SUFFIX;
    public static final String ROADBIKE_ORS = "roadbike" + ORS_SUFFIX;
    public static final String MTB_ORS = "mtb" + ORS_SUFFIX;
    public static final String BIKE_ELECTRO = "electrobike";
    public static final String BIKE_CARGO = "cargobike";

    public static final String PEDESTRIAN_ORS = "pedestrian" + ORS_SUFFIX;
    public static final String HIKING_ORS = "hiking" + ORS_SUFFIX;


    public static final String GH_CAR = "car";
    public static final String GH_CAR4WD = "car4wd";
    public static final String GH_MOTOCYCLE = "motorcycle";
    public static final String GH_FOOT = "foot";
    public static final String GH_HIKE = "hike";
    public static final String GH_RACINGBIKE = "racingbike";
    public static final String GH_MTB = "mtb";
    public static final String GH_BIKE = "bike";
    public static final String GH_BIKE2 = "bike2";

    private FlagEncoderNames() {
    }

    public static String getBaseName(String name) {
        if (name.endsWith(ORS_SUFFIX))
            name = name.substring(0, name.indexOf(ORS_SUFFIX));

        return name;
    }
}

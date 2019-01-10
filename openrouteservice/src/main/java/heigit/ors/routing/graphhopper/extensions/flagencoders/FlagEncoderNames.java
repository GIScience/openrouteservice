package heigit.ors.routing.graphhopper.extensions.flagencoders;

public interface FlagEncoderNames {

    public static final String CAR_ORS          = "car-ors";
    public static final String HEAVYVEHICLE     = "heavyvehicle";
    public static final String CAROFFROAD       = "caroffroad";     // NOT IN USE!
    // MARQ24: cartmc is not ready for production
    public static final String CARTMC           = "cartmc";         // not ready for production
    public static final String EMERGENCY        = "emergency";
    // MARQ24 NO eVEHICLE FlagEncoder implemented yet!!!
    public static final String EVEHICLE         = "evehicle";       // NOT IN USE!
    public static final String RUNNING          = "running";        // NOT IN USE!

    public static final String WHEELCHAIR       = "wheelchair";

    public static final String BIKE_ORS         = "bike-ors";
    public static final String ROADBIKE_ORS     = "roadbike-ors";
    public static final String MTB_ORS          = "mtb-ors";
    public static final String BIKE_ELECTRO     = "electrobike";


    public static final String GH_CAR           = "car";
    public static final String GH_CAR4WD        = "car4wd";
    public static final String GH_MOTOCYCLE     = "motorcycle";
    public static final String GH_FOOT          = "foot";
    public static final String GH_HIKE          = "hike";
    public static final String GH_RACINGBIKE    = "racingbike";
    public static final String GH_MTB           = "mtb";
    public static final String GH_BIKE          = "bike";
    public static final String GH_BIKE2         = "bike2";
    public static final String GH_GENERIC       = "generic";
    public static final String GH_PT            = "pt";
}

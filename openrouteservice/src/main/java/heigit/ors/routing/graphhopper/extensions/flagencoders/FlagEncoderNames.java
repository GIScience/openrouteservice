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

    // MARQ24 please note there is no "foot" profile present simply cause we will make
    // use of the default foot profile of GH
    @Deprecated // make use og original GH "hike" instead !!!
    public static final String WALKING          = "walking";        // DEPRECATED - DO NOT USE
    @Deprecated // make use og original GH "hike" instead !!!
    public static final String HIKING           = "hiking";         // DEPRECATED - DO NOT USE
    public static final String RUNNING          = "running";        // NOT IN USE!

    public static final String WHEELCHAIR       = "wheelchair";

    public static final String BIKE_ORS         = "bike-ors";
    public static final String ROADBIKE_ORS     = "roadbike-ors";
    public static final String MTB_ORS          = "mtb-ors";
    public static final String BIKE_ELECTRO     = "electrobike";

    // MARQ24: just here to be able to compare old encoders with NextGen (NG) impl
    // MARQ24 - please note this is the  depricated "road bike" profile of ors which was based on gh 0.9.x -> I have no
    // clue where this 'RACING' is comming from - for sure englich is not my mother thong - but this is a false
    // translation for a very long time - probably comming even from gpsies.com where I have seen 'racebike' for the
    // first time - somewhere around 2009... [so ors-'original' is called racingbike - the new one have noe the
    // propper name roadbike!]
    @Deprecated
    public static final String RACINGBIKE_ORS   = "racingbike-ors";
    //@Deprecated - IS CURRENTLY STILL THE DEFAULT - since the result of nextGen and the ors-original is too different
    public static final String BIKE_ORS_OLD     = "bike-oold";
    @Deprecated
    public static final String MTB_ORS_OLD      = "mtb-oold";

    // MARQ24 depricated bike profiles which we are not going to use anylonger (since we want to save
    // memory [in previous 0.9.x adjustments of the ors gh fork there was a cool feature where you could
    // combine multiple vehicles in a single graph - this feature was hard to maintain over the time
    // with all the original gh changes so we (timothy, alan & marq24) have decided to ged rid of this
    // feature...
    @Deprecated
    public static final String BIKE_SAFTY       = "safetybike";
    @Deprecated
    public static final String BIKE_TOUR        = "cycletourbike";


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

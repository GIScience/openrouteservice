package org.heigit.ors.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;

public enum EncoderNameEnum {
    DEFAULT("default", 0),

    // DRIVING STUFF
    DRIVING_CAR("driving-car", 1),
    DRIVING_HGV("driving-hgv", 2),
    @JsonIgnore
    DRIVING_EMERGENCY("driving-emergency", 3), // not supported
    @JsonIgnore
    DRIVING_CAROFFROAD("driving-caroffroad", 4), // not supported
    @JsonIgnore
    DRIVING_SEGWAY("driving-segway", 5), // not implemented
    @JsonIgnore
    DRIVING_ELECTRIC_CAR("driving-ecar", 6),
    @JsonIgnore
    DRIVING_MOTORCYCLE("driving-motorcycle", 7),
    @JsonIgnore
    DRIVING_TRAFFIC("driving-traffic", 8),

    // CYCLING STUFF
    CYCLING_REGULAR("cycling-regular", 10),
    CYCLING_MOUNTAIN("cycling-mountain", 11),
    CYCLING_ROAD("cycling-road", 12),
    CYCLING_ELECTRIC("cycling-electric", 17),

    // WALKING STUFF
    FOOT_WALKING("foot-walking", 20),
    FOOT_HIKING("foot-hiking", 21),
    @JsonIgnore
    FOOT_JOGGING("foot-jogging", 24),

    // OTHER STUFF
    WHEELCHAIR("wheelchair", 30),
    PUBLIC_TRANSPORT("public-transport", 31),

    // GH default FlagEncoders...
    GH_CAR("gh-car", 40),
    GH_CAR4WD("gh-car4wd", 41),
    GH_BIKE("gh-bike", 42),
    GH_BIKE2("gh-bike2", 43),
    GH_BIKE_MTB("gh-mtb", 44),
    GH_BIKE_ROAD("gh-racingbike", 45),
    GH_FOOT("gh-foot", 46),
    GH_HIKE("gh-hike", 47);

    public final String name;
    @Getter
    private final int value;

    EncoderNameEnum(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public static boolean isDriving(int routePref) {
        return routePref == DRIVING_CAR.getValue()
                || routePref == DRIVING_HGV.getValue()
                || routePref == DRIVING_ELECTRIC_CAR.getValue()
                || routePref == DRIVING_EMERGENCY.getValue()
                || routePref == DRIVING_MOTORCYCLE.getValue()
                || routePref == DRIVING_CAROFFROAD.getValue()
                || routePref == DRIVING_TRAFFIC.getValue()
                || routePref == GH_CAR.getValue()
                || routePref == GH_CAR4WD.getValue();
    }

    public static boolean isHeavyVehicle(int routePref) {
        return routePref == DRIVING_HGV.getValue()
                || routePref == DRIVING_CAROFFROAD.getValue()
                || routePref == DRIVING_EMERGENCY.getValue();
    }

    public static boolean isWalking(int routePref) {
        return routePref == FOOT_WALKING.getValue()
                || routePref == FOOT_HIKING.getValue()
                || routePref == FOOT_JOGGING.getValue()
                || routePref == GH_FOOT.getValue()
                || routePref == GH_HIKE.getValue();
    }

    public static boolean isPedestrian(int routePref) {
        return isWalking(routePref) || routePref == WHEELCHAIR.getValue();
    }

    public static boolean isWheelchair(int routePref) {
        return routePref == WHEELCHAIR.getValue();
    }

    public static boolean isCycling(int routePref) {
        return routePref == CYCLING_REGULAR.getValue()
                || routePref == CYCLING_MOUNTAIN.getValue()
                || routePref == CYCLING_ROAD.getValue()
                || routePref == CYCLING_ELECTRIC.getValue()
                || routePref == GH_BIKE.getValue()
                || routePref == GH_BIKE2.getValue()
                || routePref == GH_BIKE_MTB.getValue()
                || routePref == GH_BIKE_ROAD.getValue();
    }

    public static boolean supportMessages(int profileType) {
        return isDriving(profileType);
    }

    public static String getName(int profileType) {
        for (EncoderNameEnum type : values()) {
            if (type.getValue() == profileType) {
                return type.getName();
            }
        }
        return DEFAULT.getName();
    }

    public static int getFromString(String profileType) {
        for (EncoderNameEnum type : values()) {
            if (type.getName().equalsIgnoreCase(profileType)) {
                return type.getValue();
            }
        }
        return DEFAULT.getValue();
    }

    public static String getEncoderName(int routePref) {
        return switch (routePref) {
            case 1 -> FlagEncoderNames.CAR_ORS;
            case 2 -> FlagEncoderNames.HEAVYVEHICLE;
            case 3 -> FlagEncoderNames.EMERGENCY;
            case 7 -> FlagEncoderNames.GH_MOTOCYCLE;
            case 6 -> FlagEncoderNames.EVEHICLE;
            case 24 -> FlagEncoderNames.RUNNING;
            case 10 -> FlagEncoderNames.BIKE_ORS;
            case 11 -> FlagEncoderNames.MTB_ORS;
            case 12 -> FlagEncoderNames.ROADBIKE_ORS;
            case 20 -> FlagEncoderNames.PEDESTRIAN_ORS;
            case 21 -> FlagEncoderNames.HIKING_ORS;
            case 30 -> FlagEncoderNames.WHEELCHAIR;
            case 31, 46 -> FlagEncoderNames.GH_FOOT;
            case 40 -> FlagEncoderNames.GH_CAR;
            case 41 -> FlagEncoderNames.GH_CAR4WD;
            case 42 -> FlagEncoderNames.GH_BIKE;
            case 43 -> FlagEncoderNames.GH_BIKE2;
            case 44 -> FlagEncoderNames.GH_MTB;
            case 45 -> FlagEncoderNames.GH_RACINGBIKE;
            case 47 -> FlagEncoderNames.GH_HIKE;
            case 17 -> FlagEncoderNames.BIKE_ELECTRO;
            default -> FlagEncoderNames.UNKNOWN;
        };
    }

    public static int getFromEncoderName(String encoder) {
        return switch (encoder) {
            case FlagEncoderNames.CAR_ORS -> DRIVING_CAR.getValue();
            case FlagEncoderNames.HEAVYVEHICLE -> DRIVING_HGV.getValue();
            case FlagEncoderNames.EMERGENCY -> DRIVING_EMERGENCY.getValue();
            case FlagEncoderNames.GH_MOTOCYCLE -> DRIVING_MOTORCYCLE.getValue();
            case FlagEncoderNames.EVEHICLE -> DRIVING_ELECTRIC_CAR.getValue();
            case FlagEncoderNames.RUNNING -> FOOT_JOGGING.getValue();
            case FlagEncoderNames.BIKE_ORS -> CYCLING_REGULAR.getValue();
            case FlagEncoderNames.MTB_ORS -> CYCLING_MOUNTAIN.getValue();
            case FlagEncoderNames.ROADBIKE_ORS -> CYCLING_ROAD.getValue();
            case FlagEncoderNames.PEDESTRIAN_ORS, FlagEncoderNames.GH_FOOT -> FOOT_WALKING.getValue();
            case FlagEncoderNames.HIKING_ORS, FlagEncoderNames.GH_HIKE -> FOOT_HIKING.getValue();
            case FlagEncoderNames.WHEELCHAIR -> WHEELCHAIR.getValue();
            case FlagEncoderNames.GH_CAR -> GH_CAR.getValue();
            case FlagEncoderNames.GH_CAR4WD -> GH_CAR4WD.getValue();
            case FlagEncoderNames.GH_BIKE -> GH_BIKE.getValue();
            case FlagEncoderNames.GH_BIKE2 -> GH_BIKE2.getValue();
            case FlagEncoderNames.GH_MTB -> GH_BIKE_MTB.getValue();
            case FlagEncoderNames.GH_RACINGBIKE -> GH_BIKE_ROAD.getValue();
            case FlagEncoderNames.BIKE_ELECTRO -> CYCLING_ELECTRIC.getValue();
            default -> DEFAULT.getValue();
        };
    }

    @Override
    public String toString() {
        return name;
    }

    @JsonValue
    public String getName() {
        return name;
    }
}

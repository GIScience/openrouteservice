package org.heigit.ors.routing.graphhopper.extensions.reader.traffic;

public class TrafficEnums {
    public enum PatternResolution {
        MINUTES_15(15);

        private final int value;

        PatternResolution(int resolution) {
            this.value = resolution;
        }
    }

    public enum FerryType {
        NO('H'),
        BOAT('B'),
        RAIL('R');

        private final char ferryType;

        FerryType(char value) {
            this.ferryType = value;
        }

        public static FerryType forValue(char v) {
            for (FerryType enumItem : FerryType.values()) {
                if (enumItem.ferryType == v)
                    return enumItem;
            }
            return null;
        }


        public char getFerryType() {
            return ferryType;
        }
    }

    public enum NoYesEnum {
        YES('Y'),
        NO('N');

        private final char noYesEnum;

        NoYesEnum(char value) {
            this.noYesEnum = value;
        }

        public static NoYesEnum forValue(char v) {
            for (NoYesEnum enumItem : NoYesEnum.values()) {
                if (enumItem.noYesEnum == v)
                    return enumItem;
            }
            return null;
        }


        public char getNoYesEnum() {
            return noYesEnum;
        }
    }

    /**
     *
     */
    public enum FunctionalClass {
        CLASS1(1),
        CLASS2(2),
        CLASS3(3),
        CLASS4(4),
        CLASS5(5);

        private final int functionalClass;

        FunctionalClass(int value) {
            this.functionalClass = value;
        }

        public static FunctionalClass forValue(int v) {
            for (FunctionalClass enumItem : FunctionalClass.values()) {
                if (enumItem.functionalClass == v)
                    return enumItem;
            }
            return null;
        }


        public int getFunctionalClass() {
            return functionalClass;
        }
    }

    /**
     * Describes the travel direction of the link (road segment).
     * - or T is the direction from the From Node
     * + or F is the direction towards the From Node
     */
    public enum LinkTravelDirection {
        TO("T"),
        FROM("F"),
        BOTH("B");

        private final String direction;

        LinkTravelDirection(String value) {
            this.direction = value;
        }

        public static LinkTravelDirection forValue(String v) {
            for (LinkTravelDirection enumItem : LinkTravelDirection.values()) {
                if (enumItem.direction.equals(v.trim()))
                    return enumItem;
            }
            return null;
        }

        public String getDirection() {
            return direction;
        }
    }

    /**
     * Describes the travel direction of the link (road segment).
     * - or T is the direction from the From Node
     * + or F is the direction towards the From Node
     */
    public enum TravelDirection {
        TO("T", '-'),
        FROM("F", '+');

        private final String direction;
        private final char abbreviation;

        TravelDirection(String direction, char abbreviation) {
            this.direction = direction;
            this.abbreviation = abbreviation;
        }

        public static TravelDirection forValue(String v) {
            for (TravelDirection enumItem : TravelDirection.values()) {
                if (enumItem.direction.equals(v.trim()))
                    return enumItem;
            }
            return null;
        }

        public static TravelDirection forValue(char v) {
            for (TravelDirection enumItem : TravelDirection.values()) {
                if (enumItem.abbreviation == v)
                    return enumItem;
            }
            return null;
        }


        public String getDirection() {
            return direction;
        }

        public char getAbbreviation() {
            return abbreviation;
        }
    }

    /**
     * The Radio Data System direction
     * + is in the positive direction and external to the Problem Location
     * - is in the negative direction and external to the Problem Location
     * P is in the positive direction and internal to the Problem Location
     * N is in the negative direction and internal to the Problem Location
     */
    public enum RDSDirection {
        PE('+'),
        NE('-'),
        PI('P'),
        NI('N');

        private final char value;

        RDSDirection(char rdsDirection) {
            this.value = rdsDirection;
        }

        public static RDSDirection forValue(char v) {
            for (RDSDirection enumItem : RDSDirection.values()) {
                if (enumItem.value == v)
                    return enumItem;
            }
            return null;
        }

        public char getValue() {
            return this.value;
        }
    }

    public enum Country {
        GERMANY('D');

        private final char value;

        Country(char country) {
            this.value = country;
        }

        public static Country forValue(char v) {
            for (Country enumItem : Country.values()) {
                if (enumItem.value == v)
                    return enumItem;
            }
            return null;
        }
    }

    public enum WeekDay {
        MONDAY("M", 0),
        TUESDAY("T", 2),
        WEDNESDAY("W", 4),
        THURSDAY("R", 6),
        FRIDAY("F", 8),
        SATURDAY("S", 10),
        SUNDAY("U", 12);

        private final String value;
        private final int canonical;

        WeekDay(String weekday, int canonical) {
            this.value = weekday;
            this.canonical = canonical;
        }

        public String getValue() {
            return value;
        }

        public int getCanonical(){
            return canonical;
        }
    }
}

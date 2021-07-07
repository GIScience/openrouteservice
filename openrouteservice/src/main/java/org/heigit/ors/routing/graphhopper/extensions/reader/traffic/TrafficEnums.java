package org.heigit.ors.routing.graphhopper.extensions.reader.traffic;

public class TrafficEnums {
    public enum PatternResolution {
        MINUTES_15(15);

        private final int value;

        PatternResolution(int resolution) {
            this.value = resolution;
        }

        public int getValue() {
            return value;
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

        public String getDirection() {
            return direction;
        }

    }

    public enum WeekDay {
        SUNDAY("U", 12, 1),
        MONDAY("M", 0, 2),
        TUESDAY("T", 2, 3),
        WEDNESDAY("W", 4, 4),
        THURSDAY("R", 6, 5),
        FRIDAY("F", 8, 6),
        SATURDAY("S", 10, 7);

        private final String value;
        private final int byteLocation;
        private final int canonical;

        WeekDay(String weekday, int byteLocation, int canonical) {
            this.value = weekday;
            this.byteLocation = byteLocation;
            this.canonical = canonical;
        }

        /**
         * Get the correct Weekday by using 1 (Monday) -7 (Sunday)
         *
         * @param weekDay 1 (Monday) -7 (Sunday)
         * @return Correct Weekday enum
         */
        public static WeekDay valueOfCanonical(int weekDay) {
            for (WeekDay enumItem : WeekDay.values()) {
                if (enumItem.canonical == weekDay)
                    return enumItem;
            }
            return null;
        }

        public String getValue() {
            return value;
        }

        public int getByteLocation() {
            return byteLocation;
        }
    }
}

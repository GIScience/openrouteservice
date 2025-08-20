package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.WayType;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;

import java.util.List;

import static org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors.OSMAttachedSidewalkProcessor.KEY_ORS_SIDEWALK_SIDE;


public class WayTypeParser implements TagParser {
    private final EnumEncodedValue<WayType> wayTypeEnc;

    public static final String TAG_HIGHWAY = "highway";
    public static final String TAG_ROUTE = "route";

    public WayTypeParser() {
        this(new EnumEncodedValue<>(WayType.KEY, WayType.class));
    }

    public WayTypeParser(EnumEncodedValue<WayType> wayTypeEnc) {
        this.wayTypeEnc = wayTypeEnc;
    }

    @Override
    public void createEncodedValues(EncodedValueLookup encodedValueLookup, List<EncodedValue> list) {
        list.add(wayTypeEnc);
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay readerWay, boolean b, IntsRef relationFlags) {
        WayType wayType = getWayType(readerWay);
        wayTypeEnc.setEnum(false, edgeFlags, wayType);
        return edgeFlags;
    }

    private WayType getWayType(ReaderWay readerWay) {
        if (readerWay.hasTag(TAG_ROUTE, "ferry", "shuttle_train")) {
            return WayType.FERRY;
        }
        if (readerWay.hasTag(KEY_ORS_SIDEWALK_SIDE)) {
            return WayType.FOOTWAY;
        }
        return WayTypeParser.getFromString(readerWay.getTag(TAG_HIGHWAY, ""));
    }

    public static WayType getFromString(String highway) {
        return switch (highway.toLowerCase()) {
            case "primary", "primary_link", "motorway", "motorway_link", "trunk", "trunk_link" -> WayType.STATE_ROAD;
            case "secondary", "secondary_link", "tertiary", "tertiary_link", "road", "unclassified" -> WayType.ROAD;
            case "residential", "service", "living_street" -> WayType.STREET;
            case "path" -> WayType.PATH;
            case "track" -> WayType.TRACK;
            case "cycleway" -> WayType.CYCLEWAY;
            case "footway", "pedestrian", "crossing" -> WayType.FOOTWAY;
            case "steps" -> WayType.STEPS;
            case "construction" -> WayType.CONSTRUCTION;
            default -> WayType.UNKNOWN;
        };
    }
}

package org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;

import java.util.HashSet;
import java.util.Set;

public class OSMAttachedSidewalkProcessor {

    public static final String KEY_ORS_SIDEWALK_SIDE = "ors-sidewalk-side";
    public static final String VAL_RIGHT = "right";
    public static final String VAL_LEFT = "left";

    protected enum Side { LEFT, RIGHT, BOTH, NONE }

    public boolean hasSidewalkInfo(ReaderWay way) {
        return identifySidesWhereSidewalkIsPresent(way) != Side.NONE;
    }

    /**
     * Get the keys that represent sidewalk information
     *
     * @param way
     * @return
     */
    protected Set<String> getSidewalkKeys(ReaderWay way) {
        Set<String> sidewalkInfoKeys = new HashSet<>();

        Set<String> keys = way.getTags().keySet();
        for(String k : keys) {
            if(isSidewalkInfoKey(k)) {
                sidewalkInfoKeys.add(k);
            }
        }

        return sidewalkInfoKeys;
    }

    /**
     * Identify if the specified key could contain sidewalk information
     *
     * @param osmTagKey
     * @return
     */
    private boolean isSidewalkInfoKey(String osmTagKey) {
        return osmTagKey.startsWith("sidewalk:") || osmTagKey.startsWith("footway:");
    }

    /**
     * Add a tag to the way which signifies which side of the road the sidewalk needs to be processed for
     *
     * @param way
     * @param side
     * @return
     */
    public ReaderWay attachSidewalkTag(ReaderWay way, Side side) {
        switch(side) {
            case LEFT:
                way.setTag(KEY_ORS_SIDEWALK_SIDE, VAL_LEFT);
                break;
            case RIGHT:
                way.setTag(KEY_ORS_SIDEWALK_SIDE, VAL_RIGHT);
                break;
            case BOTH:
                if(way.hasTag(KEY_ORS_SIDEWALK_SIDE) && way.getTag(KEY_ORS_SIDEWALK_SIDE).equalsIgnoreCase(VAL_LEFT)) {
                    // The left side has been attached previously, so now attach the right side
                    way.setTag(KEY_ORS_SIDEWALK_SIDE, VAL_RIGHT);
                } else {
                    // start with the left side
                    way.setTag(KEY_ORS_SIDEWALK_SIDE, VAL_LEFT);
                }
                break;
            case NONE:
                if(way.hasTag(KEY_ORS_SIDEWALK_SIDE)) {
                    way.removeTag(KEY_ORS_SIDEWALK_SIDE);
                }
        }

        return way;
    }

    /**
     * Determine which sidewalk side is to be processed based on the tag that was attached.
     *
     * @param way
     * @return
     */
    public Side getPreparedSide(ReaderWay way) {
        if(way.hasTag(KEY_ORS_SIDEWALK_SIDE)) {
            String preparedSide = way.getTag(KEY_ORS_SIDEWALK_SIDE);
            if(preparedSide.equalsIgnoreCase(VAL_LEFT)) {
                return Side.LEFT;
            }
            if(preparedSide.equalsIgnoreCase(VAL_RIGHT)) {
                return Side.RIGHT;
            }
        }

        return Side.NONE;
    }

    /**
     * Identify which side there is a sidealk present on the road based on the tags assigned in OSM
     *
     * @param osmWay
     * @return
     */
    protected Side identifySidesWhereSidewalkIsPresent(ReaderWay osmWay) {
        boolean sidewalkOnLeftSide = false;
        boolean sidewalkOnRightSide = false;
        boolean sidewalkOnBothSides = false;

        if(osmWay.hasTag("sidewalk")) {
            String side = osmWay.getTag("sidewalk");
            switch(side) {
                case VAL_LEFT:
                    sidewalkOnLeftSide = true;
                    break;
                case VAL_RIGHT:
                    sidewalkOnRightSide = true;
                    break;
                case "both":
                    sidewalkOnBothSides = true;
                    break;
                default:
            }
        }

        Set<String> sidewalkProperties = getSidewalkKeys(osmWay);

        for(String key : sidewalkProperties) {
            if(key.startsWith("sidewalk:left") || key.startsWith("footway:left")) sidewalkOnLeftSide = true;
            if(key.startsWith("sidewalk:right") || key.startsWith("footway:right")) sidewalkOnRightSide = true;
            if(key.startsWith("sidewalk:both") || key.startsWith("footway:both")) sidewalkOnBothSides = true;
        }

        if(sidewalkOnLeftSide && sidewalkOnRightSide) {
            sidewalkOnBothSides = true;
        }

        if(sidewalkOnBothSides) {
            return Side.BOTH;
        }

        if(sidewalkOnLeftSide) {
            return Side.LEFT;
        }

        if(sidewalkOnRightSide) {
            return Side.RIGHT;
        }

        return Side.NONE;
    }
}

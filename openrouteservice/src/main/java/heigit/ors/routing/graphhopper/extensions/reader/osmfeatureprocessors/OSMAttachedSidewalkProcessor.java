package heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;

import java.util.HashSet;
import java.util.Set;

public class OSMAttachedSidewalkProcessor {
    protected enum Side { LEFT, RIGHT, BOTH, NONE }

    protected boolean hasSidewalkInfo(ReaderWay way) {
        return identifySidesWhereSidewalkIsPresent(way) != Side.NONE;
    }

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

    private boolean isSidewalkInfoKey(String osmTagKey) {
        if(osmTagKey.startsWith("sidewalk:") || osmTagKey.startsWith("footway:")) {
            return true;
        }

        return false;
    }



    public ReaderWay attachSidewalkTag(ReaderWay way, Side side) {
        switch(side) {
            case LEFT:
                way.setTag("ors-sidewalk-side", "left");
                break;
            case RIGHT:
                way.setTag("ors-sidewalk-side", "right");
                break;
            case BOTH:
                if(way.hasTag("ors-sidewalk-side") && way.getTag("ors-sidewalk-side").equalsIgnoreCase("left")) {
                    // The left side has been attached previously, so now attach the right side
                    way.setTag("ors-sidewalk-side", "right");
                } else {
                    // start with the left side
                    way.setTag("ors-sidewalk-side", "left");
                }
                break;
            case NONE:
                if(way.hasTag("ors-sidewalk-side")) {
                    way.removeTag("ors-sidewalk-side");
                }
        }

        return way;
    }

    public Side getPreparedSide(ReaderWay way) {
        if(way.hasTag("ors-sidewalk-side")) {
            String preparedSide = way.getTag("ors-sidewalk-side");
            if(preparedSide.equalsIgnoreCase("left")) {
                return Side.LEFT;
            }
            if(preparedSide.equalsIgnoreCase("right")) {
                return Side.RIGHT;
            }
        }

        return Side.NONE;
    }

    protected Side identifySidesWhereSidewalkIsPresent(ReaderWay osmWay) {
        boolean sidewalkOnLeftSide = false;
        boolean sidewalkOnRightSide = false;
        boolean sidewalkOnBothSides = false;

        if(osmWay.hasTag("sidewalk")) {
            String side = osmWay.getTag("sidewalk");
            switch(side) {
                case "left":
                    sidewalkOnLeftSide = true;
                    break;
                case "right":
                    sidewalkOnRightSide = true;
                    break;
                case "both":
                    sidewalkOnBothSides = true;
                    break;
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

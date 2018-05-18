package heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;

public class OSMPedestrianProcessor {
    public boolean isPedestrianisedWay(ReaderWay way) {

        boolean isPedestrian = false;

        if(way.hasTag("highway")) {
            String highwayType = way.getTag("highway");
            switch (highwayType) {
                case "footway":
                case "living_street":
                case "pedestrian":
                case "path":
                case "track":
                    isPedestrian = true;
                    break;
            }
        }
        if(way.hasTag("foot")) {
            if(way.getTag("foot").equalsIgnoreCase("designated")) {
                isPedestrian = true;
            }
        }

        return isPedestrian;
    }
}

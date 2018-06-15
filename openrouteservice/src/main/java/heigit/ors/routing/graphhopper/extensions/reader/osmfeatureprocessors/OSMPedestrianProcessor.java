package heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;

public class OSMPedestrianProcessor {
    /**
     * Determine if the way is pedestrianised, i.e. that a person should be able to traverse it on foot.
     *
     * @param way
     * @return
     */
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

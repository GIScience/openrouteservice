package heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OSMPedestrianProcessor {
    List<String> allowed;

    public OSMPedestrianProcessor() {
        allowed = new ArrayList<>();
        allowed.add("yes");
        allowed.addAll(Arrays.asList(new String[] {
                "yes",
                "designated",
                "permissive",
                "destination"
        }));
    }
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
        if(way.hasTag("public_transport") && way.getTag("public_transport").equals("platform"))
            isPedestrian = true;

        if(way.hasTag("foot")) {
            String footTag = way.getTag("foot");
            if(allowed.contains(footTag)) {
                isPedestrian = true;
            }
        }

        return isPedestrian;
    }
}

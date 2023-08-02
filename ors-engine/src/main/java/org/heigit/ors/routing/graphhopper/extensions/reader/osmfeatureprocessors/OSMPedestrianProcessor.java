package org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;

import java.util.Set;

public class OSMPedestrianProcessor {
    private static final Set<String> footValues = Set.of("yes", "designated", "permissive", "destination");
    private static final Set<String> highwayVaules = Set.of("footway", "living_street", "pedestrian", "path", "track");

    private OSMPedestrianProcessor() {
    }

    /**
     * Determine if the way is pedestrianised, i.e. that a person should be able to traverse it on foot.
     *
     * @param way
     * @return
     */
    public static boolean isPedestrianisedWay(ReaderWay way) {
        return way.hasTag("highway") && highwayVaules.contains(way.getTag("highway").toLowerCase())
                || way.hasTag("public_transport") && way.getTag("public_transport").equals("platform")
                || way.hasTag("foot") && footValues.contains(way.getTag("foot").toLowerCase());
    }
}

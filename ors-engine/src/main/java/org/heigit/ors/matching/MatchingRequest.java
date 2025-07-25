package org.heigit.ors.matching;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.AccessFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.PMap;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.ServiceRequest;
import org.heigit.ors.matrix.MatrixSearchContext;
import org.heigit.ors.matrix.MatrixSearchContextBuilder;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.WeightingMethod;
import org.heigit.ors.routing.graphhopper.extensions.ORSWeightingFactory;
import org.heigit.ors.snapping.SnappingResult;
import org.heigit.ors.util.ProfileTools;
import org.locationtech.jts.geom.Coordinate;

public class MatchingRequest extends ServiceRequest {
    @Setter
    @Getter
    private String profileName;

    @Getter
    private final int profileType;
    private int maximumLocations;

    public MatchingRequest(int profileType) {
        this.profileType = profileType;
    }

    public MatchingResult computeResult(RoutingProfile rp) throws Exception {
        GraphHopper gh = rp.getGraphhopper();GraphHopperStorage ghStorage = gh.getGraphHopperStorage();
        String graphDate = ghStorage.getProperties().get("datareader.import.date");

        return new MatchingResult(graphDate);
    }
}

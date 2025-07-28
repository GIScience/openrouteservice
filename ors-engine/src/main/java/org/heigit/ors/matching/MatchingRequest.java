package org.heigit.ors.matching;

import com.graphhopper.GraphHopper;
import com.graphhopper.storage.GraphHopperStorage;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.ServiceRequest;
import org.heigit.ors.routing.RoutingProfile;
import org.locationtech.jts.geom.Geometry;

import java.util.List;
import java.util.Map;

public class MatchingRequest extends ServiceRequest {
    @Setter
    @Getter
    private String profileName;

    @Getter
    private final int profileType;

    @Getter
    @Setter
    private String key;

    @Getter
    @Setter
    private Geometry geometry;

    @Getter
    @Setter
    List<Map<String, String>> properties;

    public MatchingRequest(int profileType) {
        this.profileType = profileType;
    }

    public MatchingResult computeResult(RoutingProfile rp) throws Exception {
        GraphHopper gh = rp.getGraphhopper();
        GraphHopperStorage ghStorage = gh.getGraphHopperStorage();
        String graphDate = ghStorage.getProperties().get("datareader.import.date");

        return new MatchingResult(graphDate);
    }
}

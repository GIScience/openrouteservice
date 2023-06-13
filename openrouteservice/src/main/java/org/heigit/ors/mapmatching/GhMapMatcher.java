package org.heigit.ors.mapmatching;

import com.graphhopper.GraphHopper;
import com.graphhopper.matching.MapMatching;
import com.graphhopper.matching.MatchResult;
import com.graphhopper.matching.Observation;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.shapes.GHPoint;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import java.util.Arrays;
import java.util.List;

/**
 * GhMapMatcher is a facade implementing the interface of ORS's
 * AbstractMapMatcher using Graphhopper's MapMatching internally.
 */
public class GhMapMatcher extends AbstractMapMatcher {
    PMap hints;
    public GhMapMatcher(GraphHopper graphHopper, String profile) {
        setGraphHopper(graphHopper);
        hints = new PMap()
            .putObject("profile", profile)
            .putObject(Parameters.Landmark.DISABLE, true);
    }
    @Override
    public RouteSegmentInfo[] match(Coordinate[] locations, boolean bothDirections) {
        MapMatching mapMatching = new MapMatching(graphHopper, hints);
        List<Observation> inputGPXEntries = getObservationsFromLocations(locations);
        MatchResult mr = mapMatching.match(inputGPXEntries);
        return getRouteSegmentInfoFromMatchResult(mr);
    }

    private static RouteSegmentInfo[] getRouteSegmentInfoFromMatchResult(MatchResult mr) {
        List<EdgeIteratorState> edgeStates = mr.getMergedPath().calcEdges();
        double distance = mr.getMatchLength();
        long time = mr.getMatchMillis();
        Geometry geometry = null; // TODO: get geometry from match result
                                  // it is unclear, whether the geometry is
                                  // used at all, so we leave it for now.
        RouteSegmentInfo[] rsi = new RouteSegmentInfo[1];
        rsi[0] = new RouteSegmentInfo(edgeStates, distance, time, geometry);
        return rsi;
    }

    private static List<Observation> getObservationsFromLocations(Coordinate[] locations) {
        return Arrays.stream(locations).map(
                loc -> new Observation(new GHPoint(loc.y, loc.x))
        ).toList();
    }

    @Override
    public void clear() {

    }
}

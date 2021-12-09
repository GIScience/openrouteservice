package org.heigit.ors.routing.graphhopper.extensions.reader.ubertraffic;

import com.carrotsearch.hppc.LongObjectHashMap;

import java.util.HashMap;

public class UberTrafficPattern {
    private final long osmWayId;
    private final UberTrafficEnums.PatternResolution resolution;
    private final LongObjectHashMap<HashMap<Long, Float[]>> patternsByOsmId;

    public UberTrafficPattern(long osmWayId, UberTrafficEnums.PatternResolution patternResolution) {
        this.osmWayId = osmWayId;
        this.resolution = patternResolution;
        this.patternsByOsmId = new LongObjectHashMap<>();
    }

    public void addPattern(long osmStartNodeId, long osmEndNodeId, int hour_of_day, float speed_kph_mean) {
        HashMap<Long, Float[]> node_patterns = this.patternsByOsmId.get(osmStartNodeId);

        if (node_patterns != null) {
            Float[] hourly_speeds = node_patterns.get(osmEndNodeId);
            if (hourly_speeds == null) {
                hourly_speeds = new Float[24];
            }
            hourly_speeds[hour_of_day] = speed_kph_mean;
            node_patterns.put(osmEndNodeId, hourly_speeds);
        } else {
            node_patterns = new HashMap<>();
            Float[] hourly_speeds = new Float[24];
            if (hour_of_day >23){
                System.out.println("");
            }
            hourly_speeds[hour_of_day] = speed_kph_mean;
            node_patterns.put(osmEndNodeId, hourly_speeds);
        }
        this.patternsByOsmId.put(osmStartNodeId, node_patterns);
    }

    public long getPatternId() {
        return this.osmWayId;
    }


    public Float[] getNodePattern(long osmStartNodeId, long osmEndNodeId) {
        HashMap<Long, Float[]> osmStartNodeIdPatterns = this.patternsByOsmId.get(osmStartNodeId);
        if (osmStartNodeIdPatterns != null) {
            return osmStartNodeIdPatterns.get(osmEndNodeId);
        }
        return null;
    }
}




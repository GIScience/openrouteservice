package org.heigit.ors.routing.graphhopper.extensions.reader.ubertraffic;

import com.carrotsearch.hppc.LongHashSet;
import com.carrotsearch.hppc.LongObjectHashMap;

public class UberTrafficPattern {
    private final long osmWayId;
    private final int newUberEdgeId;
    private final UberTrafficEnums.PatternResolution resolution;

    private final LongObjectHashMap<LongObjectHashMap<byte[]>> patternsByOsmId;

    public UberTrafficPattern(long osmWayId, int osmWayIdNew, UberTrafficEnums.PatternResolution patternResolution) {
        this.osmWayId = osmWayId;
        this.newUberEdgeId = osmWayIdNew;
        this.resolution = patternResolution;
        this.patternsByOsmId = new LongObjectHashMap<>();
    }

    public void addPatternByOsmId(long osmStartNodeId, long osmEndNodeId, int hour_of_day, byte speed_kph_mean) {
        LongObjectHashMap<byte[]> patterns = this.patternsByOsmId.get(osmStartNodeId);
        if (patterns == null) {
            patterns = new LongObjectHashMap<>();
        }
        byte[] hourly_speeds = patterns.get(osmEndNodeId);
        if (hourly_speeds == null) {
            hourly_speeds = new byte[24];
        }
        hourly_speeds[hour_of_day] = speed_kph_mean;
        patterns.put(osmEndNodeId, hourly_speeds);
        this.patternsByOsmId.put(osmStartNodeId, patterns);
    }

    public LongObjectHashMap<byte[]> getPatternsByOsmId(long osmStartNodeId) {
        LongObjectHashMap<byte[]> patterns = this.patternsByOsmId.get(osmStartNodeId);
        if (patterns == null) {
            return new LongObjectHashMap<>();
        }
        return patterns;
    }

    public long[] getAllNodeIds() {
        LongHashSet ids = new LongHashSet();
        for (long startKey : this.patternsByOsmId.keys) {
            ids.add(startKey);
            if (startKey > 0) {
                for (long destKey : this.patternsByOsmId.get(startKey).keys) {
                    ids.add(destKey);
                }

            }
        }
        return ids.toArray();
    }
}




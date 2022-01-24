package org.heigit.ors.routing.graphhopper.extensions.reader.ubertraffic;

import com.carrotsearch.hppc.LongHashSet;
import com.carrotsearch.hppc.LongObjectHashMap;
import com.carrotsearch.hppc.cursors.LongObjectCursor;


public class UberTrafficData {

    private LongObjectHashMap<UberTrafficPattern> patternsByOsmId;

    public UberTrafficData() {
        this.patternsByOsmId = new LongObjectHashMap<>();
    }

    public UberTrafficPattern getPattern(Long osm_way_id) {
        return patternsByOsmId.get(osm_way_id);
    }

    public void setPatternsByOsmId(LongObjectHashMap<UberTrafficPattern> patternsByOsmId) {
        this.patternsByOsmId = patternsByOsmId;
    }

    public LongObjectHashMap<UberTrafficPattern> getPatterns() {
        return this.patternsByOsmId;
    }

    public long[] getUniqueOriginalOsmNodeIds() {
        LongHashSet longCursors = new LongHashSet();
        for (LongObjectCursor<UberTrafficPattern> patternObject : this.patternsByOsmId) {
            longCursors.addAll(patternObject.value.getAllNodeIds());
        }
        longCursors.removeAll(0);
        return longCursors.toArray();
    }
}

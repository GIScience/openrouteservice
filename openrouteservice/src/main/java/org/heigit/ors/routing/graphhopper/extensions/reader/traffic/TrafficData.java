package org.heigit.ors.routing.graphhopper.extensions.reader.traffic;

import com.carrotsearch.hppc.IntObjectHashMap;

public class TrafficData {

    private IntObjectHashMap<TrafficLink> links;
    private IntObjectHashMap<TrafficPattern> patterns;

    public TrafficData() {
        this.links = new IntObjectHashMap<>();
        this.patterns = new IntObjectHashMap<>();
    }


    /**
     * Add a {@link TrafficLink} to the Traffic data and update the extent.
     *
     * @param link Add a  link to the traffic data.
     */
    public void setLink(TrafficLink link) {
        this.links.put(link.getLinkId(), link);
    }

    public IntObjectHashMap<TrafficLink> getLinks() {
        return links;
    }

    public boolean hasLink(Integer linkId) {
        return links.get(linkId) != null;
    }

    public TrafficLink getLink(int linkId) {
        return links.get(linkId);
    }

    public IntObjectHashMap<TrafficPattern> getPatterns() {
        return patterns;
    }

    public void setPattern(TrafficPattern pattern) {
        this.patterns.put(pattern.getPatternId(), pattern);
    }
}

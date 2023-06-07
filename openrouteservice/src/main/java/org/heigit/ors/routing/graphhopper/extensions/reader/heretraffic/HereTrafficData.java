package org.heigit.ors.routing.graphhopper.extensions.reader.heretraffic;

import com.carrotsearch.hppc.IntObjectHashMap;

public class HereTrafficData {

    private IntObjectHashMap<HereTrafficLink> links;
    private IntObjectHashMap<HereTrafficPattern> patterns;

    public HereTrafficData() {
        this.links = new IntObjectHashMap<>();
        this.patterns = new IntObjectHashMap<>();
    }


    /**
     * Add a {@link HereTrafficLink} to the Traffic data and update the extent.
     *
     * @param link Add a  link to the traffic data.
     */
    public void setLink(HereTrafficLink link) {
        this.links.put(link.getLinkId(), link);
    }

    public IntObjectHashMap<HereTrafficLink> getLinks() {
        return links;
    }

    public boolean hasLink(Integer linkId) {
        return links.get(linkId) != null;
    }

    public HereTrafficLink getLink(int linkId) {
        return links.get(linkId);
    }

    public IntObjectHashMap<HereTrafficPattern> getPatterns() {
        return patterns;
    }

    public void setPattern(HereTrafficPattern pattern) {
        this.patterns.put(pattern.getPatternId(), pattern);
    }
}

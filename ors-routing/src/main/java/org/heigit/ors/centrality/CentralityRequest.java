package org.heigit.ors.centrality;

import com.graphhopper.util.shapes.BBox;
import org.heigit.ors.common.ServiceRequest;

import java.util.ArrayList;
import java.util.List;

public class CentralityRequest extends ServiceRequest {
    private BBox bbox;
    private List<Integer> excludeNodes = new ArrayList<>();
    private String mode;

    private int profileType = -1;

    public BBox getBoundingBox() { return this.bbox; }

    public void setBoundingBox(BBox bbox) { this.bbox = bbox; }

    public List<Integer> getExcludeNodes() { return excludeNodes; }

    public void setExcludeNodes(List<Integer> excludeNodes) { this.excludeNodes = excludeNodes; }

    public int getProfileType() { return profileType; }

    public void setProfileType(int profileType) { this.profileType = profileType; }

    public String getMode() { return mode; }

    public void setMode(String mode) { this.mode = mode; }
}

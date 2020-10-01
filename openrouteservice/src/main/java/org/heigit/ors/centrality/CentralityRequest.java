package org.heigit.ors.centrality;

import com.graphhopper.util.shapes.BBox;
import org.heigit.ors.services.ServiceRequest;

public class CentralityRequest extends ServiceRequest {
    private BBox bbox;
    private int profileType = -1;

    public BBox getBoundingBox() { return this.bbox; }

    public void setBoundingBox(BBox bbox) { this.bbox = bbox; }

    public int getProfileType() { return profileType; }

    public void setProfileType(int profileType) { this.profileType = profileType; }
}

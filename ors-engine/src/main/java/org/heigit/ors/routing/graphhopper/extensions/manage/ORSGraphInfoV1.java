package org.heigit.ors.routing.graphhopper.extensions.manage;

import java.util.Date;

public class ORSGraphInfoV1 {

    private Date osmDate;

    public ORSGraphInfoV1() {
    }

    public ORSGraphInfoV1(Date osmDate) {
        this.osmDate = osmDate;
    }

    public Date getOsmDate() {
        return osmDate;
    }

    public void setOsmDate(Date osmDate) {
        this.osmDate = osmDate;
    }
}

package org.heigit.ors.routing.graphhopper.extensions.manage;

import java.util.Date;

public class ORSGraphInfoV1 {

    private Date osmDate;
    private Date importDate;
    private ORSGraphInfoV1ProfileProperties profileProperties;

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

    public Date getImportDate() {
        return importDate;
    }

    public void setImportDate(Date importDate) {
        this.importDate = importDate;
    }

    public ORSGraphInfoV1ProfileProperties getProfileProperties() {
        return profileProperties;
    }

    public void setProfileProperties(ORSGraphInfoV1ProfileProperties profileProperties) {
        this.profileProperties = profileProperties;
    }
}

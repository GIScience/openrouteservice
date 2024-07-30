package org.heigit.ors.routing.graphhopper.extensions.manage;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class ORSGraphInfoV1 {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date osmDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date importDate;

    @JsonProperty("profileProperties")
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

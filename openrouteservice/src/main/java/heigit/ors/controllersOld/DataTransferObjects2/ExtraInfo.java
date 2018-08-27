package heigit.ors.controllersOld.DataTransferObjects2;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ExtraInfo {
    @JsonProperty("surface") SURFACE,
    @JsonProperty("waycategory") WAY_CATEGORY,
    @JsonProperty("osmid") OSM_ID
}

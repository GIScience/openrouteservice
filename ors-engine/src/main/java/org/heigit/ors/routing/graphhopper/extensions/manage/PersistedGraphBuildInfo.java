package org.heigit.ors.routing.graphhopper.extensions.manage;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.heigit.ors.config.profile.ProfileProperties;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class PersistedGraphBuildInfo {

    @JsonProperty("graph_build_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date graphBuildDate;

    @JsonProperty("osm_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date osmDate;

    @JsonProperty("graph_version")
    private String graphVersion;

    @JsonProperty("profile_properties")
    private ProfileProperties profileProperties;

    public static PersistedGraphBuildInfo withOsmDate(Date osmDate) {
        PersistedGraphBuildInfo graphBuildInfo = new PersistedGraphBuildInfo();
        graphBuildInfo.setOsmDate(osmDate);
        return graphBuildInfo;
    }

    public static PersistedGraphBuildInfo withGraphBuildDate(Date importDate) {
        PersistedGraphBuildInfo graphBuildInfo = new PersistedGraphBuildInfo();
        graphBuildInfo.setGraphBuildDate(importDate);
        return graphBuildInfo;
    }

}

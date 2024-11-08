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
public class PersistedGraphInfo {

    @JsonProperty("graph_build_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date graphBuildDate;

    @JsonProperty("osm_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date osmDate;

    @JsonProperty("profile_properties")
    private ProfileProperties profileProperties;

    public static PersistedGraphInfo withOsmDate(Date osmDate) {
        PersistedGraphInfo graphInfo = new PersistedGraphInfo();
        graphInfo.setOsmDate(osmDate);
        return graphInfo;
    }

    public static PersistedGraphInfo withGraphBuildDate(Date importDate) {
        PersistedGraphInfo graphInfo = new PersistedGraphInfo();
        graphInfo.setGraphBuildDate(importDate);
        return graphInfo;
    }

}

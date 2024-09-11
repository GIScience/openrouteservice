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
public class ORSGraphInfoV1 { //TOOD rename (remove V1)

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date importDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date osmDate;

    @JsonProperty("profile_properties")
    private ProfileProperties profileProperties;

    public static ORSGraphInfoV1 withOsmDate(Date osmDate){
        ORSGraphInfoV1 graphInfo = new ORSGraphInfoV1();
        graphInfo.setOsmDate(osmDate);
        return graphInfo;
    }

    public static ORSGraphInfoV1 withImportDate(Date importDate){
        ORSGraphInfoV1 graphInfo = new ORSGraphInfoV1();
        graphInfo.setImportDate(importDate);
        return graphInfo;
    }

}

package org.heigit.ors.config.defaults;

import lombok.Getter;
import org.heigit.ors.config.profile.ProfileProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
public class DefaultProfiles {
    Map<String, ProfileProperties> profiles = new HashMap<>();

    public DefaultProfiles(Boolean setDefaults) {
        if (setDefaults) {
            profiles.put("car", new DefaultProfilePropertiesCar(true));
            profiles.put("hgv", new DefaultProfilePropertiesHgv(true));
            profiles.put("bike-regular", new DefaultProfilePropertiesBikeRegular(true));
            profiles.put("bike-mountain", new DefaultProfilePropertiesBikeMountain(true));
            profiles.put("bike-road", new DefaultProfilePropertiesBikeRoad(true));
            profiles.put("bike-electric", new DefaultProfilePropertiesBikeElectric(true));
            profiles.put("walking", new DefaultProfilePropertiesWalking(true));
            profiles.put("hiking", new DefaultProfilePropertiesHiking(true));
            profiles.put("wheelchair", new DefaultProfilePropertiesWheelchair(true));
            profiles.put("public-transport", new DefaultProfilePropertiesPublicTransport(true));
        }
    }

}

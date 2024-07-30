package org.heigit.ors.config.profile.defaults;

import lombok.Getter;
import org.heigit.ors.config.profile.ProfileProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
public class DefaultProfiles {
    Map<String, ProfileProperties> profiles = new HashMap<>();

    public DefaultProfiles(Boolean setDefaults) {
        if (setDefaults) {
            profiles.put("driving-car", new DefaultProfileProperties(true));
            profiles.put("driving-hgv", new DefaultProfileProperties(true));
            profiles.put("cycling-regular", new DefaultProfileProperties(true));
            profiles.put("cycling-mountain", new DefaultProfileProperties(true));
            profiles.put("cycling-road", new DefaultProfileProperties(true));
            profiles.put("cycling-electric", new DefaultProfileProperties(true));
            profiles.put("foot-walking", new DefaultProfileProperties(true));
            profiles.put("foot-hiking", new DefaultProfileProperties(true));
            profiles.put("wheelchair", new DefaultProfileProperties(true));
            profiles.put("public-transport", new DefaultProfileProperties(true));
        }
    }

}

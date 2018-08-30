package heigit.ors.api.converters;

import org.springframework.core.convert.converter.Converter;
import heigit.ors.api.requests.routing.APIRoutingEnums;

public class APIRequestProfileConverter implements Converter<String, APIRoutingEnums.RoutingProfile> {
    @Override
    public APIRoutingEnums.RoutingProfile convert(String s) {
        for(APIRoutingEnums.RoutingProfile profile : APIRoutingEnums.RoutingProfile.values()) {
            if(profile.toString().equals(s)) {
                return profile;
            }
        }
        return null;
    }
}

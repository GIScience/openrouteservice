package org.heigit.ors.api.converters;

import org.heigit.ors.api.config.profile.DefaultProfileProperties;
import org.heigit.ors.api.config.profile.ProfileProperties;
import org.heigit.ors.util.StringUtility;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationPropertiesBinding
public class ConfigEmptyProfileMapConverter implements Converter<String, Map<String, ProfileProperties>> {
    @Override
    public Map<String, ProfileProperties> convert(String from) {
        Map<String, ProfileProperties> map = new HashMap<>();
        if (!StringUtility.isNullOrEmpty(from))
            map.put(from, new DefaultProfileProperties());
        return map;
    }
}

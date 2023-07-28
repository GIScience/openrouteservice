package org.heigit.ors.api.converters;

import org.heigit.ors.api.EngineProperties;
import org.heigit.ors.util.StringUtility;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationPropertiesBinding
public class ConfigEmptyProfileMapConverter implements Converter<String, Map<String, EngineProperties.ProfileProperties>> {
    @Override
    public Map<String, EngineProperties.ProfileProperties> convert(String from) {
        Map<String, EngineProperties.ProfileProperties> map = new HashMap<>();
        if (!StringUtility.isNullOrEmpty(from))
            map.put(from, new EngineProperties.ProfileProperties());
        return map;
    }
}

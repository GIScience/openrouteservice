package org.heigit.ors.api.converters;

import org.heigit.ors.util.StringUtility;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationPropertiesBinding
public class ConfigEmptyMapConverter implements Converter<String, Map<String, String>> {
    @Override
    public Map<String, String> convert(String from) {
        Map<String, String> map = new HashMap<>();
        if (!StringUtility.isNullOrEmpty(from))
            map.put(from, "");
        return map;
    }
}

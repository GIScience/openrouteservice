package org.heigit.ors.api.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "ors.messages")
public class SystemMessageProperties extends ArrayList<SystemMessageProperties.MessageObject> {
    @Getter
    @Setter(AccessLevel.PACKAGE)
    public static class MessageObject {
        private boolean active;
        private String text;
        private List<Map<String, String>> condition;
    }
}

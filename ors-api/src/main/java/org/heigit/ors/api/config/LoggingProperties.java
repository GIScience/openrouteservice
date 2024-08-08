package org.heigit.ors.api.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter(AccessLevel.PACKAGE)
@Configuration
@ConfigurationProperties(prefix = "logging")
public class LoggingProperties {
    private FileProperties file;
    private PatternProperties pattern;
    private LevelProperties level;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    public static class FileProperties {
        private String name;
    }

    @Getter
    @Setter(AccessLevel.PACKAGE)
    public static class PatternProperties {
        private String console;
        private String file;
    }

    @Getter
    @Setter(AccessLevel.PACKAGE)
    public static class LevelProperties {
        private String root;
        private OrgProperties org;
    }

    @Getter
    @Setter(AccessLevel.PACKAGE)
    @Configuration
    @ConfigurationProperties(prefix = "logging.level.org")
    public static class OrgProperties {
        private String heigit;
    }
}

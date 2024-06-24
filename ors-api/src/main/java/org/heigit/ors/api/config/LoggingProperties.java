package org.heigit.ors.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "logging")
public class LoggingProperties {
    private FileProperties file;
    private PatternProperties pattern;
    private LevelProperties level;

    public FileProperties getFile() {
        return file;
    }

    public void setFile(FileProperties file) {
        this.file = file;
    }

    public PatternProperties getPattern() {
        return pattern;
    }

    public void setPattern(PatternProperties pattern) {
        this.pattern = pattern;
    }

    public LevelProperties getLevel() {
        return level;
    }

    public void setLevel(LevelProperties level) {
        this.level = level;
    }

    public static class FileProperties {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class PatternProperties {
        private String console;
        private String file;

        public String getConsole() {
            return console;
        }

        public void setConsole(String console) {
            this.console = console;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }
    }

    public static class LevelProperties {
        private String root;
        private OrgProperties org;

        public String getRoot() {
            return root;
        }

        public void setRoot(String root) {
            this.root = root;
        }

        public OrgProperties getOrg() {
            return org;
        }

        public void setOrg(OrgProperties org) {
            this.org = org;
        }
    }

    @Configuration
    @ConfigurationProperties(prefix = "logging.level.org")
    public static class OrgProperties {
        private String heigit;

        public String getHeigit() {
            return heigit;
        }

        public void setHeigit(String heigit) {
            this.heigit = heigit;
        }
    }
}

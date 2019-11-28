package org.heigit.ors.logging;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.json.JsonConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Plugin(
        name="CustomConfigurationFactory",
        category = ConfigurationFactory.CATEGORY
)
@Order(50)
public class LoggingConfigFactory extends ConfigurationFactory{
    protected static final Logger LOGGER = Logger.getLogger(LoggingConfigFactory.class);

    public Configuration getConfiguration(LoggerContext context, ConfigurationSource source) {
        // We need to read the settings from the app.config if it is available
        if (LoggingSettings.getEnabled()) {
            String settingsFileName = LoggingSettings.getLevelFile();

            System.setProperty("ors-log-filepath", LoggingSettings.getLocation());

            if (settingsFileName != null) {
                ClassPathResource rs = new ClassPathResource("logs/" + settingsFileName);
                try {
                    source = new ConfigurationSource(rs.getInputStream());
                } catch (IOException ioe) {
                    LOGGER.error("LOGGING FILE DOES NOT EXIST!");
                }
            }

            return new LoggingJsonConfiguration(context, source);
        }

        return null;

    }

    public String[] getSupportedTypes() {
        return new String[] {"*"};
    }

}

class LoggingJsonConfiguration extends JsonConfiguration {
    public LoggingJsonConfiguration(LoggerContext context, ConfigurationSource source) {
        super(context, source);

    }

    @Override
    protected void doConfigure() {
        super.doConfigure();
        if(!LoggingSettings.getStdOut())
            this.removeAppender("stdout");

    }
}





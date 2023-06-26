package org.heigit.ors.api.util;

import com.typesafe.config.ConfigObject;
import org.apache.log4j.Logger;
import org.heigit.ors.config.AppConfig;

import java.util.ArrayList;
import java.util.List;

public class AppConfigMigration {
    private static final Logger LOGGER = Logger.getLogger(AppConfigMigration.class.getName());

    public static void loadSystemMessagesfromAppConfig(List<SystemMessage.Message> messages) {
        for (ConfigObject message : AppConfig.getGlobal().getObjectList("system_message")) {
            try {
                if (message.toConfig().getBoolean("active")) {
                    List<SystemMessage.Condition> conditions = new ArrayList<>();
                    loadConditionsForMessage(message, conditions);
                    messages.add(new SystemMessage.Message(message.toConfig().getString("text"), conditions));
                }
            } catch (Exception e) {
                // ignore otherwise incomplete messages entirely
                LOGGER.warn(String.format("Invalid SystemMessage object in ors config %s.", message.toString().substring(18)));
            }
        }
    }

    private static void loadConditionsForMessage(ConfigObject message, List<SystemMessage.Condition> conditions) {
        try {
            ConfigObject condition = message.toConfig().getObject("condition");
            for (String key : condition.keySet()) {
                conditions.add(new SystemMessage.Condition(key, condition.toConfig().getString(key)));
            }
        } catch (Exception e) {
            // ignore missing condition block and keep message
            LOGGER.info("Invalid or missing condition in message object.");
        }
    }
}

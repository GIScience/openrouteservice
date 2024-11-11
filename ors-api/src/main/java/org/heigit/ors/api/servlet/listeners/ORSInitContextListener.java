/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://giscience.uni-hd.de
 *   http://heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file
 *  distributed with this work for additional information regarding copyright
 *  ownership. The GIScience licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.heigit.ors.api.servlet.listeners;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import lombok.AllArgsConstructor;
import org.apache.juli.logging.LogFactory;
import org.apache.log4j.Logger;
import org.heigit.ors.api.services.GraphService;
import org.heigit.ors.api.util.AppInfo;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.isochrones.statistics.StatisticsProviderFactory;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.RoutingProfileManagerStatus;
import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphManager;
import org.heigit.ors.util.FormatUtility;
import org.heigit.ors.util.StringUtility;
import org.springframework.core.io.ClassPathResource;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@AllArgsConstructor
public class ORSInitContextListener implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(ORSInitContextListener.class);
    private final EngineProperties engineProperties;
    private final GraphService graphService;

    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        String outputTarget = configurationOutputTarget(engineProperties);
        if (!StringUtility.isNullOrEmpty(outputTarget)) {
            copyDefaultConfigurationToFile(outputTarget);
            return;
        }
        new Thread(() -> {
            try {
                LOGGER.info("Initializing ORS...");
                graphService.setIsActivatingGraphs(true);
                RoutingProfileManager routingProfileManager = new RoutingProfileManager(engineProperties, AppInfo.GRAPH_VERSION);
                for (RoutingProfile profile : routingProfileManager.getUniqueProfiles()) {
                    ORSGraphManager orsGraphManager = profile.getGraphhopper().getOrsGraphManager();
                    if (orsGraphManager != null && orsGraphManager.useGraphRepository()) {
                        LOGGER.debug("Adding orsGraphManager for profile %s with encoder %s to GraphService".formatted(profile.getProfileConfiguration().getProfileName(), profile.getProfileConfiguration().getEncoderName()));
                        graphService.addGraphManagerInstance(orsGraphManager);
                    }
                }
                if (Boolean.TRUE.equals(engineProperties.getPreparationMode())) {
                    LOGGER.info("Running in preparation mode, all enabled graphs are built, job is done.");
                    RoutingProfileManagerStatus.setShutdown(true);
                }
            } catch (Exception e) {
                LOGGER.warn("Unable to initialize ORS due to an unexpected exception: " + e);
            } finally {
                graphService.setIsActivatingGraphs(false);
            }
        }, "ORS-Init").start();
    }

    public String configurationOutputTarget(EngineProperties engineProperties) {
        String output = engineProperties.getConfigOutput();
        if (StringUtility.isNullOrEmpty(output))
            return null;
        if (!output.endsWith(".yml") && !output.endsWith(".yaml"))
            output += ".yml";
        return output;
    }

    private void copyDefaultConfigurationToFile(String output) {
        try (FileOutputStream fos = new FileOutputStream(output)) {
            LOGGER.info("Creating configuration file " + output);
            fos.write(new ClassPathResource("application.yml").getContentAsString(StandardCharsets.UTF_8).getBytes());
        } catch (IOException e) {
            LOGGER.error("Failed to write output configuration file.", e);
        }
        LOGGER.info("Configuration output completed.");
        RoutingProfileManagerStatus.setShutdown(true);
    }

    @Override
    public void contextDestroyed(ServletContextEvent contextEvent) {
        try {
            LOGGER.info("Shutting down openrouteservice %s and releasing resources.".formatted(AppInfo.getEngineInfo()));
            FormatUtility.unload();
            if (RoutingProfileManagerStatus.isReady())
                RoutingProfileManager.getInstance().destroy();
            StatisticsProviderFactory.releaseProviders();
            LogFactory.release(Thread.currentThread().getContextClassLoader());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}

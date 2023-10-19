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
import org.apache.juli.logging.LogFactory;
import org.apache.log4j.Logger;
import org.heigit.ors.api.EngineProperties;
import org.heigit.ors.api.services.GraphService;
import org.heigit.ors.config.EngineConfig;
import org.heigit.ors.isochrones.statistics.StatisticsProviderFactory;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.RoutingProfileManagerStatus;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphManager;
import org.heigit.ors.util.FormatUtility;
import org.heigit.ors.util.StringUtility;

import java.net.MalformedURLException;
import java.net.URL;

import static org.heigit.ors.api.ORSEnvironmentPostProcessor.ORS_CONFIG_LOCATION_ENV;
import static org.heigit.ors.api.ORSEnvironmentPostProcessor.ORS_CONFIG_LOCATION_PROPERTY;

public class ORSInitContextListener implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(ORSInitContextListener.class);
    private final EngineProperties engineProperties;
    private final GraphService graphService;

    public ORSInitContextListener(EngineProperties engineProperties, GraphService graphService) {
        this.engineProperties = engineProperties;
        this.graphService = graphService;
    }

    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        if (LOGGER.isDebugEnabled()) {
            if (!StringUtility.isNullOrEmpty(System.getenv(ORS_CONFIG_LOCATION_ENV))) {
                LOGGER.debug("Configuration loaded by ENV, location: " + System.getenv(ORS_CONFIG_LOCATION_ENV));
            }
            if (!StringUtility.isNullOrEmpty(System.getProperty(ORS_CONFIG_LOCATION_PROPERTY))) {
                LOGGER.debug("Configuration loaded by ARG, location: " + System.getProperty(ORS_CONFIG_LOCATION_PROPERTY));
            }
        }
        SourceFileElements sourceFileElements = extractSourceFileElements(engineProperties.getSourceFile());
        final EngineConfig config = EngineConfig.EngineConfigBuilder.init()
                .setInitializationThreads(engineProperties.getInitThreads())
                .setPreparationMode(engineProperties.isPreparationMode())
                .setElevationPreprocessed(engineProperties.getElevation().isPreprocessed())
                .setGraphsRootPath(engineProperties.getGraphsRootPath())
                .setMaxNumberOfGraphBackups(engineProperties.getMaxNumberOfGraphBackups())
                .setSourceFile(sourceFileElements.localOsmFilePath)
                .setGraphsRepoUrl(sourceFileElements.repoBaseUrlString)
                .setGraphsRepoName(sourceFileElements.repoName)
                .setGraphsExtent(engineProperties.getGraphsExtent())
                .setProfiles(engineProperties.getConvertedProfiles())
                .buildWithAppConfigOverride();
        Runnable runnable = () -> {
            try {
                LOGGER.info("Initializing ORS...");
                RoutingProfileManager routingProfileManager = new RoutingProfileManager(config);
                if (routingProfileManager.getProfiles() != null) {
                    for (RoutingProfile profile : routingProfileManager.getProfiles().getUniqueProfiles()) {
                        ORSGraphHopper orsGraphHopper = profile.getGraphhopper();
                        ORSGraphManager orsGraphManager = orsGraphHopper.getOrsGraphManager();
                        if (orsGraphManager != null) {
                            LOGGER.debug("Adding orsGraphManager for profile %s to GraphService".formatted(profile.getConfiguration().getName()));
                            graphService.addGraphhopperLocation(orsGraphManager);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Unable to initialize ORS due to an unexpected exeception: " + e);
            }
        };
        Thread thread = new Thread(runnable);
        thread.setName("ORS-Init");
        thread.start();
    }

    record SourceFileElements(String repoBaseUrlString, String repoName, String localOsmFilePath) {
    }

    SourceFileElements extractSourceFileElements(String sourceFilePropertyValue) {
        String repoBaseUrlString = null;
        String repoName = null;
        String localOsmFilePath = "";
        try {
            new URL(sourceFilePropertyValue);
            LOGGER.debug("Configuration property 'source_file' contains a URL, using value as URL for a graphs repository");
            sourceFilePropertyValue = sourceFilePropertyValue.trim().replaceAll("/$", "");
            String[] urlElements = sourceFilePropertyValue.split("/");

            repoName = urlElements[urlElements.length - 1];
            repoBaseUrlString = sourceFilePropertyValue.replaceAll("/%s$".formatted(repoName), "");
        } catch (MalformedURLException e) {
            LOGGER.debug("Configuration property 'source_file' does not contain a URL, using value as local osm file path");
            localOsmFilePath = sourceFilePropertyValue;
        }
        return new SourceFileElements(repoBaseUrlString, repoName, localOsmFilePath);
    }

    @Override
    public void contextDestroyed(ServletContextEvent contextEvent) {
        try {
            LOGGER.info("Shutting down ORS and releasing resources.");
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

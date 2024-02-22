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
import org.heigit.ors.api.util.AppInfo;
import org.heigit.ors.config.EngineConfig;
import org.heigit.ors.isochrones.statistics.StatisticsProviderFactory;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.RoutingProfileManagerStatus;
import org.heigit.ors.util.FormatUtility;

public class ORSInitContextListener implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(ORSInitContextListener.class);
    private final EngineProperties engineProperties;

    public ORSInitContextListener(EngineProperties engineProperties) {
        this.engineProperties = engineProperties;
    }

    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        final EngineConfig config = EngineConfig.EngineConfigBuilder.init()
                .setInitializationThreads(engineProperties.getInitThreads())
                .setPreparationMode(engineProperties.isPreparationMode())
                .setElevationPreprocessed(engineProperties.getElevation().isPreprocessed())
                .setSourceFile(engineProperties.getSourceFile())
                .setGraphsRootPath(engineProperties.getGraphsRootPath())
                .setGraphsDataAccess(engineProperties.getGraphsDataAccess())
                .setProfiles(engineProperties.getConvertedProfiles())
                .buildWithAppConfigOverride();
        Runnable runnable = () -> {
            try {
                LOGGER.info("Initializing ORS...");
                new RoutingProfileManager(config);
                if (engineProperties.isPreparationMode()) {
                    LOGGER.info("Running in preparation mode, all enabled graphs are built, job is done.");
                    System.exit(0);
                }
            } catch (Exception e) {
                LOGGER.warn("Unable to initialize ORS due to an unexpected exeception: " + e);
            }
        };
        Thread thread = new Thread(runnable);
        thread.setName("ORS-Init");
        thread.start();
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

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
package org.heigit.ors.servlet.listeners;

import org.apache.juli.logging.LogFactory;
import org.apache.log4j.Logger;
import org.heigit.ors.isochrones.statistics.StatisticsProviderFactory;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.RoutingProfileManagerStatus;
import org.heigit.ors.util.FormatUtility;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ORSInitContextListener implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(ORSInitContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        Runnable runnable = () -> {
            try {
                RoutingProfileManager.getInstance();
            } catch (Exception e) {
                LOGGER.warn("Unable to initialize ORS." + e);
            }
        };

        Thread thread = new Thread(runnable);
        thread.setName("ORS-Init");
        thread.start();
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

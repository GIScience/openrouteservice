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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.apache.juli.logging.LogFactory;
import org.apache.log4j.Logger;
import org.heigit.ors.api.config.*;
import org.heigit.ors.api.util.AppInfo;
import org.heigit.ors.config.EngineConfig;
import org.heigit.ors.isochrones.statistics.StatisticsProviderFactory;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.RoutingProfileManagerStatus;
import org.heigit.ors.util.FormatUtility;

import java.io.FileOutputStream;
import java.io.IOException;

import static com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.*;

public class ORSInitContextListener implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(ORSInitContextListener.class);
    private final EngineProperties engineProperties;
    private final EndpointsProperties endpointsProperties;
    private final CorsProperties corsProperties;
    private final SystemMessageProperties systemMessageProperties;
    private final LoggingProperties loggingProperties;
    private final ServerProperties serverProperties;

    public ORSInitContextListener(EngineProperties engineProperties, EndpointsProperties endpointsProperties, CorsProperties corsProperties, SystemMessageProperties systemMessageProperties, LoggingProperties loggingProperties, ServerProperties serverProperties) {
        this.engineProperties = engineProperties;
        this.endpointsProperties = endpointsProperties;
        this.corsProperties = corsProperties;
        this.systemMessageProperties = systemMessageProperties;
        this.loggingProperties = loggingProperties;
        this.serverProperties = serverProperties;
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
                .build();

        if (engineProperties.isConfigOutputMode()) {
            YAMLFactory yf = new CustomYAMLFactory()
                    .disable(WRITE_DOC_START_MARKER)
                    .disable(SPLIT_LINES)
                    .enable(MINIMIZE_QUOTES);
            ObjectMapper mapper = new ObjectMapper(yf)
                    .configure(WRITE_BIGDECIMAL_AS_PLAIN, true);
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            try (FileOutputStream fos = new FileOutputStream("ors-config-example.yml"); JsonGenerator generator = mapper.createGenerator(fos)) {
                LOGGER.info("Output configuration file");
                ORSConfigBundle ors = new ORSConfigBundle(corsProperties, systemMessageProperties, endpointsProperties, engineProperties);
                ConfigBundle configBundle = new ConfigBundle(serverProperties, loggingProperties, ors);
                generator.writeObject(configBundle);
                if (LOGGER.isDebugEnabled()) {
                    System.out.println(mapper.writeValueAsString(configBundle));
                }
            } catch (IOException e) {
                LOGGER.error("Failed to write output configuration file.", e);
            }
            RoutingProfileManagerStatus.setShutdown(true);
            return;
        }

        new Thread(() -> {
            try {
                LOGGER.info("Initializing ORS...");
                new RoutingProfileManager(config);
                if (engineProperties.isPreparationMode()) {
                    LOGGER.info("Running in preparation mode, all enabled graphs are built, job is done.");
                    RoutingProfileManagerStatus.setShutdown(true);
                }
            } catch (Exception e) {
                LOGGER.warn("Unable to initialize ORS due to an unexpected exception: " + e);
            }
        }, "ORS-Init").start();
    }

    record ORSConfigBundle(
            @JsonIgnoreProperties({"$$beanFactory"})
            CorsProperties cors,
            @JsonInclude(JsonInclude.Include.CUSTOM)
            @JsonIgnoreProperties({"$$beanFactory"})
            SystemMessageProperties messages,
            @JsonIgnoreProperties({"$$beanFactory"})
            EndpointsProperties endpoints,
            @JsonIgnoreProperties({"$$beanFactory"})
            EngineProperties engine
    ) {
    }

    record ConfigBundle(
            @JsonProperty
            @JsonIgnoreProperties({"$$beanFactory"})
            ServerProperties server,
            @JsonProperty
            @JsonIgnoreProperties({"$$beanFactory"})
            LoggingProperties logging,
            @JsonProperty
            ORSConfigBundle ors
    ) {
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

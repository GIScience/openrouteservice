package org.heigit.ors.api.services;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import lombok.Getter;
import org.apache.juli.logging.LogFactory;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.isochrones.statistics.StatisticsProviderFactory;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphManager;
import org.heigit.ors.util.AppInfo;
import org.heigit.ors.util.FormatUtility;
import org.heigit.ors.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class EngineService implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(EngineService.class);

    private final EngineProperties engineProperties;
    private final GraphService graphService;
    private
    final Environment environment;
    @Getter
    private final RoutingProfileManager routingProfileManager;

    @Autowired
    public EngineService(EngineProperties engineProperties, GraphService graphService, Environment environment) {
        this.engineProperties = engineProperties;
        this.graphService = graphService;
        this.environment = environment;
        routingProfileManager = new RoutingProfileManager(engineProperties, AppInfo.GRAPH_VERSION);
    }

    public synchronized RoutingProfileManager waitForActiveRoutingProfileManager() {
        while (!routingProfileManager.isReady()) {
            try {
                this.wait(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("Thread interrupted while waiting for RoutingProfileManager to be ready.", e);
            }
        }
        return routingProfileManager;
    }

    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        String outputTarget = configurationOutputTarget(engineProperties);
        if (!StringUtility.isNullOrEmpty(outputTarget)) {
            copyDefaultConfigurationToFile(outputTarget);
            routingProfileManager.setShutdown(true);
            return;
        }
        new Thread(this::initializeORS, "ORS-Init").start();
    }

    private void initializeORS() {
        synchronized (EngineService.class) {
            try {
                LOGGER.info("Initializing ORS...");
                graphService.setIsActivatingGraphs(true);
                routingProfileManager.initialize();
                if (Boolean.TRUE.equals(engineProperties.getPreparationMode())) {
                    LOGGER.info("Running in preparation mode, all enabled graphs are built, job is done.");
                    if (environment.getActiveProfiles().length == 0) { // only exit if no active profile is set (i.e., not in test mode)
                        routingProfileManager.setShutdown(true);
                    }
                }
                if (routingProfileManager.isShutdown()) {
                    System.exit(routingProfileManager.hasFailed() ? 1 : 0);
                }
                for (RoutingProfile profile : routingProfileManager.getUniqueProfiles()) {
                    ORSGraphManager orsGraphManager = profile.getGraphhopper().getOrsGraphManager();
                    if (orsGraphManager != null && orsGraphManager.useGraphRepository()) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Adding orsGraphManager for profile %s with encoder %s to GraphService".formatted(profile.getProfileConfiguration().getProfileName(), profile.getProfileConfiguration().getEncoderName()));
                        }
                        graphService.addGraphManagerInstance(orsGraphManager);
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Unable to initialize ORS due to an unexpected exception: %s".formatted(e.toString()));
            } finally {
                graphService.setIsActivatingGraphs(false);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent contextEvent) {
        LOGGER.info("Shutting down openrouteservice {} and releasing resources.", AppInfo.getEngineInfo());
        try {
            FormatUtility.unload();
            StatisticsProviderFactory.releaseProviders();
            LogFactory.release(Thread.currentThread().getContextClassLoader());
            routingProfileManager.destroy();
        } catch (Exception e) {
            LOGGER.error(e.toString());
        }
    }

    public static String configurationOutputTarget(EngineProperties engineProperties) {
        String output = engineProperties.getConfigOutput();
        if (StringUtility.isNullOrEmpty(output))
            return null;
        if (!output.endsWith(".yml") && !output.endsWith(".yaml"))
            output += ".yml";
        return output;
    }

    public static void copyDefaultConfigurationToFile(String output) {
        try (FileOutputStream fos = new FileOutputStream(output)) {
            LOGGER.info("Creating configuration file {}", output);
            fos.write(new ClassPathResource("application.yml").getContentAsString(StandardCharsets.UTF_8).getBytes());
        } catch (IOException e) {
            LOGGER.error("Failed to write output configuration file.", e);
        }
        LOGGER.info("Configuration output completed.");
    }
}

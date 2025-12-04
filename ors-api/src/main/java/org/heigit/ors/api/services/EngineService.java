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
import java.util.concurrent.ExecutionException;

@Service
public class EngineService implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(EngineService.class);
    public static final String SHUTDOWN_IMMEDIATELY = "ors.engine_service.shutdown";
    private final EngineProperties engineProperties;
    private final GraphService graphService;
    @Getter
    private final RoutingProfileManager routingProfileManager;

    @Autowired
    public EngineService(EngineProperties engineProperties, GraphService graphService, Environment environment) {
        this.engineProperties = engineProperties;
        this.graphService = graphService;
        routingProfileManager = new RoutingProfileManager(engineProperties, engineProperties.getPreparationMode() && environment.getActiveProfiles().length == 0);
    }

    /*
     * Waits for the RoutingProfileManager to be fully initialized and ready.
     * This method blocks the calling thread until the initialization is complete.
     * EngineSevice.getRoutingProfileManager().isReady() can be used to check the readiness without blocking.
     *
     * @return The initialized RoutingProfileManager instance.
     * */
    public RoutingProfileManager waitForInitializedRoutingProfileManager() {
        try {
            routingProfileManager.awaitReady();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Thread interrupted while waiting for RoutingProfileManager to be ready.");
            routingProfileManager.setFailed(true);
            Thread.currentThread().interrupt();
        }
        return routingProfileManager;
    }

    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        String outputTarget = configurationOutputTarget(engineProperties);
        if (!StringUtility.isNullOrEmpty(outputTarget)) {
            copyDefaultConfigurationToFile(outputTarget);
            System.setProperty(SHUTDOWN_IMMEDIATELY, "true");
            return;
        }
        LOGGER.info("Initializing ORS...");
        new Thread(this::initializeORS, "ORS-Init").start();
    }

    public void reloadGraphs() {
        if (!routingProfileManager.isReady()) {
            LOGGER.warn("RoutingProfileManager initialization is in progress, skipping graph reload request.");
            return;
        }
        LOGGER.info("Reloading ORS graphs...");
        new Thread(this::initializeORS, "ORS-Reload").start();
    }

    private void initializeORS() {
        try {
            graphService.setIsActivatingGraphs(true);
            routingProfileManager.initialize();
            routingProfileManager.awaitReady();
            if (routingProfileManager.isShutdown() || routingProfileManager.hasFailed()) {
                System.exit(routingProfileManager.hasFailed() ? 1 : 0);
            }
            for (RoutingProfile profile : routingProfileManager.getUniqueProfiles()) {
                ORSGraphManager orsGraphManager = profile.getGraphhopper().getOrsGraphManager();
                if (orsGraphManager != null && orsGraphManager.useGraphRepository()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[%s] Adding orsGraphManager for profile %s with encoder %s to GraphService".formatted(orsGraphManager.getQualifiedProfileName(), orsGraphManager.getQualifiedProfileName(), profile.getProfileConfiguration().getEncoderName()));
                    }
                    graphService.addGraphManagerInstance(orsGraphManager);
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error("Thread interrupted during ORS initialization.");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOGGER.warn("Unable to initialize ORS due to an unexpected exception: %s".formatted(e.toString()));
        } finally {
            graphService.setIsActivatingGraphs(false);
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
        if (StringUtility.isNullOrEmpty(output)) return null;
        if (!output.endsWith(".yml") && !output.endsWith(".yaml")) output += ".yml";
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

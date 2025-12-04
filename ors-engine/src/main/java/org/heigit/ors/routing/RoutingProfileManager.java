/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.routing;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.util.RuntimeUtility;
import org.heigit.ors.util.TimeUtility;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class RoutingProfileManager {
    private static final Logger LOGGER = Logger.getLogger(RoutingProfileManager.class.getName());
    public static final String KEY_SKIPPED_EXTRA_INFO = "skipped_extra_info";

    // The current initialization task. Replaced for each cycle.
    private FutureTask<RoutingProfileManager> initTask;
    // Lock protecting creation of a new FutureTask.
    private final Object lock = new Object();

    private final EngineProperties engineProperties;
    private LinkedHashMap<String, RoutingProfile> routingProfiles = new LinkedHashMap<>();
    private boolean statusFailed = false;
    private boolean statusShutdown;

    public RoutingProfileManager(EngineProperties engineProperties, boolean shutdownAfterInit) {
        this.engineProperties = engineProperties;
        this.statusShutdown = shutdownAfterInit;
    }

    public void initialize() {
        // If no initialization is in progress or complete, create a new task
        if (initTask == null || initTask.isDone()) {
            synchronized (lock) {
                if (initTask == null || initTask.isDone()) {
                    initTask = new FutureTask<>(this::loadProfiles);
                }
            }
        }
        // Run the task. Only one thread will actually execute it.
        initTask.run();
    }

    private RoutingProfileManager loadProfiles() {
        RuntimeUtility.printRAMInfo("", LOGGER);
        long startTime = System.currentTimeMillis();
        routingProfiles = new LinkedHashMap<>();
        try {
            Map<String, ProfileProperties> profiles = engineProperties.getInitializedActiveProfiles();
            if (profiles.isEmpty()) {
                fail("No profiles configured. Exiting.");
                return this;
            }
            int initializationThreads = engineProperties.getInitThreads();
            LOGGER.info("====> Initializing %d profiles (%d threads) ...".formatted(profiles.size(), initializationThreads));
            RoutingProfileLoadContext loadContext = new RoutingProfileLoadContext();
            ExecutorService executor = Executors.newFixedThreadPool(initializationThreads);
            ExecutorCompletionService<RoutingProfile> compService = new ExecutorCompletionService<>(executor);
            int nTotalTasks = 0;
            for (Map.Entry<String, ProfileProperties> profile : profiles.entrySet()) {
                if (profile.getValue().getProfilesTypes() != null && profile.getValue().getEnabled()) {
                    Callable<RoutingProfile> task = new RoutingProfileLoader(profile.getKey(), profile.getValue(), engineProperties, loadContext);
                    compService.submit(task);
                    nTotalTasks++;
                }
            }
            LOGGER.info("%d profile configurations submitted as tasks.".formatted(nTotalTasks));

            int nCompletedTasks = 0;
            while (nCompletedTasks < nTotalTasks) {
                nCompletedTasks = handleCompletedTasks(compService, nCompletedTasks);
            }
            executor.shutdown();
            loadContext.releaseElevationProviderCacheAfterAllVehicleProfilesHaveBeenProcessed();

            LOGGER.info("Total time: " + TimeUtility.getElapsedTime(startTime, true) + ".");
            LOGGER.info("========================================================================");
            if (statusShutdown) {
                LOGGER.info("Running in preparation mode, all enabled graphs are built, job is done.");
            }
            RuntimeUtility.clearMemory(LOGGER);
            if (LOGGER.isInfoEnabled())
                printStatistics();
        } catch (Exception ex) {
            fail("Exception at RoutingProfileManager initialization: " + ex.getClass() + ": " + ex.getMessage());
            Thread.currentThread().interrupt();
        }
        return this;
    }

    private int handleCompletedTasks(ExecutorCompletionService<RoutingProfile> compService, int nCompletedTasks) throws InterruptedException, ExecutionException {
        Future<RoutingProfile> future = compService.take();
        try {
            RoutingProfile rp = future.get();
            nCompletedTasks++;
            routingProfiles.put(rp.name(), rp);
        } catch (ExecutionException e) {
            LOGGER.debug(e);
            if (ExceptionUtils.indexOfThrowable(e, FileNotFoundException.class) != -1) {
                throw new IllegalStateException("Output files can not be written. Make sure ors.engine.graphs_data_access is set to a writable type! ");
            }
            throw e;
        } catch (InterruptedException e) {
            LOGGER.debug(e);
            Thread.currentThread().interrupt();
            throw e;
        }
        return nCompletedTasks;
    }

    private void printStatistics() {
        LOGGER.info("====> Memory usage by profiles:");
        long totalUsedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long totalProfilesMemory = 0;

        int i = 0;
        for (RoutingProfile profile : getUniqueProfiles()) {
            i++;
            long profileMemory = profile.getMemoryUsage();
            totalProfilesMemory += profileMemory;
            LOGGER.info("[%d] %s (%.1f%%)".formatted(i, RuntimeUtility.getMemorySize(profileMemory), ((double) profileMemory / totalUsedMemory) * 100));
        }
        LOGGER.info("Total: %s (%.1f%%)".formatted(RuntimeUtility.getMemorySize(totalProfilesMemory), ((double) totalProfilesMemory / totalUsedMemory) * 100));
        LOGGER.info("========================================================================");
    }

    public RoutingProfile getRoutingProfile(String profileName) {
        if (!routingProfiles.containsKey(profileName))
            return null;
        return routingProfiles.get(profileName);
    }

    public List<RoutingProfile> getUniqueProfiles() {
        return new ArrayList<>(routingProfiles.values());
    }

    public void destroy() {
        for (RoutingProfile rp : routingProfiles.values()) {
            rp.close();
        }
    }

    private void fail(String message) {
        LOGGER.error("");
        LOGGER.error(message);
        LOGGER.error("");
        statusFailed = true;
        statusShutdown = true;
    }

    public void awaitReady() throws ExecutionException, InterruptedException {
        if (initTask == null) {
            // Nothing initialized yet—ensure we have at least one task before waiting
            initialize();
        }
        initTask.get();
    }

    public boolean isReady() {
        return initTask != null && initTask.isDone();
    }

    public boolean hasFailed() {
        return statusFailed;
    }

    public boolean isShutdown() {
        return statusShutdown;
    }

    public void setFailed(boolean b) {
        this.statusFailed = b;
    }
}

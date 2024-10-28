package utils;

import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static utils.ContainerInitializer.DEFAULT_STARTUP_TIMEOUT;

public class TestContainersHelper {

    public static WaitStrategy simpleLogMessageWaitStrategy(String logLookupMessage) {
        return waitStrategyWithLogMessage(new String[]{logLookupMessage}).withStartupTimeout(DEFAULT_STARTUP_TIMEOUT);
    }

    public static WaitStrategy waitStrategyWithLogMessage(String[] logLookupMessages) {
        WaitAllStrategy waitAllStrategy = new WaitAllStrategy();
        for (String logLookupMessage : logLookupMessages) {
            waitAllStrategy.withStrategy(new LogMessageWaitStrategy().withRegEx(".*" + logLookupMessage + ".*")).withStartupTimeout(DEFAULT_STARTUP_TIMEOUT);
        }
        return waitAllStrategy.withStartupTimeout(DEFAULT_STARTUP_TIMEOUT);
    }

    public static WaitStrategy healthyWaitStrategyWithLogMessage(String[] logLookupMessages, Duration startupTimeout) {
        if (startupTimeout != null) {
            DEFAULT_STARTUP_TIMEOUT = startupTimeout;
        }
        return healthyWaitStrategyWithLogMessage(logLookupMessages);

    }

    public static WaitStrategy healthyWaitStrategyWithLogMessage(String[] logLookupMessages) {
        WaitAllStrategy waitAllStrategy = new WaitAllStrategy();
        for (String logLookupMessage : logLookupMessages) {
            waitAllStrategy.withStrategy(new LogMessageWaitStrategy().withRegEx(".*" + logLookupMessage + ".*")).withStartupTimeout(DEFAULT_STARTUP_TIMEOUT);
        }
        return waitAllStrategy.withStrategy(healthyOrsWaitStrategy()).withStartupTimeout(DEFAULT_STARTUP_TIMEOUT);
    }

    public static WaitStrategy healthyOrsWaitStrategy() {
        //@formatter:off
        return new HttpWaitStrategy()
                .forPort(8080)
                .forStatusCode(200)
                .forPath("/ors/v2/health")
                .withReadTimeout(Duration.ofSeconds(5))
                .withStartupTimeout(DEFAULT_STARTUP_TIMEOUT);
        //@formatter:on
    }

    // Wait strategy that looks for "Loaded file 'ors-config-car.yml'" in the logs and waits for the container to be healthy
    public static WaitStrategy orsCorrectConfigLoadedWaitStrategy(String configName) {
        //@formatter:off
        return new WaitAllStrategy()
                .withStrategy(new LogMessageWaitStrategy().withRegEx(".*Loaded file '" + configName + "'.*")).withStartupTimeout(DEFAULT_STARTUP_TIMEOUT)
                .withStrategy(healthyOrsWaitStrategy()).withStartupTimeout(DEFAULT_STARTUP_TIMEOUT);
        //@formatter:on
    }

    public static void copyFolderContentFromContainer(GenericContainer<?> container, String containerPath, String destinationPath) throws IOException, InterruptedException {
        Container.ExecResult result = container.execInContainer("ls", "-p", "-1", containerPath);
        String[] files = result.getStdout().split("\n");

        for (String fileName : files) {
            if (fileName.trim().isEmpty()) {
                continue;
            }
            if (fileName.endsWith("/")) {
                String folderName = fileName.substring(0, fileName.length() - 1);
                new File(destinationPath, folderName).mkdirs();
                copyFolderContentFromContainer(container, containerPath + "/" + folderName, destinationPath + "/" + folderName);
            } else {
                container.copyFileFromContainer(containerPath + "/" + fileName, destinationPath + "/" + fileName);
            }
        }
    }

    public static boolean waitForLogPatterns(GenericContainer<?> container, List<String> logPatterns, int maxWaitTimeInSeconds, int recheckFrequencyInMillis, boolean expected) throws InterruptedException {
        int elapsedTime = 0;
        while (elapsedTime < maxWaitTimeInSeconds * 1000) {
            boolean allPatternsMatch = logPatterns.stream().allMatch(pattern -> container.getLogs().contains(pattern) == expected);
            if (allPatternsMatch) {
                return true;
            }
            Thread.sleep(recheckFrequencyInMillis);
            elapsedTime += recheckFrequencyInMillis;
        }

        // If we reach here, not all patterns matched the expected presence
        List<String> mismatchedPatterns = logPatterns.stream().filter(pattern -> container.getLogs().contains(pattern) != expected).toList();
        // print mismatched patterns line by line
        System.out.println("Mismatched patterns: ");
        mismatchedPatterns.forEach(System.out::println);

        return false;
    }

    public static boolean waitForFailedGraphActivationInOrsLogs(GenericContainer<?> container, String profilePath, int maxWaitTimeInSeconds, int recheckFrequencyInMillis) throws InterruptedException {
        List<String> logPatterns = List.of(
                "java.util.concurrent.ExecutionException: java.lang.IllegalStateException: Couldn't load from existing folder: " + profilePath + " but also cannot use file for DataReader as it wasn't specified!",
                "ExecutionException while initializing RoutingProfileManager: java.lang.IllegalStateException: Couldn't load from existing folder: " + profilePath + " but also cannot use file for DataReader as it wasn't specified!"
        );
        return waitForLogPatterns(container, logPatterns, maxWaitTimeInSeconds, recheckFrequencyInMillis, true);
    }

    public static boolean waitForEmptyGrcRepoCheck(GenericContainer<?> container, String profile, String encoder_name, String graphRepoName, int maxWaitTimeInSeconds, int recheckFrequencyInMillis, boolean expected) throws InterruptedException {
        List<String> logPatterns = List.of(
                "[" + profile + "] Checking for possible graph update from remote repository...",
                "[" + profile + "] Checking latest graphInfo in remote repository...",
                "[driving-hgv] No graphInfo found in remote repository: /tmp/wrong-filesystem-repo/vendor-xyz/fastisochrones/heidelberg/1/fastisochrones_heidelberg_1_driving-hgv.yml",
                "[" + profile + "] No graphInfo found in remote repository: " + graphRepoName + "/vendor-xyz/fastisochrones/heidelberg/1/fastisochrones_heidelberg_1_" + encoder_name + ".yml",
                "[" + profile + "] No newer graph found in repository.",
                "[" + profile + "] No downloaded graph to extract."
        );
        return waitForLogPatterns(container, logPatterns, maxWaitTimeInSeconds, recheckFrequencyInMillis, expected);
    }

    public static boolean waitForNoNewGraphGrcRepoCheck(GenericContainer<?> container, String profile, String encoder_name, int maxWaitTimeInSeconds, int recheckFrequencyInMillis, boolean expected) throws InterruptedException {
        List<String> logPatterns = List.of(
                "Scheduled repository check...",
                "Scheduled graph activation check...",
                "[" + profile + "] Scheduled repository check: Checking for update.",
                "[" + profile + "] Checking for possible graph update from remote repository...",
                "[" + profile + "] Checking latest graphInfo in remote repository...",
                "[" + profile + "] Downloading fastisochrones_heidelberg_1_" + encoder_name + ".yml...",
                "[" + profile + "] No newer graph found in repository.",
                "[" + profile + "] No downloaded graph to extract.",
                "Scheduled repository check done"
        );
        return waitForLogPatterns(container, logPatterns, maxWaitTimeInSeconds, recheckFrequencyInMillis, expected);
    }

    public static boolean waitForSuccessfulGrcRepoCheckAndActivation(GenericContainer<?> container, String profile, String encoder_name, int maxWaitTimeInSeconds, int recheckFrequencyInMillis, boolean expected) throws InterruptedException {
        List<String> logPatterns = List.of(
                "[" + profile + "] Checking for possible graph update from remote repository...",
                "[" + profile + "] Checking latest graphInfo in remote repository...",
                "[" + profile + "] Downloading fastisochrones_heidelberg_1_" + encoder_name + ".yml...",
                "[" + profile + "] Downloading fastisochrones_heidelberg_1_" + encoder_name + ".ghz...",
                "[" + profile + "] Download finished after",
                "[" + profile + "] Extracting downloaded graph file to /home/ors/openrouteservice/graphs/" + profile + "_new_incomplete",
                "[" + profile + "] Extraction of downloaded graph file finished after",
                "deleting downloaded graph file /home/ors/openrouteservice/graphs/vendor-xyz_fastisochrones_heidelberg_1_" + encoder_name + ".ghz",
                "[" + profile + "] Renaming extraction directory to /home/ors/openrouteservice/graphs/" + profile + "_new",
                "[" + profile + "] Downloaded graph was extracted and will be activated at next graph activation check or application start.",
                "[" + profile + "] Activating extracted downloaded graph.",
                "[1] Profile: '" + profile + "', encoder: '" + encoder_name + "', location: '/home/ors/openrouteservice/graphs/" + profile + "'",
                "Adding orsGraphManager for profile " + profile + " with encoder " + encoder_name + " to GraphService",
                "Started Application in"
        );
        return waitForLogPatterns(container, logPatterns, maxWaitTimeInSeconds, recheckFrequencyInMillis, expected);
    }

    public static boolean waitForSuccessfulGrcRepoInitWithoutExistingGraph(GenericContainer<?> container, String profile, String fileRepoName, int maxWaitTimeInSeconds, int recheckFrequencyInMillis, boolean expected) throws InterruptedException {
        List<String> logPatterns = List.of(
                "[" + profile + "] Creating graph directory /home/ors/openrouteservice/graphs/" + profile,
                "Using FileSystemRepoManager for repoUri " + fileRepoName,
                "[" + profile + "] No local graph or extracted downloaded graph found - trying to download and extract graph from repository"
        );
        return waitForLogPatterns(container, logPatterns, maxWaitTimeInSeconds, recheckFrequencyInMillis, expected);
    }


}

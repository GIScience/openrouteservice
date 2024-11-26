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
import java.util.concurrent.TimeUnit;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;
import static utils.ContainerInitializer.defaultStartupTimeout;

public class TestContainersHelper {

    public static WaitStrategy simpleLogMessageWaitStrategy(String logLookupMessage) {
        return waitStrategyWithLogMessage(new String[]{logLookupMessage}).withStartupTimeout(defaultStartupTimeout);
    }

    public static WaitStrategy waitStrategyWithLogMessage(String[] logLookupMessages) {
        WaitAllStrategy waitAllStrategy = new WaitAllStrategy();
        for (String logLookupMessage : logLookupMessages) {
            waitAllStrategy.withStrategy(new LogMessageWaitStrategy().withRegEx(".*" + logLookupMessage + ".*")).withStartupTimeout(defaultStartupTimeout);
        }
        return waitAllStrategy.withStartupTimeout(defaultStartupTimeout);
    }

    public static WaitStrategy healthyWaitStrategyWithLogMessage(String[] logLookupMessages, Duration startupTimeout) {
        if (startupTimeout != null) {
            defaultStartupTimeout = startupTimeout;
        }
        return healthyWaitStrategyWithLogMessage(logLookupMessages);

    }

    public static WaitStrategy healthyWaitStrategyWithLogMessage(String[] logLookupMessages) {
        WaitAllStrategy waitAllStrategy = new WaitAllStrategy();
        for (String logLookupMessage : logLookupMessages) {
            waitAllStrategy.withStrategy(new LogMessageWaitStrategy().withRegEx(".*" + logLookupMessage + ".*")).withStartupTimeout(defaultStartupTimeout);
        }
        return waitAllStrategy.withStrategy(healthyOrsWaitStrategy()).withStartupTimeout(defaultStartupTimeout);
    }

    public static WaitStrategy healthyOrsWaitStrategy() {
        //@formatter:off
        return new HttpWaitStrategy()
                .forPort(8080)
                .forStatusCode(200)
                .forPath("/ors/v2/health")
                .withReadTimeout(Duration.ofSeconds(5))
                .withStartupTimeout(defaultStartupTimeout);
        //@formatter:on
    }

    // Wait strategy that looks for "Loaded file 'ors-config-car.yml'" in the logs and waits for the container to be healthy
    public static WaitStrategy orsCorrectConfigLoadedWaitStrategy(String configName) {
        //@formatter:off
        return new WaitAllStrategy()
                .withStrategy(new LogMessageWaitStrategy().withRegEx(".*Loaded file '" + configName + "'.*")).withStartupTimeout(defaultStartupTimeout)
                .withStrategy(healthyOrsWaitStrategy()).withStartupTimeout(defaultStartupTimeout);
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

    public static boolean waitForLogPatterns(GenericContainer<?> container, List<String> logPatterns, int maxWaitTimeInSeconds, boolean expected) {

        try {
            await().atMost(maxWaitTimeInSeconds, TimeUnit.SECONDS).until(() ->
                    logPatterns.stream().allMatch(pattern -> container.getLogs().contains(pattern) == expected)
            );
        } catch (Exception e) {
            // If we reach here, not all patterns matched the expected presence
            List<String> mismatchedPatterns = logPatterns.stream().filter(pattern -> container.getLogs().contains(pattern) != expected).toList();
            // print mismatched patterns line by line
            System.out.println("Mismatched patterns: ");
            mismatchedPatterns.forEach(System.out::println);
            return false;
        }
        return true;
    }

    public static boolean waitForFailedGraphActivationInOrsLogs(GenericContainer<?> container, String profilePath, int maxWaitTimeInSeconds) {
        List<String> logPatterns = List.of(
                "java.util.concurrent.ExecutionException: java.lang.IllegalStateException: Couldn't load from existing folder: " + profilePath + " but also cannot use file for DataReader as it wasn't specified!",
                "ExecutionException while initializing RoutingProfileManager: java.lang.IllegalStateException: Couldn't load from existing folder: " + profilePath + " but also cannot use file for DataReader as it wasn't specified!"
        );
        return waitForLogPatterns(container, logPatterns, maxWaitTimeInSeconds, true);
    }

    public static boolean waitForEmptyGrcRepoCheck(GenericContainer<?> container, String profile, String encoderName, String graphRepoName, int maxWaitTimeInSeconds, boolean expected) {
        List<String> logPatterns = List.of(
                "[" + profile + "] Checking for possible graph update from remote repository...",
                "[" + profile + "] Checking latest graphInfo in remote repository...",
                "[driving-hgv] No graphInfo found in remote repository: /tmp/wrong-filesystem-repo/vendor-xyz/fastisochrones/heidelberg/1/fastisochrones_heidelberg_1_driving-hgv.yml",
                "[" + profile + "] No graphInfo found in remote repository: " + graphRepoName + "/vendor-xyz/fastisochrones/heidelberg/1/fastisochrones_heidelberg_1_" + encoderName + ".yml",
                "[" + profile + "] No newer graph found in repository.",
                "[" + profile + "] No downloaded graph to extract."
        );
        return waitForLogPatterns(container, logPatterns, maxWaitTimeInSeconds, expected);
    }

    public static boolean waitForNoNewGraphGrcRepoCheck(GenericContainer<?> container, String profile, String encoderName, int maxWaitTimeInSeconds, boolean expected) {
        List<String> logPatterns = List.of(
                "Scheduled repository check...",
                "Scheduled graph activation check...",
                "[" + profile + "] Scheduled repository check: Checking for update.",
                "[" + profile + "] Checking for possible graph update from remote repository...",
                "[" + profile + "] Checking latest graphInfo in remote repository...",
                "Scheduled graph activation check done: No downloaded graphs found, no graph activation required.",
                "[" + profile + "] Downloading fastisochrones_heidelberg_1_" + encoderName + ".yml...",
                "[" + profile + "] No newer graph found in repository.",
                "[" + profile + "] No downloaded graph to extract.",
                "Scheduled repository check done"
        );
        return waitForLogPatterns(container, logPatterns, maxWaitTimeInSeconds, expected);
    }


    public static boolean waitForSuccessfulGrcRepoCheckAndDownload(GenericContainer<?> container, String profile, String encoderName, int maxWaitTimeInSeconds, boolean expected) {
        List<String> logPatterns = List.of(
                "[" + profile + "] Checking for possible graph update from remote repository...",
                "[" + profile + "] Checking latest graphInfo in remote repository...",
                "[" + profile + "] Downloading fastisochrones_heidelberg_1_" + encoderName + ".yml...",
                "[" + profile + "] Downloading fastisochrones_heidelberg_1_" + encoderName + ".ghz...",
                "[" + profile + "] Download of compressed graph file finished after",
                "[" + profile + "] Extracting downloaded graph file to /home/ors/openrouteservice/graphs/" + profile + "_new_incomplete",
                "[" + profile + "] Extraction of downloaded graph file finished after",
                "deleting downloaded graph file /home/ors/openrouteservice/graphs/vendor-xyz_fastisochrones_heidelberg_1_" + encoderName + ".ghz",
                "[" + profile + "] Renaming extraction directory to /home/ors/openrouteservice/graphs/" + profile + "_new",
                "[" + profile + "] Downloaded graph was extracted and will be activated at next graph activation check or application start."
        );
        return waitForLogPatterns(container, logPatterns, maxWaitTimeInSeconds, expected);
    }

    public static boolean waitForSuccessfulGrcActivationOnFreshGraph(GenericContainer<?> container, String profile, String encoderName, int maxWaitTimeInSeconds, boolean expected) {
        List<String> logPatterns = List.of(
                "[" + profile + "] Activating extracted downloaded graph.",
                "[1] Profile: '" + profile + "', encoder: '" + encoderName + "', location: '/home/ors/openrouteservice/graphs/" + profile + "'",
                "Adding orsGraphManager for profile " + profile + " with encoder " + encoderName + " to GraphService",
                "Started Application in"
        );
        return waitForLogPatterns(container, logPatterns, maxWaitTimeInSeconds, expected);
    }


    public static boolean waitForSuccessfulGrcRepoActivationOnExistingGraph(GenericContainer<?> container, String profile, String encoderName, int maxWaitTimeInSeconds, boolean expected) {
        List<String> logPatterns = List.of(
                "[" + profile + "] Scheduled graph activation check: Downloaded extracted graph available",
                "Scheduled graph activation check done: Performing graph activation...",
                "Using FileSystemGraphRepoClient for repoUri /tmp/test-filesystem-repo",
                "[" + profile + "] Deleted graph-info download file from previous application run: /home/ors/openrouteservice/graphs/vendor-xyz_fastisochrones_heidelberg_1_" + encoderName + ".yml",
                "[" + profile + "] Found local graph and extracted downloaded graph",
                "[" + profile + "] Renamed old local graph directory /home/ors/openrouteservice/graphs/driving-car to /home/ors/openrouteservice/graphs/" + profile + "_",
                "[" + profile + "] Activating extracted downloaded graph.",
                "[2] Profile: '" + profile + "', encoder: '" + encoderName + "', location: '/home/ors/openrouteservice/graphs/" + profile + "'.",
                "[" + profile + "] Adding orsGraphManager for profile " + profile + " with encoder " + encoderName + " to GraphService"
        );
        return waitForLogPatterns(container, logPatterns, maxWaitTimeInSeconds, expected);
    }

    public static boolean waitForSuccessfulGrcRepoInitWithoutExistingGraph(GenericContainer<?> container, String profile, String fileRepoName, int maxWaitTimeInSeconds, boolean expected) {
        List<String> logPatterns = List.of(
                "[" + profile + "] Creating graph directory /home/ors/openrouteservice/graphs/" + profile,
                "Using FileSystemGraphRepoClient for repoUri " + fileRepoName,
                "[" + profile + "] No local graph or extracted downloaded graph found - trying to download and extract graph from repository"
        );
        return waitForLogPatterns(container, logPatterns, maxWaitTimeInSeconds, expected);
    }

    public static boolean waitForSuccessfulGrcRepoInitWithExistingGraph(GenericContainer<?> container, String profile, String encoderName, String fileRepoName, int maxWaitTimeInSeconds, boolean expected) {
        List<String> logPatterns = List.of(
                "Using FileSystemGraphRepoClient for repoUri " + fileRepoName,
                "[" + profile + "] Found local graph only",
                "[1] Profile: '" + profile + "', encoder: '" + encoderName + "', location: '/home/ors/openrouteservice/graphs/" + profile + "'",
                "Adding orsGraphManager for profile " + profile + " with encoder " + encoderName + " to GraphService",
                "Started Application in"
        );
        return waitForLogPatterns(container, logPatterns, maxWaitTimeInSeconds, expected);
    }


}

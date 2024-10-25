package utils;

import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class TestContainersHelper {
    public static WaitStrategy noConfigFailWaitStrategy() {
        return new LogMessageWaitStrategy().withRegEx(".*No profiles configured. Exiting.*");
    }

    public static WaitStrategy simpleLogMessageWaitStrategy(String logLookupMessage) {
        return new LogMessageWaitStrategy().withRegEx(".*" + logLookupMessage + ".*");
    }

    public static WaitStrategy noConfigHealthyWaitStrategy(String logLookupMessage) {
        return new WaitAllStrategy().withStrategy(new LogMessageWaitStrategy().withRegEx(".*" + logLookupMessage + ".*")).withStrategy(healthyOrsWaitStrategy());
    }

    public static WaitStrategy noConfigHealthyWaitStrategy(String[] logLookupMessages) {
        WaitAllStrategy waitAllStrategy = new WaitAllStrategy();
        for (String logLookupMessage : logLookupMessages) {
            waitAllStrategy.withStrategy(new LogMessageWaitStrategy().withRegEx(".*" + logLookupMessage + ".*"));
        }
        return waitAllStrategy.withStrategy(healthyOrsWaitStrategy());
    }

    public static WaitStrategy healthyOrsWaitStrategy() {
        //@formatter:off
        return new HttpWaitStrategy()
                .forPort(8080)
                .forStatusCode(200)
                .forPath("/ors/v2/health")
                .withReadTimeout(Duration.ofSeconds(5))
                .withStartupTimeout(Duration.ofSeconds(100));
    }

    // Wait strategy that looks for "Loaded file 'ors-config-car.yml'" in the logs and waits for the container to be healthy
    public static WaitStrategy orsCorrectConfigLoadedWaitStrategy(String configName) {
        //@formatter:off
        return new WaitAllStrategy()
                .withStrategy(new LogMessageWaitStrategy().withRegEx(".*Loaded file '" + configName + "'.*"))
                .withStrategy(healthyOrsWaitStrategy());
    }

    /**
     * Restarts the container with the provided files. TestContainers doesn't support a normal docker restart and stop/start is the only way to restart a container.
     * The downside is that stop/start will create a fresh container from the original image.
     * Therefore, we preserve files with a FileSystemBind.
     *
     * @param container     The container to restart
     * @param preserveFiles A map of containerPath -> hostPath to preserve files
     */
    public static void restartContainer(GenericContainer<?> container, Map<String, Path> preserveFiles) {
        // deep copy
        if (preserveFiles != null) {
            for (Map.Entry<String, Path> entry : preserveFiles.entrySet()) {
                String containerPath = entry.getKey();
                Path hostPath = entry.getValue();
                container.copyFileFromContainer(containerPath, hostPath.toString());
                container.withCopyFileToContainer(MountableFile.forHostPath(hostPath), containerPath);
            }
        }
        restartContainer(container);
    }

    public static void restartContainer(GenericContainer<?> container) {
        container.stop();
        container.start();
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

        System.out.println("Mismatched patterns: " + mismatchedPatterns);

        return false;
    }

}

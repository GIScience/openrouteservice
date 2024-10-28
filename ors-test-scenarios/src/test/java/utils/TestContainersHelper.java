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
    }

    // Wait strategy that looks for "Loaded file 'ors-config-car.yml'" in the logs and waits for the container to be healthy
    public static WaitStrategy orsCorrectConfigLoadedWaitStrategy(String configName) {
        //@formatter:off
        return new WaitAllStrategy()
                .withStrategy(new LogMessageWaitStrategy().withRegEx(".*Loaded file '" + configName + "'.*")).withStartupTimeout(DEFAULT_STARTUP_TIMEOUT)
                .withStrategy(healthyOrsWaitStrategy()).withStartupTimeout(DEFAULT_STARTUP_TIMEOUT);
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

}

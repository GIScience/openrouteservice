package utils;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class TestContainersHelper {
    public static WaitStrategy noConfigWaitStrategy() {
        return new LogMessageWaitStrategy().withRegEx(".*No profiles configured. Exiting.*");
    }

    public static WaitStrategy noConfigHealthyWaitStrategy(String logLookupMessage) {
        return new WaitAllStrategy()
                .withStrategy(new LogMessageWaitStrategy().withRegEx(".*" + logLookupMessage + ".*"))
                .withStrategy(healthyOrsWaitStrategy());
    }

    public static WaitStrategy healthyOrsWaitStrategy() {
        //@formatter:off
        return new HttpWaitStrategy()
                .forPort(8080)
                .forStatusCode(200)
                .forPath("/ors/v2/health")
                .withStartupTimeout(Duration.ofSeconds(80));
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

    /**
     * Restarts the container and resets the binds to the original binds.
     * @param container The container to restart
     * @param resetPreservedFiles Whether to reset the preserved files
     */
    public static void resetContainer(GenericContainer<?> container, Boolean resetPreservedFiles, Boolean restartContainer) {
        if (resetPreservedFiles) {
            container.setCopyToFileContainerPathMap(new HashMap<>());
        }
        if (restartContainer) {
            restartContainer(container);
        }
    }

    public static void restartContainer(GenericContainer<?> container) {
        container.stop();
        container.start();
    }

}

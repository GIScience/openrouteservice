package utils;

import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class ContainerInitializer {
    private static final Map<String, String> defaultEnv = Map.of("logging.level.org.heigit",
            "DEBUG",
            "ors.engine.graphs_data_access",
            "MMAP",
            "server.port",
            "8080"
    );
    private static List<ContainerTestImage> selectedContainers = List.of();
    private static GenericContainer<?> warContainer;
    private static GenericContainer<?> jarContainer;
    private static GenericContainer<?> mvnContainer;

    static {
        initializeContainers();
    }

    public static void initializeContainers() {
        String containerValue = System.getenv("CONTAINER_SCENARIO");
        if (containerValue == null) {
            containerValue = "all";
        }
        selectedContainers = switch (containerValue) {
            case "war" -> List.of(ContainerTestImage.WAR_CONTAINER);
            case "maven" -> List.of(ContainerTestImage.MAVEN_CONTAINER);
            case "jar" -> List.of(ContainerTestImage.JAR_CONTAINER);
            default ->
                    List.of(ContainerTestImage.JAR_CONTAINER, ContainerTestImage.WAR_CONTAINER, ContainerTestImage.MAVEN_CONTAINER);
        };

        Startables.deepStart(
                selectedContainers.stream()
                        .map(container -> initContainer(container, false, false))
                        .toArray(GenericContainer[]::new)
        ).join();
    }

    public static Stream<Object[]> imageStream() {
        // Check selectedContainers and return a stream of ContainerTestImage Enum objects
        return Stream.of(selectedContainers)
                .flatMap(List::stream)
                .map(container -> new Object[]{container});
    }

    public static GenericContainer<?> initContainer(ContainerTestImage containerTestImage) {
        return initContainer(containerTestImage, false, true);
    }

    public static GenericContainer<?> initContainer(ContainerTestImage containerTestImage, Boolean recreate, Boolean autoStart) {
        if (containerTestImage == null) {
            throw new IllegalArgumentException("containerTestImage must not be null");
        }
        //@formatter:off
        WaitStrategy waitStrategy = new HttpWaitStrategy()
                .forPort(8080)
                .forStatusCode(200)
                .forPath("/ors/v2/health")
                .withStartupTimeout(Duration.ofSeconds(80));
        //@formatter:off
        GenericContainer<?> container = new GenericContainer<>(
                new ImageFromDockerfile(containerTestImage.getName(), false)
                        .withFileFromPath("ors-api", Path.of("../ors-api"))
                        .withFileFromPath("ors-engine", Path.of("../ors-engine"))
                        .withFileFromPath("ors-report-aggregation", Path.of("../ors-report-aggregation"))
                        .withFileFromPath("pom.xml", Path.of("../pom.xml"))
                        .withFileFromPath("ors-config.yml", Path.of("../ors-config.yml"))
                        .withFileFromPath("Dockerfile", Path.of("../ors-test-scenarios/src/test/resources/Dockerfile"))
                        .withFileFromPath(".dockerignore", Path.of("../.dockerignore"))
                        .withTarget(containerTestImage.getName())
        )
                .withEnv(defaultEnv)
                .withFileSystemBind("./graphs-integrationtests/" + containerTestImage.getName(),
                        "/home/ors/openrouteservice/graphs", BindMode.READ_WRITE)
                .withExposedPorts(8080)
                .withLogConsumer(outputFrame -> System.out.print(outputFrame.getUtf8String()))
                .waitingFor(waitStrategy);

        if (containerTestImage == ContainerTestImage.WAR_CONTAINER) {
            if (warContainer == null || recreate) {
                if (warContainer != null && recreate) {
                    warContainer.stop();
                    }
                warContainer = container;
            }
            if (autoStart && !warContainer.isRunning()) {
                warContainer.start();
            }
            return warContainer;
        } else if (containerTestImage == ContainerTestImage.JAR_CONTAINER) {
            if (jarContainer == null || recreate) {
                if (jarContainer != null && recreate) {
                    jarContainer.stop();
                    }
                jarContainer = container;
            }
            if (autoStart && !jarContainer.isRunning())
                jarContainer.start();
            return jarContainer;
        } else {
            if (mvnContainer == null || recreate) {
                if (mvnContainer != null && recreate) {
                    mvnContainer.stop();
                    }
                mvnContainer = container;
            }
            if (autoStart && !mvnContainer.isRunning())
                mvnContainer.start();
            return mvnContainer;
        }
    }

/**
     * Restarts the container with the provided files. TestContainers doesn't support a normal docker restart and stop/start is the only way to restart a container.
     * The downside is that stop/start will create a fresh container from the original image.
     * Therefore, we preserve files with a FileSystemBind.
     *
     * @param container     The container to restart
     * @param preserveFiles A map of containerPath -> hostPath to preserve files
     * @return The original binds of the container for a later reset
     */
    protected void restartContainer(GenericContainer<?> container, Map<String, Path> preserveFiles) {
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
    protected void restartContainer(GenericContainer<?> container, Boolean resetPreservedFiles) {
        if (resetPreservedFiles) {
            container.setCopyToFileContainerPathMap(new HashMap<>());
        }
        restartContainer(container);
    }

    protected void restartContainer(GenericContainer<?> container) {
        container.stop();
        container.start();
    }

    @BeforeEach
    public void resetEnv() {
        if (warContainer != null) {
            warContainer.setEnv(List.of());
            warContainer.withEnv(defaultEnv);
        }
        if (jarContainer != null) {
            jarContainer.setEnv(List.of());
            jarContainer.withEnv(defaultEnv);
        }
    }

    // Create enum for available test images
    public enum ContainerTestImage {
        WAR_CONTAINER("ors-test-scenarios-war"),
        JAR_CONTAINER("ors-test-scenarios-jar"),
        MAVEN_CONTAINER("ors-test-scenarios-maven");

        private final String name;

        ContainerTestImage(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
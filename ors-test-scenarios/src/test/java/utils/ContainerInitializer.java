package utils;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static utils.TestContainersHelper.healthyOrsWaitStrategy;

/**
 * Abstract class for initializing and managing TestContainers.
 */
public abstract class ContainerInitializer {
    // @formatter:off
    private static final Map<String, String> defaultEnv = Map.of(
            "logging.level.org.heigit", "DEBUG",
            "ors.engine.graphs_data_access", "MMAP",
            "server.port", "8080"
    );

    private static List<ContainerTestImageDefaults> selectedDefaultContainers = List.of();
    private static List<ContainerTestImageBare> selectedBareContainers = List.of();

    /**
     * Initializes the containers based on the environment variable `CONTAINER_SCENARIO`.
     *
     * @param startDefaultContainers Whether to start the default containers.
     */
    public static void initializeContainers(Boolean startDefaultContainers) {
        String containerValue = System.getenv("CONTAINER_SCENARIO");
        if (containerValue == null) {
            containerValue = "all";
        }
        switch (containerValue) {
            case "war":
                selectedDefaultContainers = List.of(ContainerTestImageDefaults.WAR_CONTAINER);
                break;
            case "jar":
                selectedDefaultContainers = List.of(ContainerTestImageDefaults.JAR_CONTAINER);
                selectedBareContainers = List.of(ContainerTestImageBare.JAR_CONTAINER_BARE);
                break;
            case "maven":
                selectedDefaultContainers = List.of(ContainerTestImageDefaults.MAVEN_CONTAINER);
                selectedBareContainers = List.of(ContainerTestImageBare.MAVEN_CONTAINER_BARE);
                break;
            default:
                // @formatter:off
                selectedDefaultContainers = List.of(
                        ContainerTestImageDefaults.WAR_CONTAINER,
                        ContainerTestImageDefaults.JAR_CONTAINER,
                        ContainerTestImageDefaults.MAVEN_CONTAINER
                );
                selectedBareContainers = List.of(
                        ContainerTestImageBare.JAR_CONTAINER_BARE,
                        ContainerTestImageBare.MAVEN_CONTAINER_BARE
                );
                break;
        }
    }

    /**
     * Provides a stream of default container test images for unit tests.
     *
     * @return A stream of default container test images.
     */
    public static Stream<ContainerTestImage[]> ContainerTestImageDefaultsImageStream() {
        initializeContainers(false);
        return selectedDefaultContainers.stream().map(container -> new ContainerTestImage[]{container});
    }

    /**
     * Provides a stream of bare container test images for unit tests.
     *
     * @return A stream of bare container test images.
     */
    public static Stream<ContainerTestImage[]> ContainerTestImageBareImageStream() {
        initializeContainers(false);
        return selectedBareContainers.stream().map(container -> new ContainerTestImage[]{container});
    }

    /**
     * Initializes a container with the given test image, with options to recreate and auto-start.
     *
     * @param containerTestImage The container test image.
     * @param autoStart          Whether to auto-start the container.
     * @return The initialized container.
     */
    public static GenericContainer<?> initContainer(ContainerTestImage containerTestImage, Boolean autoStart) {
        if (containerTestImage == null) {
            throw new IllegalArgumentException("containerTestImage must not be null");
        }

        // @formatter:off
        GenericContainer<?> container = new GenericContainer<>(
                new ImageFromDockerfile(containerTestImage.getName(), false)
                        .withFileFromPath("ors-api", Path.of("../ors-api"))
                        .withFileFromPath("ors-engine", Path.of("../ors-engine"))
                        .withFileFromPath("ors-report-aggregation", Path.of("../ors-report-aggregation"))
                        .withFileFromPath("pom.xml", Path.of("../pom.xml"))
                        .withFileFromPath("ors-config.yml", Path.of("../ors-config.yml"))
                        .withFileFromPath("Dockerfile", Path.of("../ors-test-scenarios/src/test/resources/Dockerfile"))
                        .withFileFromPath(".dockerignore", Path.of("../.dockerignore"))
                        // Special case for maven container entrypoint. This is not needed for the other containers.
                        .withFileFromPath("./ors-test-scenarios/src/test/resources/maven-entrypoint.sh", Path.of("./src/test/resources/maven-entrypoint.sh"))
                        .withTarget(containerTestImage.getName())
        )
                .withEnv(defaultEnv)
                .withFileSystemBind("./graphs-integrationtests/" + containerTestImage.getName(),
                        "/home/ors/openrouteservice/graphs", BindMode.READ_WRITE)
                .withExposedPorts(8080)
                .withLogConsumer(outputFrame -> System.out.print(outputFrame.getUtf8String()))
                .waitingFor(healthyOrsWaitStrategy());

        if (containerTestImage == ContainerTestImageDefaults.MAVEN_CONTAINER || containerTestImage == ContainerTestImageBare.MAVEN_CONTAINER_BARE) {
            container.withStartupTimeout(Duration.ofSeconds(120));
        } else {
            container.withStartupTimeout(Duration.ofSeconds(80));
        }

        if (autoStart) {
            container.start();
        }

        return container;
    }

    /**
     * Enum representing default container test images.
     */
    public enum ContainerTestImageDefaults implements ContainerTestImage {
        WAR_CONTAINER("ors-test-scenarios-war"),
        JAR_CONTAINER("ors-test-scenarios-jar"),
        MAVEN_CONTAINER("ors-test-scenarios-maven");

        private final String name;

        ContainerTestImageDefaults(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Enum representing bare container test images.
     * These can be adjusted to fit specific CMD requirements.
     */
    public enum ContainerTestImageBare implements ContainerTestImage {
        // WAR_CONTAINER_BARE("ors-test-scenarios-war-bare"), // War works different. The default CMD is hardcoded to catalina.sh run.
        JAR_CONTAINER_BARE("ors-test-scenarios-jar-bare"),
        MAVEN_CONTAINER_BARE("ors-test-scenarios-maven-bare");

        private final String name;

        ContainerTestImageBare(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public ArrayList<String> getCommand(String xmx) {
            ArrayList<String> command = new ArrayList<>();
            switch (this) {
                case JAR_CONTAINER_BARE:
                    command.add("java");
                    command.add("-Xmx" + xmx);
                    command.add("-jar");
                    command.add("ors.jar");
                    break;
                case MAVEN_CONTAINER_BARE:
                    command.add("mvn");
                    command.add("spring-boot:run");
                    command.add("-Dspring-boot.run.jvmArguments=-Xmx" + xmx);
                    command.add("-DskipTests");
                    command.add("-Dmaven.test.skip=true");
                    command.add("-T 1C");
                    break;
            }
            return command;
        }
    }

    /**
     * Interface representing a container test image.
     * Allows for generic usage  of the inheriting enums.
     */
    public interface ContainerTestImage {
        String getName();
    }
}
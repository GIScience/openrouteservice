package utils;

import lombok.Getter;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
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
            "server.port", "8080",
            "ors.engine.elevation.profile_default.build.elevation", "false"
    );
    private static final Map<String, String> defaultEnvWarBare = Map.of(
            "ors.engine.profile_default.graph_path", "/home/ors/openrouteservice/graphs",
            "ors.engine.profile_default.build.source_file", "/home/ors/openrouteservice/files/heidelberg.test.pbf",
            "logging.file.name", "/home/ors/openrouteservice/logs/ors.log"
    );
    // @formatter:on

    private static final String hostSharedGraphPath = "sharedGraphMount";
    public static Duration DEFAULT_STARTUP_TIMEOUT = Duration.ofSeconds(150);
    private static boolean shareGraphsWithContainer = true;
    private static List<ContainerTestImageDefaults> selectedDefaultContainers = List.of();
    private static List<ContainerTestImageBare> selectedBareContainers = List.of();

    /**
     * Initializes the containers based on the environment variable `CONTAINER_SCENARIO`.
     */
    public static void initializeContainers() {
        String containerValue = System.getenv("CONTAINER_SCENARIO");
        if (System.getenv("CONTAINER_SHARE_GRAPHS") != null)
            shareGraphsWithContainer = Boolean.parseBoolean(System.getenv("CONTAINER_SHARE_GRAPHS"));
        if (shareGraphsWithContainer)
            DEFAULT_STARTUP_TIMEOUT = Duration.ofSeconds(50);
        if (containerValue == null) {
            containerValue = "all";
        }
        switch (containerValue) {
            case "war":
                selectedDefaultContainers = List.of(ContainerTestImageDefaults.WAR_CONTAINER);
                selectedBareContainers = List.of(ContainerTestImageBare.WAR_CONTAINER_BARE);
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
                        ContainerTestImageBare.MAVEN_CONTAINER_BARE,
                        ContainerTestImageBare.WAR_CONTAINER_BARE
                );
                // @formatter:on
                break;
        }
    }

    /**
     * Provides a stream of default container test images for unit tests.
     *
     * @return A stream of default container test images.
     */
    public static Stream<ContainerTestImage[]> ContainerTestImageDefaultsImageStream() {
        initializeContainers();
        return selectedDefaultContainers.stream().map(container -> new ContainerTestImage[]{container});
    }

    /**
     * Provides a stream of bare container test images for unit tests.
     *
     * @return A stream of bare container test images.
     */
    public static Stream<ContainerTestImage[]> ContainerTestImageBareImageStream() {
        initializeContainers();
        return selectedBareContainers.stream().map(container -> new ContainerTestImage[]{container});
    }

    public static GenericContainer<?> initContainerWithSharedGraphs(ContainerTestImage containerTestImage, Boolean autoStart) {
        return initContainer(containerTestImage, autoStart, hostSharedGraphPath, shareGraphsWithContainer);
    }

    public static GenericContainer<?> initContainer(ContainerTestImage containerTestImage, Boolean autoStart, String graphMountSubPath) {
        return initContainer(containerTestImage, autoStart, graphMountSubPath, shareGraphsWithContainer);
    }


    /**
     * Initializes a container with the given test image, with options to recreate and auto-start.
     *
     * @param containerTestImage The container test image.
     * @param autoStart          Whether to auto-start the container.
     * @param graphMountSubPath  The subpath to mount the graph. This differentiates the graph mount path for each container. If null, no graph is mounted.
     * @param usePreBuildGraph   Whether to use a pre-built graph. If true graphMountSubPath is ignored and set to the hostSharedGraphPath.
     * @return The initialized container.
     */
    public static GenericContainer<?> initContainer(ContainerTestImage containerTestImage, Boolean autoStart, String graphMountSubPath, Boolean usePreBuildGraph) {
        if (containerTestImage == null) {
            throw new IllegalArgumentException("containerTestImage must not be null");
        }
        // @formatter:off
        Path rootPath = Path.of("../");
        GenericContainer<?> container = new GenericContainer<>(
                new ImageFromDockerfile(containerTestImage.getName(), false)
                        // Specify the copies explicitly to avoid copying the whole project
                        .withFileFromPath("Dockerfile", rootPath.resolve("ors-test-scenarios/src/test/resources/Dockerfile"))
                        .withFileFromPath("pom.xml", rootPath.resolve("pom.xml"))
                        .withFileFromPath("ors-api/pom.xml", rootPath.resolve("ors-api/pom.xml"))
                        .withFileFromPath("ors-engine/pom.xml", rootPath.resolve("ors-engine/pom.xml"))
                        .withFileFromPath("ors-report-aggregation/pom.xml", rootPath.resolve("ors-report-aggregation/pom.xml"))
                        .withFileFromPath("ors-test-scenarios/pom.xml", rootPath.resolve("ors-test-scenarios/pom.xml"))
                        .withFileFromPath("ors-engine/src/main", rootPath.resolve("ors-engine/src/main"))
                        .withFileFromPath("ors-api/src/main", rootPath.resolve("ors-api/src/main"))
                        .withFileFromPath("ors-api/src/test/files/heidelberg.test.pbf", rootPath.resolve("ors-api/src/test/files/heidelberg.test.pbf"))
                        .withFileFromPath("ors-api/src/test/files/vrn_gtfs_cut.zip", rootPath.resolve("ors-api/src/test/files/vrn_gtfs_cut.zip"))
                        .withFileFromPath("ors-config.yml", rootPath.resolve("ors-config.yml"))
                        .withFileFromPath(".dockerignore", rootPath.resolve(".dockerignore"))
                        // Special case for maven container entrypoint. This is not needed for the other containers.
                        .withFileFromPath("./ors-test-scenarios/src/test/resources/maven-entrypoint.sh", Path.of("./src/test/resources/maven-entrypoint.sh"))
                        .withTarget(containerTestImage.getName())
        )
                .withEnv(defaultEnv)
                .withExposedPorts(8080)
                .withStartupTimeout(DEFAULT_STARTUP_TIMEOUT)
                .waitingFor(healthyOrsWaitStrategy());
        // @formatter:on

        if (containerTestImage == ContainerTestImageBare.WAR_CONTAINER_BARE) {
            container.withEnv(defaultEnvWarBare);
        }

        // Set the graph mount path
        Path hostGraphPath = null;
        if (usePreBuildGraph) {
            container.withCopyFileToContainer(MountableFile.forHostPath(Path.of("./graphs-integrationtests/").resolve(hostSharedGraphPath)), "/home/ors/openrouteservice/graphs");
        } else if (graphMountSubPath != null && !graphMountSubPath.isEmpty()) {
            hostGraphPath = Path.of("./graphs-integrationtests/").resolve(graphMountSubPath.equals(hostSharedGraphPath) ? graphMountSubPath : graphMountSubPath + "/" + containerTestImage.getName());
        }

        if (hostGraphPath != null) {
            container.withFileSystemBind(hostGraphPath.toAbsolutePath().toString(), "/home/ors/openrouteservice/graphs", BindMode.READ_WRITE);
            // Create folder if it does not exist
            if (!hostGraphPath.toFile().exists()) {
                hostGraphPath.toFile().mkdirs();
            }
        }

        if (autoStart) {
            container.start();
        }
        return container;
    }

    @BeforeAll
    public static void buildSharedLayersAndGraphs() throws IOException, InterruptedException {
        // Build the shared layers
        // The jar and maven container do not contain any major layers. The heavy one is in war.
        // The build graphs are shared between all containers that are configured to use them.
        GenericContainer<?> containerWar = initContainer(ContainerTestImageBare.WAR_CONTAINER_BARE, false, hostSharedGraphPath, false);
        containerWar.withStartupTimeout(Duration.ofSeconds(300));
        containerWar.addEnv("ors.engine.profile_default.enabled", "true");
        containerWar.addEnv("ors.engine.profiles.public-transport.enabled", "false");
        containerWar.addEnv("ors.engine.graphs_data_access", "MMAP");
        containerWar.addEnv("ors.engine.profile_default.graph_path", "/home/ors/openrouteservice/graphs");
        containerWar.addEnv("ors.engine.profile_default.build.source_file", "/home/ors/openrouteservice/files/heidelberg.test.pbf");
        containerWar.setCommand(ContainerTestImageBare.WAR_CONTAINER_BARE.getCommand("500M").toArray(new String[0]));

        containerWar.start();
        // set read write execute permissions for all
        containerWar.execInContainer("chmod", "-R", "777", "/home/ors/openrouteservice/graphs");
        containerWar.stop();
    }

    /**
     * Enum representing default container test images.
     */
    @Getter
    public enum ContainerTestImageDefaults implements ContainerTestImage {
        WAR_CONTAINER("ors-test-scenarios-war"),
        JAR_CONTAINER("ors-test-scenarios-jar"),
        MAVEN_CONTAINER("ors-test-scenarios-maven");

        private final String name;

        ContainerTestImageDefaults(String name) {
            this.name = name;
        }
    }

    /**
     * Enum representing bare container test images.
     * These can be adjusted to fit specific CMD requirements.
     */
    @Getter
    public enum ContainerTestImageBare implements ContainerTestImage {
        WAR_CONTAINER_BARE("ors-test-scenarios-war-bare"), // War works different. The default CMD is hardcoded to catalina.sh run.
        JAR_CONTAINER_BARE("ors-test-scenarios-jar-bare"),
        MAVEN_CONTAINER_BARE("ors-test-scenarios-maven-bare");

        private final String name;

        ContainerTestImageBare(String name) {
            this.name = name;
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
                    command.add("-o");
                    command.add("-pl");
                    command.add("!:ors-test-scenarios,!:ors-report-aggregation,!:ors-engine");
                    command.add("-Dspring-boot.run.jvmArguments=-Xmx" + xmx);
                    command.add("-DskipTests");
                    command.add("-Dmaven.test.skip=true");
                    break;
                default:
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
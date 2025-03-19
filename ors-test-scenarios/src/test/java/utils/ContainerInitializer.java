package utils;

import lombok.Getter;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static utils.TestContainersHelper.healthyOrsWaitStrategy;

/**
 * Abstract class for initializing and managing TestContainers.
 */
public abstract class ContainerInitializer {
    private static final ch.qos.logback.classic.Logger parentLogger =
            (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ContainerInitializer.class);

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

    private static final Path hostSharedGraphPath = Path.of("./graphs-integrationtests/").resolve("sharedGraphMount");
    public static Duration defaultStartupTimeout = Duration.ofSeconds(180);
    private static boolean shareGraphsWithContainer = true;
    private static List<ContainerTestImageDefaults> selectedDefaultContainers = List.of();
    private static List<ContainerTestImageBare> selectedBareContainers = List.of();
    private static String containerValue = "all";

    /**
     * Initializes the containers based on the environment variable `CONTAINER_SCENARIO`.
     */
    public static void initializeContainers() {
        containerValue = System.getProperty("container.run.scenario", "all");
        parentLogger.info("Container scenario: {}", containerValue);
        shareGraphsWithContainer = Boolean.parseBoolean(System.getProperty("container.run.share_graphs", "true"));
        parentLogger.info("Share graphs with container: {}", shareGraphsWithContainer);
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

    public static GenericContainer<?> initContainer(ContainerTestImage containerTestImage, Boolean autoStart, String graphMountSubPath) {
        return initContainer(containerTestImage, autoStart, graphMountSubPath, shareGraphsWithContainer);
    }

    public static GenericContainer<?> initContainer(ContainerTestImage containerTestImage, Boolean autoStart, String graphMountSubPath, Boolean usePreBuildGraph) {
        return initContainer(containerTestImage, autoStart, graphMountSubPath, usePreBuildGraph, defaultStartupTimeout);
    }

    private static ImageFromDockerfile createImage(ContainerTestImage imageName, String dockerFileName, Boolean deleteOnExit) {
        Path rootPath = Path.of("../");
        // @formatter:off
        return new ImageFromDockerfile(imageName.getName(), deleteOnExit)
                // Specify the copies explicitly to avoid copying the whole project
                .withFileFromPath("Dockerfile", rootPath.resolve("ors-test-scenarios/src/test/resources/").resolve(dockerFileName))
                .withFileFromPath("pom.xml", rootPath.resolve("pom.xml"))
                .withFileFromPath("ors-api/pom.xml", rootPath.resolve("ors-api/pom.xml"))
                .withFileFromPath("ors-engine/pom.xml", rootPath.resolve("ors-engine/pom.xml"))
                .withFileFromPath("ors-report-aggregation/pom.xml", rootPath.resolve("ors-report-aggregation/pom.xml"))
                .withFileFromPath("ors-test-scenarios/pom.xml", rootPath.resolve("ors-test-scenarios/pom.xml"))
                .withFileFromPath("ors-benchmark/pom.xml", rootPath.resolve("ors-benchmark/pom.xml"))
                .withFileFromPath("ors-engine/src/main", rootPath.resolve("ors-engine/src/main"))
                .withFileFromPath("ors-api/src/main", rootPath.resolve("ors-api/src/main"))
                .withFileFromPath("ors-api/src/test/files/heidelberg.test.pbf", rootPath.resolve("ors-api/src/test/files/heidelberg.test.pbf"))
                .withFileFromPath("ors-api/src/test/files/vrn_gtfs_cut.zip", rootPath.resolve("ors-api/src/test/files/vrn_gtfs_cut.zip"))
                .withFileFromPath("ors-config.yml", rootPath.resolve("ors-config.yml"))
                .withFileFromPath(".dockerignore", rootPath.resolve("ors-test-scenarios/src/test/resources/.dockerignore"))
                // Special case for maven container entrypoint.
                .withFileFromPath("ors-test-scenarios/src/test/resources/maven-entrypoint.sh", rootPath.resolve("ors-test-scenarios/src/test/resources/maven-entrypoint.sh"))
                .withTarget(imageName.getName());
        // @formatter:on
    }

    /**
     * The reason for a separate builder image is to avoid the need to build the image every time a container is started.
     * Testcontainers modifies the dockerfiles when something down the road changes from the beginning on, resulting in many unnecessary rebuilds.
     * The two referenced builders are very heavy and it's better to separate them.
     *
     * @param builderName The builder image to initialize.
     */
    static void initBuilderImage(ContainerTestImage builderName) {
        createImage(builderName, "Builder.Dockerfile", false).get();
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
    public static GenericContainer<?> initContainer(ContainerTestImage containerTestImage, Boolean autoStart, String graphMountSubPath, Boolean usePreBuildGraph, Duration startupTimeout) {
        if (containerTestImage == null) {
            throw new IllegalArgumentException("containerTestImage must not be null");
        }

        if (startupTimeout == null) {
            startupTimeout = defaultStartupTimeout;
        }

        String dockerFile = null;
        if (containerTestImage == ContainerTestImageBare.WAR_CONTAINER_BARE || containerTestImage == ContainerTestImageDefaults.WAR_CONTAINER) {
            dockerFile = "war.Dockerfile";
        } else if (containerTestImage == ContainerTestImageBare.MAVEN_CONTAINER_BARE || containerTestImage == ContainerTestImageDefaults.MAVEN_CONTAINER) {
            dockerFile = "maven.Dockerfile";
        } else if (containerTestImage == ContainerTestImageBare.JAR_CONTAINER_BARE || containerTestImage == ContainerTestImageDefaults.JAR_CONTAINER) {
            dockerFile = "jar.Dockerfile";
        } else {
            parentLogger.error("Container image not recognized. Exiting.");
            System.exit(1);
        }

        // @formatter:off
        GenericContainer<?> container = new GenericContainer<>(createImage(containerTestImage, dockerFile, true))
                .withEnv(defaultEnv)
                .withExposedPorts(8080)
                .withStartupTimeout(startupTimeout)
                .withStartupAttempts(3)
                .waitingFor(healthyOrsWaitStrategy());
        // @formatter:on

        if (containerTestImage == ContainerTestImageBare.WAR_CONTAINER_BARE) {
            container.withEnv(defaultEnvWarBare);
        }

        // Set the graph mount path
        Path hostGraphPath = null;
        if (usePreBuildGraph) {
            container.withCopyFileToContainer(MountableFile.forHostPath(hostSharedGraphPath), "/home/ors/openrouteservice/graphs");
        } else if (graphMountSubPath != null && !graphMountSubPath.isEmpty()) {
            hostGraphPath = hostSharedGraphPath.getFileName().toString().equals(graphMountSubPath) ? hostSharedGraphPath : hostSharedGraphPath.resolve(graphMountSubPath).resolve(containerTestImage.getName());
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

    static void buildBuilderImages() {
        parentLogger.info("Starting builder image initialization");
        try {
            // Initialize the base builder first
            parentLogger.info("Initializing base builder image");
            initBuilderImage(ContainterBuildStage.ORS_TEST_SCENARIO_BUILDER);

            // Create a list to store all futures for better tracking
            List<CompletableFuture<Void>> buildFutures = new ArrayList<>();

            // Asynchronous start the rest with proper error handling for each
            CompletableFuture<Void> warFuture = CompletableFuture
                    .runAsync(() -> {
                        parentLogger.info("Starting WAR builder image initialization");
                        initBuilderImage(ContainterBuildStage.ORS_TEST_SCENARIO_WAR_BUILDER);
                        parentLogger.info("WAR builder image initialized successfully");
                    })
                    .exceptionally(ex -> {
                        parentLogger.error("Failed to initialize WAR builder image: {}", ex.getMessage(), ex);
                        throw new RuntimeException("WAR builder initialization failed", ex);
                    });
            buildFutures.add(warFuture);

            CompletableFuture<Void> jarFuture = CompletableFuture
                    .runAsync(() -> {
                        parentLogger.info("Starting JAR builder image initialization");
                        initBuilderImage(ContainterBuildStage.ORS_TEST_SCENARIO_JAR_BUILDER);
                        parentLogger.info("JAR builder image initialized successfully");
                    })
                    .exceptionally(ex -> {
                        parentLogger.error("Failed to initialize JAR builder image: {}", ex.getMessage(), ex);
                        throw new RuntimeException("JAR builder initialization failed", ex);
                    });
            buildFutures.add(jarFuture);

            CompletableFuture<Void> mavenFuture = CompletableFuture
                    .runAsync(() -> {
                        parentLogger.info("Starting MAVEN builder image initialization");
                        initBuilderImage(ContainterBuildStage.ORS_TEST_SCENARIO_MAVEN_BUILDER);
                        parentLogger.info("MAVEN builder image initialized successfully");
                    })
                    .exceptionally(ex -> {
                        parentLogger.error("Failed to initialize MAVEN builder image: {}", ex.getMessage(), ex);
                        throw new RuntimeException("MAVEN builder initialization failed", ex);
                    });
            buildFutures.add(mavenFuture);

            // Wait for all with a timeout
            CompletableFuture
                    .allOf(buildFutures.toArray(new CompletableFuture[0]))
                    .orTimeout(5, java.util.concurrent.TimeUnit.MINUTES)
                    .join();

            parentLogger.info("All builder images initialized successfully");
        } catch (Exception e) {
            parentLogger.error("Failed to initialize builder images: {}", e.getMessage(), e);
            throw new RuntimeException("Builder images initialization failed", e);
        }
    }

    @BeforeAll
    public static void buildSharedLayersAndGraphs() throws IOException, InterruptedException {
        initializeContainers();
        if (!Boolean.parseBoolean(System.getProperty("container.builder.use_prebuild", "false"))) {
            parentLogger.info("container.builder.use_prebuild is set to false. Building builder images.");
            buildBuilderImages();
        } else {
            // The GitHub workflows build the images externally and caches them for the tests.
            parentLogger.warn("container.builder.use_prebuild is set to true. Skipping builder images.");
        }

        List<String> expectedGraphFolders = List.of("bobby-car", "cycling-electric", "cycling-mountain", "cycling-regular", "cycling-road", "driving-car", "driving-hgv", "foot-hiking", "foot-walking", "wheelchair");

        if (expectedGraphFolders.stream().allMatch(subdir -> Path.of(hostSharedGraphPath.toString(), subdir).toFile().exists())) {
            return;
        }
        GenericContainer<?> container = null;
        if (containerValue.equals("all") || containerValue.equals("jar")) {
            container = initContainer(ContainerTestImageBare.JAR_CONTAINER_BARE, false, hostSharedGraphPath.getFileName().toString(), false, Duration.ofSeconds(300));
            container.setCommand(ContainerTestImageBare.JAR_CONTAINER_BARE.getCommand("500M").toArray(new String[0]));
        } else if (containerValue.equals("maven")) {
            container = initContainer(ContainerTestImageBare.MAVEN_CONTAINER_BARE, false, hostSharedGraphPath.getFileName().toString(), false, Duration.ofSeconds(300));
            container.setCommand(ContainerTestImageBare.MAVEN_CONTAINER_BARE.getCommand("500M").toArray(new String[0]));
        } else if (containerValue.equals("war")) {
            container = initContainer(ContainerTestImageBare.WAR_CONTAINER_BARE, false, hostSharedGraphPath.getFileName().toString(), false, Duration.ofSeconds(300));
            container.setCommand(ContainerTestImageBare.WAR_CONTAINER_BARE.getCommand("500M").toArray(new String[0]));
        } else {
            parentLogger.error("Container scenario not set to either all, jar, maven or war. Exiting.");
        }
        container.addEnv("ors.engine.profile_default.enabled", "true");
        container.addEnv("ors.engine.profiles.public-transport.enabled", "false");
        container.addEnv("ors.engine.profiles.bobby-car.enabled", "true");
        container.addEnv("ors.engine.profiles.bobby-car.encoder_name", "driving-car");
        container.addEnv("ors.engine.graphs_data_access", "MMAP");
        container.addEnv("ors.engine.profile_default.graph_path", "/home/ors/openrouteservice/graphs");
        container.addEnv("ors.engine.profile_default.build.source_file", "/home/ors/openrouteservice/files/heidelberg.test.pbf");
        container.withStartupTimeout(Duration.ofSeconds(150));

        container.start();
        // set read write execute permissions for all
        container.execInContainer("chmod", "-R", "777", "/home/ors/openrouteservice/graphs");
        container.stop();
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

    @Getter
    public enum ContainterBuildStage implements ContainerTestImage {
        ORS_TEST_SCENARIO_BUILDER("ors-test-scenarios-builder"),
        ORS_TEST_SCENARIO_WAR_BUILDER("ors-test-scenarios-war-builder"),
        ORS_TEST_SCENARIO_JAR_BUILDER("ors-test-scenarios-jar-builder"),
        ORS_TEST_SCENARIO_MAVEN_BUILDER("ors-test-scenarios-maven-builder");

        private final String name;

        ContainterBuildStage(String name) {
            this.name = name;
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
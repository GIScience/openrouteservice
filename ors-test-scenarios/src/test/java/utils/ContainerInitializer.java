package utils;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.lifecycle.Startables;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static utils.TestContainersHelper.healthyOrsWaitStrategy;

public abstract class ContainerInitializer {
    private static final Map<String, String> defaultEnv = Map.of("logging.level.org.heigit", "DEBUG", "ors.engine.graphs_data_access", "MMAP", "server.port", "8080");
    private static List<ContainerTestImage> selectedDefaultContainers = List.of();
    private static List<ContainerTestImage> selectedNoConfigContainers = List.of();
    private static GenericContainer<?> warContainer;
    private static GenericContainer<?> jarContainer;
    private static GenericContainer<?> mvnContainer;
    private static GenericContainer<?> warContainerNoConfig;
    private static GenericContainer<?> jarContainerNoConfig;
    private static GenericContainer<?> mvnContainerNoConfig;

    public static void initializeContainers(Boolean startDefaultContainers) {
        String containerValue = System.getenv("CONTAINER_SCENARIO");
        if (containerValue == null) {
            containerValue = "all";
        }
        switch (containerValue) {
            case "war":
                selectedDefaultContainers = List.of(ContainerTestImageDefaults.WAR_CONTAINER);
                selectedNoConfigContainers = List.of(ContainerTestImageNoConfigs.WAR_CONTAINER_NO_CONFIG);
                break;
            case "jar":
                selectedDefaultContainers = List.of(ContainerTestImageDefaults.JAR_CONTAINER);
                selectedNoConfigContainers = List.of(ContainerTestImageNoConfigs.JAR_CONTAINER_NO_CONFIG);
                break;
            case "maven":
                selectedDefaultContainers = List.of(ContainerTestImageDefaults.MAVEN_CONTAINER);
                selectedNoConfigContainers = List.of(ContainerTestImageNoConfigs.MAVEN_CONTAINER_NO_CONFIG);
                break;
            default:
                selectedDefaultContainers = List.of(ContainerTestImageDefaults.WAR_CONTAINER, ContainerTestImageDefaults.JAR_CONTAINER, ContainerTestImageDefaults.MAVEN_CONTAINER);
                selectedNoConfigContainers = List.of(ContainerTestImageNoConfigs.WAR_CONTAINER_NO_CONFIG, ContainerTestImageNoConfigs.JAR_CONTAINER_NO_CONFIG, ContainerTestImageNoConfigs.MAVEN_CONTAINER_NO_CONFIG);
                break;
        }
        if (startDefaultContainers) {
            Startables.deepStart(selectedDefaultContainers.stream().map(container -> initContainer(container, false, false)).toArray(GenericContainer[]::new)).join();
        }
    }

    public static Stream<Object[]> ContainerTestImageDefaultsImageStream() {
        initializeContainers(false);
        // Check selectedContainers and return a stream of ContainerTestImage Enum objects
        return Stream.of(selectedDefaultContainers).flatMap(List::stream).map(container -> new Object[]{container});
    }

    public static Stream<Object[]> ContainerTestImageNoConfigsImageStream() {
        initializeContainers(false);
        // Create container selection
        return Stream.of(selectedNoConfigContainers).flatMap(List::stream).map(container -> new Object[]{container});
    }

    public static GenericContainer<?> initContainer(ContainerTestImage containerTestImage) {
        return initContainer(containerTestImage, false, true);
    }

    public static GenericContainer<?> initContainer(ContainerTestImage containerTestImage, Boolean recreate, Boolean autoStart) {
        if (containerTestImage == null) {
            throw new IllegalArgumentException("containerTestImage must not be null");
        }
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
                .waitingFor(healthyOrsWaitStrategy());
        GenericContainer<?> containerToReturn;

        if (containerTestImage == ContainerTestImageNoConfigs.WAR_CONTAINER_NO_CONFIG) {
            if (warContainerNoConfig == null || recreate) {
                if (warContainerNoConfig!=null) {
                    warContainerNoConfig.stop();
                }
                warContainerNoConfig = container;
            }
            containerToReturn = warContainerNoConfig;
        } else if (containerTestImage == ContainerTestImageNoConfigs.JAR_CONTAINER_NO_CONFIG) {
            if (jarContainerNoConfig == null || recreate) {
                if (jarContainerNoConfig!=null) {
                    jarContainerNoConfig.stop();
                }
                jarContainerNoConfig = container;
            }
            containerToReturn = jarContainerNoConfig;
        } else if (containerTestImage == ContainerTestImageNoConfigs.MAVEN_CONTAINER_NO_CONFIG) {
            if (mvnContainerNoConfig == null || recreate) {
                if (mvnContainerNoConfig!=null) {
                    mvnContainerNoConfig.stop();
                }
                mvnContainerNoConfig = container;
            }
            containerToReturn = mvnContainerNoConfig;
        } else if (containerTestImage == ContainerTestImageDefaults.WAR_CONTAINER) {
            if (warContainer == null || recreate) {
                if (warContainer!=null) {
                    warContainer.stop();
                    }
                warContainer = container;
            }
            if (autoStart && !warContainer.isRunning()) {
                warContainer.start();
            }
            containerToReturn = warContainer;
        } else if (containerTestImage == ContainerTestImageDefaults.JAR_CONTAINER) {
            if (jarContainer == null || recreate) {
                if (jarContainer!=null) {
                    jarContainer.stop();
                    }
                jarContainer = container;
            }
            if (autoStart && !jarContainer.isRunning())
                jarContainer.start();
            containerToReturn = jarContainer;
        } else {
            if (mvnContainer == null || recreate) {
                if (mvnContainer!=null) {
                    mvnContainer.stop();
                    }
                mvnContainer = container;
            }
            if (autoStart && !mvnContainer.isRunning()) {
                mvnContainer.start();
            }
            containerToReturn = mvnContainer;
        }
        return containerToReturn;
    }

    // Create enum for available test images
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

    // Create enum for available test images
    public enum ContainerTestImageNoConfigs implements ContainerTestImage {
        WAR_CONTAINER_NO_CONFIG("ors-test-scenarios-war-no-config"),
        JAR_CONTAINER_NO_CONFIG("ors-test-scenarios-jar-no-config"),
        MAVEN_CONTAINER_NO_CONFIG("ors-test-scenarios-maven-no-config");

        private final String name;

        ContainerTestImageNoConfigs(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public  interface ContainerTestImage {

        public String getName();
    }

}
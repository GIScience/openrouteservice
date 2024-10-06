package integrationtests;

import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

public abstract class ContainerInitializer {
    private final Map<String, String> defaultEnv = Map.of(
            "logging.level.org.heigit", "INFO",
            "ors.engine.graphs_data_access", "MMAP"
    );
    private GenericContainer<?> warContainer;
    private GenericContainer<?> jarContainer;

    public GenericContainer<?> initContainer(ContainerTestImage containerTestImage) {
        if (containerTestImage == null) {
            throw new IllegalArgumentException("containerTestImage must not be null");
        }

        WaitStrategy waitStrategy = new HttpWaitStrategy()
                .forPort(8080)
                .forStatusCode(200)
                .forPath("/ors/v2/health")
                .withStartupTimeout(Duration.ofSeconds(80));

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
                .withFileSystemBind("./graphs-integrationtests", "/home/ors/openrouteservice/graphs", BindMode.READ_WRITE)
                .withExposedPorts(8080)
                .withLogConsumer(outputFrame -> System.out.print(outputFrame.getUtf8String()))
                .waitingFor(waitStrategy);

        if (containerTestImage == ContainerTestImage.WAR_CONTAINER) {
            if (warContainer == null || !warContainer.isRunning()) {
                warContainer = container;
                warContainer.start();
            }
            return warContainer;
        } else {
            if (jarContainer == null || !jarContainer.isRunning()) {
                jarContainer = container;
                jarContainer.start();
            }
            return jarContainer;
        }
    }

    @BeforeEach
    public void resetEnv() {
        if (warContainer != null) {
            warContainer.withEnv(defaultEnv);
        }
        if (jarContainer != null) {
            jarContainer.withEnv(defaultEnv);
        }
    }

    // Create enum of available images
    public enum ContainerTestImage {
        WAR_CONTAINER("ors-test-scenarios-war"),
        JAR_CONTAINER("ors-test-scenarios-jar");

        private final String name;

        ContainerTestImage(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
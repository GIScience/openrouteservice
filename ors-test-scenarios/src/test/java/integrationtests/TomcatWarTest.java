package integrationtests;

import org.junit.jupiter.api.*;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import utils.OrsApiRequests;
import utils.OrsContainerFileSystemCheck;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static utils.OrsApiRequests.checkAvoidAreaRequest;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TomcatWarTest {
    private static final WaitStrategy OrsHealthWaitStrategy = new HttpWaitStrategy()
            .forPort(8080)
            .forStatusCode(200)
            .forPath("/ors/v2/health")
            .withStartupTimeout(Duration.ofSeconds(80));
    private static final GenericContainer<?> sharedOrsTestContainer = new GenericContainer<>(
            new ImageFromDockerfile("ors-test-scenarios-war", false)
                    .withFileFromPath("ors-api", Path.of("../ors-api")) // Set the context folder
                    .withFileFromPath("ors-engine", Path.of("../ors-engine")) // Set the context folder
                    .withFileFromPath("ors-report-aggregation", Path.of("../ors-report-aggregation")) // Set the context folder
                    .withFileFromPath("pom.xml", Path.of("../pom.xml")) // Set the context folder
                    .withFileFromPath("ors-config.yml", Path.of("../ors-config.yml")) // Set the context folder
                    .withFileFromPath("Dockerfile", Path.of("../ors-test-scenarios/src/test/resources/ubuntu-jammy-tomcat-war/Dockerfile"))
                    .withFileFromPath(".dockerignore", Path.of("../.dockerignore")) // Set the context folder
    )
            .withFileSystemBind("./graphs-integrationtests", "/home/ors/openrouteservice/graphs", BindMode.READ_WRITE)
            .withExposedPorts(8080)
            .withLogConsumer(outputFrame -> System.out.print(outputFrame.getUtf8String()))
            .waitingFor(OrsHealthWaitStrategy);

    static {
        sharedOrsTestContainer.start();
    }

    @AfterEach
    void resetContainerEnv() {
        // Get current envs
        int originalEnvSize = sharedOrsTestContainer.getEnvMap().size();
        sharedOrsTestContainer.setEnv(List.of());
        // restart if original envs are not empty
        if (originalEnvSize > 0) {
            sharedOrsTestContainer.stop();
            sharedOrsTestContainer.start();
            sharedOrsTestContainer.waitingFor(OrsHealthWaitStrategy);
        }
    }

    @Test
    @Order(1)
    void testBuildAllGraphs() throws IOException {
        sharedOrsTestContainer.addEnv("ors.engine.profiles.public-transport.enabled", "false");
        sharedOrsTestContainer.addEnv("ors.engine.profile_default.enabled", "true");

        sharedOrsTestContainer.stop();
        sharedOrsTestContainer.start();

        // Assert 8 profiles are loaded
        JsonNode profiles = OrsApiRequests.getProfiles(sharedOrsTestContainer.getHost(), sharedOrsTestContainer.getFirstMappedPort());
        Assertions.assertEquals(9, profiles.size());
    }

    @Test
    void testDefaultProfileLoaded() throws IOException, InterruptedException {
        String address = sharedOrsTestContainer.getHost();
        Integer port = sharedOrsTestContainer.getFirstMappedPort();

        JsonNode profiles = OrsApiRequests.getProfiles(address, port);
        // 7. Check that the profiles key has a length of 1
        Assertions.assertEquals(1, profiles.size());
        // 8. Check that the default profile is loaded
        // TODO find out why this doesnt return "driving-car".
        Assertions.assertTrue(profiles.has("profile 1"));

        // 9. Check that the following folder exists in the container
        ArrayList<String> directories = new ArrayList<>() {{
            add("/usr/local/tomcat/webapps/ors");
            add("/usr/local/tomcat/temp");
            add("/home/ors/openrouteservice/graphs/driving-car");
        }};
        OrsContainerFileSystemCheck.assertDirectoriesExist(sharedOrsTestContainer, directories.toArray(new String[0]));
        // 10. Check that the following files exist in the container
        ArrayList<String> files = new ArrayList<>() {{
            add("/home/ors/openrouteservice/conf/ors-config.yml");
            add("/home/ors/openrouteservice/logs/ors.log");
            add("/home/ors/openrouteservice/files/heidelberg.test.pbf");
            add("/home/ors/openrouteservice/elevation_cache/srtm_38_03.gh");
            add("/usr/local/tomcat/bin/setenv.sh");
            add("/usr/local/tomcat/webapps/ors.war");
        }};
        OrsContainerFileSystemCheck.assertFilesExist(sharedOrsTestContainer, files.toArray(new String[0]));

        // 11. Check that "/usr/local/tomcat/temp/GeoTools" does not exist
        OrsContainerFileSystemCheck.assertDirectoryExists(sharedOrsTestContainer, "/usr/local/tomcat/temp/GeoTools", false);

        // 12. Request an avoid area
        checkAvoidAreaRequest("http://" + address + ":" + port + "/ors/v2/directions/driving-car/geojson", 200);

        // 13. Check that GeoTools exists in the container
        OrsContainerFileSystemCheck.assertDirectoryExists(sharedOrsTestContainer, "/usr/local/tomcat/temp/GeoTools", true);
    }

    @Test
    void testSecondAndThirdProfileActivatedByEnv() throws IOException {
        JsonNode profiles = OrsApiRequests.getProfiles(sharedOrsTestContainer.getHost(), sharedOrsTestContainer.getFirstMappedPort());
        Assertions.assertEquals(1, profiles.size());

        sharedOrsTestContainer.addEnv("ors.engine.profiles.driving-hgv.enabled", "true");
        sharedOrsTestContainer.stop();
        sharedOrsTestContainer.start();
        sharedOrsTestContainer.waitingFor(OrsHealthWaitStrategy);

        profiles = OrsApiRequests.getProfiles(sharedOrsTestContainer.getHost(), sharedOrsTestContainer.getFirstMappedPort());
        Assertions.assertEquals(2, profiles.size());

        sharedOrsTestContainer.addEnv("ors.engine.profiles.cycling-regular.enabled", "true");

        sharedOrsTestContainer.stop();
        sharedOrsTestContainer.start();
        sharedOrsTestContainer.waitingFor(OrsHealthWaitStrategy);
        profiles = OrsApiRequests.getProfiles(sharedOrsTestContainer.getHost(), sharedOrsTestContainer.getFirstMappedPort());
        Assertions.assertEquals(3, profiles.size());
    }

    @Test
    void testActivateAllProfilesExceptTwo() throws IOException, InterruptedException {
        JsonNode profiles = OrsApiRequests.getProfiles(sharedOrsTestContainer.getHost(), sharedOrsTestContainer.getFirstMappedPort());
        Assertions.assertEquals(1, profiles.size());

        sharedOrsTestContainer.addEnv("ors.engine.profile_default.enabled", "true");
        sharedOrsTestContainer.addEnv("ors.engine.profiles.cycling-regular.enabled", "false");
        sharedOrsTestContainer.addEnv("ors.engine.profiles.public-transport.enabled", "false");

        sharedOrsTestContainer.stop();
        sharedOrsTestContainer.start();
        sharedOrsTestContainer.waitingFor(OrsHealthWaitStrategy);
        profiles = OrsApiRequests.getProfiles(sharedOrsTestContainer.getHost(), sharedOrsTestContainer.getFirstMappedPort());
        Assertions.assertEquals(8, profiles.size());
        ArrayList<String> directories = new ArrayList<>() {{
            add("/usr/local/tomcat/webapps/ors");
            add("/usr/local/tomcat/temp");
            add("/home/ors/openrouteservice/graphs/driving-car");
            add("/home/ors/openrouteservice/graphs/driving-hgv");
            add("/home/ors/openrouteservice/graphs/cycling-mountain");
            add("/home/ors/openrouteservice/graphs/cycling-road");
            add("/home/ors/openrouteservice/graphs/foot-walking");
            add("/home/ors/openrouteservice/graphs/foot-hiking");
            add("/home/ors/openrouteservice/graphs/wheelchair");
        }};
        OrsContainerFileSystemCheck.assertDirectoriesExist(sharedOrsTestContainer, directories.toArray(new String[0]));
    }
}
package integrationtests;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import utils.OrsApiRequests;
import utils.OrsContainerFileSystemCheck;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static utils.OrsApiRequests.checkAvoidAreaRequest;

@Testcontainers
@ExtendWith(TestcontainersExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EnvironmentChecks extends ContainerInitializer {

    static Stream<Object[]> data() {
        return Stream.of(
                new Object[]{ContainerTestImage.WAR_CONTAINER},
                new Object[]{ContainerTestImage.JAR_CONTAINER}
        );
    }


    @Order(1)
    @MethodSource("data")
    @ParameterizedTest(name = "{0}")
    void testBuildAllImagesAndGraphs(ContainerTestImage targetImage) throws IOException, InterruptedException {
        GenericContainer<?> container = initContainer(targetImage);
        container.addEnv("ors.engine.profiles.public-transport.enabled", "false");
        container.addEnv("ors.engine.profile_default.enabled", "true");
        // sharedOrsTestContainer.addEnv("gtfs_file", "/home/ors/openrouteservice/ors-api/src/test/files/vrn_gtfs_cut.zip");

        container.stop();
        container.start();

        JsonNode profiles = OrsApiRequests.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(9, profiles.size());
        List<String> expectedProfiles = List.of(
                "foot-walking", "wheelchair", "foot-hiking", "cycling-electric",
                "cycling-mountain", "driving-car", "driving-hgv", "cycling-regular", "cycling-road"
                // "public-transport"
        );
        for (JsonNode profile : profiles) {
            Assertions.assertTrue(expectedProfiles.contains(profile.get("profiles").asText()));
        }

        List<String> files = List.of(
                "/home/ors/openrouteservice/ors-config.yml",
                "/home/ors/openrouteservice/logs/ors.log",
                "/home/ors/openrouteservice/files/heidelberg.test.pbf",
                "/home/ors/openrouteservice/elevation_cache/srtm_38_03.gh"
        );
        OrsContainerFileSystemCheck.assertFilesExist(container, files.toArray(new String[0]));

        List<String> directories = List.of(
                "/home/ors/openrouteservice/graphs/driving-car",
                "/home/ors/openrouteservice/graphs/driving-hgv",
                "/home/ors/openrouteservice/graphs/cycling-mountain",
                "/home/ors/openrouteservice/graphs/cycling-road",
                "/home/ors/openrouteservice/graphs/foot-walking",
                "/home/ors/openrouteservice/graphs/foot-hiking",
                "/home/ors/openrouteservice/graphs/wheelchair"
        );
        OrsContainerFileSystemCheck.assertDirectoriesExist(container, directories.toArray(new String[0]));
    }

    @Order(2)
    @MethodSource("data")
    @ParameterizedTest(name = "{0}")
    void testAvoidAreaRequestAndGeoToolsPopulation(ContainerTestImage targetImage) throws IOException, InterruptedException {
        GenericContainer<?> container = initContainer(targetImage);

        String geoToolsPath;
        if (targetImage.equals(ContainerTestImage.WAR_CONTAINER))
            geoToolsPath = "/usr/local/tomcat/temp/GeoTools";
        else geoToolsPath = "/tmp/GeoTools";

        OrsContainerFileSystemCheck.assertDirectoryExists(container, geoToolsPath, false);
        checkAvoidAreaRequest("http://" + container.getHost() + ":" + container.getFirstMappedPort() + "/ors/v2/directions/driving-car/geojson", 200);
        OrsContainerFileSystemCheck.assertDirectoryExists(container, geoToolsPath, true);
    }

    @Order(3)
    @MethodSource("data")
    @ParameterizedTest(name = "{0}")
    void testTwoProfilesActivatedByEnv(ContainerTestImage targetImage) throws IOException {
        GenericContainer<?> container = initContainer(targetImage);

        // Activate two new profiles alongside the default driving-car profile
        container.addEnv("ors.engine.profile_default.enabled", "false");
        container.addEnv("ors.engine.profiles.driving-hgv.enabled", "true");
        container.addEnv("ors.engine.profiles.cycling-regular.enabled", "true");
        container.stop();
        container.start();

        JsonNode profiles = OrsApiRequests.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(3, profiles.size());

        List<String> loadedProfiles = List.of("driving-car", "driving-hgv", "cycling-regular");
        for (JsonNode profile : profiles) {
            Assertions.assertTrue(loadedProfiles.contains(profile.get("profiles").asText()));
        }
    }
}
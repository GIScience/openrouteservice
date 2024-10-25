package integrationtests;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;
import utils.ContainerInitializer;
import utils.OrsContainerFileSystemCheck;

import static utils.ContainerInitializer.initContainer;
import static utils.OrsApiHelper.checkAvoidAreaRequest;

@ExtendWith(TestcontainersExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Testcontainers(disabledWithoutDocker = true)
public class GeoToolsTest {

    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testAvoidAreaRequestAndGeoToolsPopulation(ContainerInitializer.ContainerTestImageDefaults targetImage) {
        GenericContainer<?> container = initContainer(targetImage, true, "testAvoidAreaRequestAndGeoToolsPopulation");

        String geoToolsPath;
        if (targetImage.equals(ContainerInitializer.ContainerTestImageDefaults.WAR_CONTAINER))
            geoToolsPath = "/usr/local/tomcat/temp/GeoTools";
        else geoToolsPath = "/tmp/GeoTools";

        OrsContainerFileSystemCheck.assertDirectoryExists(container, geoToolsPath, false);
        checkAvoidAreaRequest("http://" + container.getHost() + ":" + container.getFirstMappedPort() + "/ors/v2/directions/driving-car/geojson", 200);
        OrsContainerFileSystemCheck.assertDirectoryExists(container, geoToolsPath, true);
        container.stop();
    }
}

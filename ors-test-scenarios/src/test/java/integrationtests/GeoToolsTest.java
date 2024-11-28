package integrationtests;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;
import utils.ContainerInitializer;
import utils.OrsContainerFileSystemCheck;

import static utils.OrsApiHelper.checkAvoidAreaRequest;

@ExtendWith(TestcontainersExtension.class)
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class GeoToolsTest extends ContainerInitializer {

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class GeoToolsTests {
        @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
        @ParameterizedTest(name = "{0}")
        @Execution(ExecutionMode.CONCURRENT)
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
}

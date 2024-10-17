package integrationtests;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;
import utils.ContainerInitializer;
import utils.OrsApiHelper;
import utils.OrsContainerFileSystemCheck;
import utils.configs.GrcConfigBuilder;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testcontainers.utility.MountableFile.forHostPath;
import static utils.TestContainersHelper.orsCorrectConfigLoadedWaitStrategy;


public class GraphRepoTest {

    /**
     * grc-startup-with-downloaded-graph.sh
     * This test sets up ors with a proper Graph Repository Configuration (GRC) file and a downloadable graph.
     * Ors doesn't come with another graph. According to the config ORS will look in the graph folder of the GRC configuration.
     * The driving-car graph will then be downloaded and ors started normally.
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testGrcStartupWithDownloadedGraphs(ContainerInitializer.ContainerTestImageDefaults targetImage, @TempDir Path tempDir) {
        GenericContainer<?> container = ContainerInitializer.initContainer(targetImage, false);
        container.withCopyFileToContainer(
                MountableFile.forHostPath("../ors-engine/src/test/resources/test-filesystem-repos/"),
                "/tmp/graphs-repo"
        );

        GrcConfigBuilder grcConfigFilePath = GrcConfigBuilder.builder()
                .graphManagementEnabled(true)
                .setRepoManagementPerProfile(true)
                .profiles(new HashMap<>() {{
                    put("driving-car", true);
                }})
                .profileDefaultGraphPath("/home/ors/openrouteservice/graphs")
                .repositoryUri("/tmp/graphs-repo")
                .repositoryName("vendor-xyz")
                .repositoryProfileGroup("fastisochrones")
                .graphExtent("heidelberg")
                .build();
        Path grcConfig = grcConfigFilePath.toYAML(tempDir, "grc-config.yml");
        String containerConfigPath = "/home/ors/openrouteservice/ors-config.yml";
        container.withCopyFileToContainer(forHostPath(grcConfig), containerConfigPath);
        if (ContainerInitializer.ContainerTestImageDefaults.WAR_CONTAINER.equals(targetImage)) {
            container.withCopyFileToContainer(forHostPath(grcConfig), containerConfigPath);
            // The war container has another working directory /usr/lib/tomcat/.
            // Tomcat therefore prints the config location as an absolute path to /home/ors/openrouteservice/ors-config.yml.
            // The waiting strategy needs to be different.
            container.waitingFor(orsCorrectConfigLoadedWaitStrategy(containerConfigPath));
        } else {
            // Jar and Maven both have the working directory /home/ors/openrouteservice/. Therefore, the config location is printed as ./ors-config.yml.
            container.withCopyFileToContainer(forHostPath(grcConfig), containerConfigPath);
            container.waitingFor(orsCorrectConfigLoadedWaitStrategy("./ors-config.yml"));
        }

        // Clean the container binds
        container.setBinds(List.of());

        container.start();

        OrsContainerFileSystemCheck.assertDirectoryExists(container, "/tmp/graphs-repo", true);
        OrsContainerFileSystemCheck.assertDirectoryExists(container, "/home/ors/openrouteservice/graphs/driving-car", true);
        OrsContainerFileSystemCheck.assertFileExists(container, "/home/ors/openrouteservice/graphs/vendor-xyz_fastisochrones_heidelberg_1_driving-car.yml", true);

        // Check that the graph was loaded
        OrsApiHelper.assertProfilesLoaded(container, Map.of("driving-car", true));

        container.stop();
    }

}

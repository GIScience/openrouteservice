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
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void testGrcStartupWithDownloadedGraphs(ContainerInitializer.ContainerTestImageBare targetImage, @TempDir Path tempDir) {
        GenericContainer<?> container = ContainerInitializer.initContainer(targetImage, false);
        container.withCopyFileToContainer(
                MountableFile.forHostPath("../ors-engine/src/test/resources/test-filesystem-repos/"),
                "/tmp/graphs-repo"
        );

        GrcConfigBuilder grcConfigFilePath = GrcConfigBuilder.builder()
                .tempDir(tempDir)
                .fileName("grc-config.yml")
                .graphManagementEnabled(true)
                .setRepoManagementPerProfile(true)
                .profiles(new HashMap<>() {{
                    put("driving-car", true);
                }})
                .repositoryUri("/tmp/graphs-repo")
                .repositoryName("vendor-xyz")
                .repositoryProfileGroup("fastisochrones")
                .graphExtent("heidelberg")
                .build();
        Path grcConfig = grcConfigFilePath.getConfig();
        String containerConfigPath = "/tmp/grc-config.yml";
        container.withCopyFileToContainer(forHostPath(grcConfig), containerConfigPath);
        container.withEnv(Map.of("ORS_CONFIG_LOCATION", containerConfigPath));
        container.waitingFor(orsCorrectConfigLoadedWaitStrategy(containerConfigPath));

        // Clean the container binds
        container.setBinds(List.of());

        container.setCommand(targetImage.getCommand("250M").toArray(new String[0]));
        container.start();

        OrsContainerFileSystemCheck.assertDirectoryExists(container, "/tmp/graphs-repo", true);
        OrsContainerFileSystemCheck.assertDirectoryExists(container, "/home/ors/openrouteservice/graphs/driving-car", true);
        OrsContainerFileSystemCheck.assertFileExists(container, "/home/ors/openrouteservice/graphs/vendor-xyz_fastisochrones_heidelberg_1_driving-car.yml", true);

        // Check that the graph was loaded
        OrsApiHelper.assertProfilesLoaded(container, Map.of("driving-car", true));

        container.stop();
    }

}

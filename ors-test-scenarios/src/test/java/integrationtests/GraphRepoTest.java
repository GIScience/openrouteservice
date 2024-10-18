package integrationtests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;
import utils.ContainerInitializer;
import utils.OrsApiHelper;
import utils.OrsConfig;
import utils.OrsContainerFileSystemCheck;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testcontainers.utility.MountableFile.forHostPath;
import static utils.GrcSetupHelper.getCurrentDateInFormat;
import static utils.GrcSetupHelper.setupGraphRepo;
import static utils.OrsApiHelper.checkAvoidAreaRequest;
import static utils.TestContainersHelper.*;


public class GraphRepoTest {

    // @formatter:off
    OrsConfig.OrsConfigBuilder GRC_CONFIG = OrsConfig.builder()
            .profileDefaultEnabled(false)
            .ProfileDefaultBuildSourceFile("/home/ors/openrouteservice/files/heidelberg.test.pbf")
            .profileDefaultGraphPath("/home/ors/openrouteservice/graphs")
            .graphManagementEnabled(true)
            .setRepoManagementPerProfile(true)
            .graphManagementDownloadSchedule("0/6 * * * * *")
            .graphManagementActivationSchedule("0/2 * * * * *")
            .profiles(new HashMap<>() {{
                put("driving-car", true);
            }})
            .ProfileDefaultBuildSourceFile("/home/ors/openrouteservice/files/heidelberg.test.pbf")
            .profileDefaultGraphPath("/home/ors/openrouteservice/graphs")
            .repositoryUri("/tmp/test-filesystem-repo")
            .repositoryName("vendor-xyz")
            .repositoryProfileGroup("fastisochrones")
            .graphExtent("heidelberg");
    // @formatter:on


    /**
     * grc-startup-with-downloaded-graph_repo-defined-in-profile-default.sh
     * This test sets up ors with a proper Graph Repository Configuration (GRC) file and a downloadable graph.
     * The first container will be used to create the graph repository and the graph.
     * The second container start will omit a pre-build graph.
     * Additionally, the second container does not come with the ability to build graphs.
     * According to the config ORS will look in the graph folder of the GRC configuration.
     * The driving-car graph will then be downloaded and ors started normally.
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testGrcStartupWithDownloadedGraphRepoDefinedInProfileDefault(ContainerInitializer.ContainerTestImageDefaults targetImage, @TempDir Path tempDir) throws IOException, InterruptedException {
        GenericContainer<?> container = ContainerInitializer.initContainer(targetImage, false);
        container.start();
        Assertions.assertTrue(setupGraphRepo(container, getCurrentDateInFormat(2)), "Failed to prepare the graph repo.");
        copyFolderContentFromContainer(container, "/tmp/test-filesystem-repo", tempDir.resolve("test-filesystem-repo").toString());
        container.stop();
        // Clear all other binds
        container.setBinds(List.of());
        Path grcConfig = GRC_CONFIG.ProfileDefaultBuildSourceFile("").build().toYAML(tempDir, "grc-config.yml");

        String containerConfigPath = "/home/ors/openrouteservice/ors-config.yml";
        container.withCopyFileToContainer(forHostPath(grcConfig), containerConfigPath);
        container.withCopyFileToContainer(MountableFile.forHostPath(tempDir.resolve("test-filesystem-repo") + "/"), "/tmp/test-filesystem-repo/");
        if (ContainerInitializer.ContainerTestImageDefaults.WAR_CONTAINER.equals(targetImage)) {
            // The war container has another working directory /usr/lib/tomcat/.
            // Tomcat therefore prints the config location as an absolute path to /home/ors/openrouteservice/ors-config.yml.
            // The waiting strategy needs to be different.
            container.waitingFor(orsCorrectConfigLoadedWaitStrategy(containerConfigPath));
        } else {
            // Jar and Maven both have the working directory /home/ors/openrouteservice/. Therefore, the config location is printed as ./ors-config.yml.
            container.waitingFor(orsCorrectConfigLoadedWaitStrategy("./ors-config.yml"));
        }

        container.start();

        // @formatter:off
        Assertions.assertTrue(
                waitForLogPatterns(container,List.of(
                                "1 profile configurations submitted as tasks",
                                "[driving-car] Creating graph directory driving-car",
                                "Using FileSystemRepoManager for repoUri /tmp/test-filesystem-repo",
                                "[driving-car] No local graph or extracted downloaded graph found - trying to download and extract graph from repository",
                                "[driving-car] Extracting downloaded graph file to /home/ors/openrouteservice/graphs/driving-car_new_incomplete",
                                "[driving-car] Renaming extraction directory to /home/ors/openrouteservice/graphs/driving-car_new",
                                "[driving-car] Downloaded graph was extracted and will be activated at next restart check or application start",
                                "[driving-car] Activating extracted downloaded graph",
                                "[1] Profile: 'driving-car', encoder: 'driving-car', location: '/home/ors/openrouteservice/graphs/driving-car'",
                                "[driving-car] Checking for possible graph update from remote repository",
                                "Restart check done: No downloaded graphs found, no restart required"),
                        12, 1000),
                "The expected log patterns were not found in the logs.");
        // @formatter:on

        OrsContainerFileSystemCheck.assertDirectoryExists(container, "/tmp/test-filesystem-repo", true);
        OrsContainerFileSystemCheck.assertDirectoryExists(container, "/home/ors/openrouteservice/graphs/driving-car", true);
        OrsContainerFileSystemCheck.assertFileExists(container, "/home/ors/openrouteservice/graphs/vendor-xyz_fastisochrones_heidelberg_1_driving-car.yml", true);

        // Check that the graph was loaded
        OrsApiHelper.assertProfilesLoaded(container, Map.of("driving-car", true));

        container.stop();
    }

    /**
     * grc-update.sh
     * This test sets up ors with a proper Graph Repository Configuration (GRC) file and a downloadable graph.
     * At the first start, the graph will be generated and loaded. This graph is then taken and adjusted to simulate a new graph.
     * The new graph is then placed in the graph repository in the proper way with a .ghz and a .yml file.
     * The graph will then be downloaded and activated by the ors instance after the download and activation schedule.
     * TODO find out what happened to max_backups and the related log output
     * TODO the container shows exception when restarting.
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testGrcUpdateExistingGraph(ContainerInitializer.ContainerTestImageDefaults targetImage, @TempDir Path tempDir) throws IOException, InterruptedException {
        GenericContainer<?> container = ContainerInitializer.initContainer(targetImage, false);

        Path grcConfig = GRC_CONFIG.build().toYAML(tempDir, "grc-config.yml");

        container.withCopyFileToContainer(forHostPath(grcConfig), "/home/ors/openrouteservice/ors-config.yml");

        container.setBinds(List.of());
        container.start();

        // Check that the graph was loaded
        OrsApiHelper.assertProfilesLoaded(container, Map.of("driving-car", true));
        OrsContainerFileSystemCheck.assertDirectoryExists(container, "/home/ors/openrouteservice/graphs/driving-car", true);

        Assertions.assertTrue(setupGraphRepo(container, getCurrentDateInFormat(2)), "Failed to prepare the graph repo.");
        // @formatter:off
        Assertions.assertTrue(waitForLogPatterns(container, List.of(
                "[driving-car] Checking for possible graph update from remote repository",
                "[driving-car] Checking latest graphInfo in remote repository",
                "[driving-car] Download finished after",
                "[driving-car] Extracting downloaded graph file to /home/ors/openrouteservice/graphs/driving-car_new_incomplete",
                "[driving-car] Extraction of downloaded graph file finished after",
                "deleting downloaded graph file /home/ors/openrouteservice/graphs/vendor-xyz_fastisochrones_heidelberg_1_driving-car.ghz",
                "[driving-car] Renaming extraction directory to /home/ors/openrouteservice/graphs/driving-car_new",
                "[driving-car] Downloaded graph was extracted and will be activated at next restart check or application start.",
                "Restart check done: Restarting openrouteservice"
                ),
                12, 1000), "The expected log patterns were not found in the logs.");
        // @formatter:on
        // Check that the graph was loaded
        OrsApiHelper.assertProfilesLoaded(container, Map.of("driving-car", true));
        OrsContainerFileSystemCheck.assertDirectoryExists(container, "/home/ors/openrouteservice/graphs/driving-car", true);
        checkAvoidAreaRequest("http://" + container.getHost() + ":" + container.getFirstMappedPort() + "/ors/v2/directions/driving-car/geojson", 200);
        // We don't want the exception to appear.
        // TODO fix this
        Assertions.assertFalse(
                waitForLogPatterns(container, List.of(" Unexpected exception occurred invoking async method: public void org.heigit.ors.api.services.GraphService.checkForDownloadedGraphsToActivate()"), 12, 1000));
        // Assert that the graph_info.yml was updated
        container.stop();
    }

    /**
     * grc-startup-fails-when-graph-missing-in-repo.sh
     * This test starts a Graph Repo Lookup but fails to find a proper graph.
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testGrcStartupFailsWhenGraphMissingInRepo(ContainerInitializer.ContainerTestImageDefaults targetImage, @TempDir Path tempDir) throws IOException, InterruptedException {
        GenericContainer<?> container = ContainerInitializer.initContainer(targetImage, false);
        if (targetImage.equals(ContainerInitializer.ContainerTestImageDefaults.WAR_CONTAINER)) {
            container.waitingFor(simpleLogMessageWaitStrategy("Restart check done: No downloaded graphs found, no restart required"));
        } else {
            container.waitingFor(simpleLogMessageWaitStrategy("Shutting down openrouteservice"));
        }
        // @formatter:off
        Path grcConfig = GRC_CONFIG
                .profileDefaultGraphPath("/home/ors/openrouteservice/graphs")
                .ProfileDefaultBuildSourceFile("")
                .graphManagementEnabled(true)
                .repositoryUri("/tmp/wrong-filesystem-repo")
                .repositoryName("vendor-xyz")
                .repositoryProfileGroup("fastisochrones")
                .graphExtent("heidelberg")
                .profiles(new HashMap<>() {{
                    put("driving-hgv", true);
                }})
                .build().toYAML(tempDir, "grc-config.yml");
        // @formatter:on
        container.withCopyFileToContainer(forHostPath(grcConfig), "/home/ors/openrouteservice/ors-config.yml");

        container.setBinds(List.of());
        container.start();
        // @formatter:off
        Assertions.assertTrue(
                waitForLogPatterns(container,List.of(
                    "1 profile configurations submitted as tasks",
                    "[driving-hgv] Creating graph directory driving-hgv",
                    "Using FileSystemRepoManager for repoUri /tmp/wrong-filesystem-repo",
                    "[driving-hgv] No local graph or extracted downloaded graph found - trying to download and extract graph from repository",
                    "[driving-hgv] Checking for possible graph update from remote repository",
                    "[driving-hgv] Checking latest graphInfo in remote repository",
                    "[driving-hgv] No graphInfo found in remote repository: /tmp/wrong-filesystem-repo/vendor-xyz/fastisochrones/heidelberg/1/fastisochrones_heidelberg_1_driving-hgv.yml",
                    "[driving-hgv] No newer graph found in repository",
                    "[driving-hgv] No downloaded graph to extract"),
                        12, 1000),
                "The expected log patterns were not found in the logs.");
        // @formatter:on
        Assertions.assertFalse(container.isHealthy(), "The container should not be healthy.");
        // Assert that the graph_info.yml was updated
        container.stop();
    }

    /**
     * grc-startup-with-downloaded-graph.sh
     * Test the GRC functionality with Graph Management settings in profile default.
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testGrcStartupWithDownloadedGraphRepoDefinedInProfile(ContainerInitializer.ContainerTestImageDefaults targetImage, @TempDir Path tempDir) throws IOException, InterruptedException {
        GenericContainer<?> container = ContainerInitializer.initContainer(targetImage, false);
        container.start();
        Assertions.assertTrue(setupGraphRepo(container, getCurrentDateInFormat(2)), "Failed to prepare the graph repo.");
        copyFolderContentFromContainer(container, "/tmp/test-filesystem-repo", tempDir.resolve("test-filesystem-repo").toString());
        container.stop();
        // Clear all other binds
        container.setBinds(List.of());
        Path grcConfig = GRC_CONFIG
                .ProfileDefaultBuildSourceFile("")
                .setRepoManagementPerProfile(true)
                .build().toYAML(tempDir, "grc-config.yml");

        String containerConfigPath = "/home/ors/openrouteservice/ors-config.yml";
        container.withCopyFileToContainer(forHostPath(grcConfig), containerConfigPath);
        container.withCopyFileToContainer(MountableFile.forHostPath(tempDir.resolve("test-filesystem-repo") + "/"), "/tmp/test-filesystem-repo/");
        if (ContainerInitializer.ContainerTestImageDefaults.WAR_CONTAINER.equals(targetImage)) {
            container.waitingFor(orsCorrectConfigLoadedWaitStrategy(containerConfigPath));
        } else {
            container.waitingFor(orsCorrectConfigLoadedWaitStrategy("./ors-config.yml"));
        }

        container.start();

        // @formatter:off
        Assertions.assertTrue(
                waitForLogPatterns(container,List.of(
                                "1 profile configurations submitted as tasks",
                                "[driving-car] Creating graph directory driving-car",
                                "Using FileSystemRepoManager for repoUri /tmp/test-filesystem-repo",
                                "[driving-car] No local graph or extracted downloaded graph found - trying to download and extract graph from repository",
                                "[driving-car] Extracting downloaded graph file to /home/ors/openrouteservice/graphs/driving-car_new_incomplete",
                                "[driving-car] Renaming extraction directory to /home/ors/openrouteservice/graphs/driving-car",
                                "[driving-car] Downloaded graph was extracted and will be activated at next restart check or application start",
                                "[driving-car] Activating extracted downloaded graph",
                                "[1] Profile: 'driving-car', encoder: 'driving-car', location: '/home/ors/openrouteservice/graphs/driving-car'",
                                "[driving-car] Checking for possible graph update from remote repository",
                                "Restart check done: No downloaded graphs found, no restart required"),
                        12, 1000),
                "The expected log patterns were not found in the logs.");
        // @formatter:on

        OrsContainerFileSystemCheck.assertDirectoryExists(container, "/tmp/test-filesystem-repo", true);
        OrsContainerFileSystemCheck.assertDirectoryExists(container, "/home/ors/openrouteservice/graphs/driving-car", true);
        OrsContainerFileSystemCheck.assertFileExists(container, "/home/ors/openrouteservice/graphs/vendor-xyz_fastisochrones_heidelberg_1_driving-car.yml", true);

        // Check that the graph was loaded
        OrsApiHelper.assertProfilesLoaded(container, Map.of("driving-car", true));

        container.stop();
    }
}

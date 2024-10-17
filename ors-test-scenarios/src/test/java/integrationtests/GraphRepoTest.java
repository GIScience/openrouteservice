package integrationtests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;
import utils.ContainerInitializer;
import utils.OrsApiHelper;
import utils.OrsContainerFileSystemCheck;
import utils.configs.GrcConfigBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testcontainers.utility.MountableFile.forHostPath;
import static utils.TestContainersHelper.orsCorrectConfigLoadedWaitStrategy;


public class GraphRepoTest {

    GrcConfigBuilder GRC_CONFIG = GrcConfigBuilder.builder()
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
            .graphExtent("heidelberg")
            .build();

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

    public static String getCurrentDateInFormat(Integer increaseDaysBy) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        return ZonedDateTime.now().plusDays(increaseDaysBy).format(formatter);
    }

    /**
     * grc-update.sh
     * This test sets up ors with a proper Graph Repository Configuration (GRC) file and a downloadable graph.
     * At the first start, the graph will be generated and loaded. This graph is then taken and adjusted to simulate a new graph.
     * The new graph is then placed in the graph repository in the proper way with a .ghz and a .yml file.
     * The graph will then be downloaded and activated by the ors instance after the download and activation schedule.
     * TODO find out what happened to max_backups and the related log output
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testGrcUpdateExistingGraph(ContainerInitializer.ContainerTestImageDefaults targetImage, @TempDir Path tempDir) throws IOException, InterruptedException {
        GenericContainer<?> container = ContainerInitializer.initContainer(targetImage, false);

        Path grcConfig = GRC_CONFIG.toYAML(tempDir, "grc-config.yml");

        container.withCopyFileToContainer(forHostPath(grcConfig), "/home/ors/openrouteservice/ors-config.yml");

        container.setBinds(List.of());
        container.start();

        // Check that the graph was loaded
        OrsApiHelper.assertProfilesLoaded(container, Map.of("driving-car", true));
        OrsContainerFileSystemCheck.assertDirectoryExists(container, "/home/ors/openrouteservice/graphs/driving-car", true);

        Assertions.assertTrue(setupGraphRepo(container, getCurrentDateInFormat(2)), "Failed to prepare the graph repo.");

        Assertions.assertTrue(waitForLogPatterns(container, List.of(
                "[driving-car] Downloaded graph was extracted and will be activated at next restart check or application start",
                "[driving-car] Activating extracted downloaded graph",
                "[2] Profile: 'driving-car', encoder: 'driving-car', location: '/home/ors/openrouteservice/graphs/driving-car'",
                "[driving-car] Checking for possible graph update from remote repository",
                "Restart check done: No downloaded graphs found, no restart required"
        ), 12, 1000), "The expected log patterns were not found in the logs.");

        // Check that the graph was loaded
        OrsApiHelper.assertProfilesLoaded(container, Map.of("driving-car", true));
        OrsContainerFileSystemCheck.assertDirectoryExists(container, "/home/ors/openrouteservice/graphs/driving-car", true);
        // Assert that the graph_info.yml was updated
        container.stop();
    }

    private boolean setupGraphRepo(GenericContainer<?> container, String importDate) throws IOException, InterruptedException {
        String carGraphPath = "/home/ors/openrouteservice/graphs/driving-car";
        String repoPath = "/tmp/test-filesystem-repo/vendor-xyz/fastisochrones/heidelberg/1";
        String scratchGraphPath = "/tmp/scratch";
        String scratchGraphPathDrivingCar = scratchGraphPath + "/driving-car";

        return executeCommands(container, List.of(
                new String[]{"mkdir", "-p", repoPath, scratchGraphPath},
                new String[]{"cp", "-r", carGraphPath, scratchGraphPath},
                new String[]{"sh", "-c", "yq -i e '.import_date = \"" + importDate + "\"' " + scratchGraphPathDrivingCar + "/graph_info.yml"},
                new String[]{"zip", "-j", "-r", repoPath + "/fastisochrones_heidelberg_1_driving-car.ghz", scratchGraphPathDrivingCar},
                new String[]{"cp", scratchGraphPathDrivingCar + "/graph_info.yml", repoPath + "/fastisochrones_heidelberg_1_driving-car.yml"}
        ));
    }

    private boolean executeCommands(GenericContainer<?> container, List<String[]> commands) throws IOException, InterruptedException {
        for (String[] command : commands) {
            Container.ExecResult result = container.execInContainer(command);
            if (result.getExitCode() != 0) {
                System.out.println(result.getExitCode());
                System.out.println(result.getStdout());
                System.out.println(result.getStderr());
                return false;
            }
        }
        return true;
    }

    private boolean waitForLogPatterns(GenericContainer<?> container, List<String> logPatterns, int maxWaitTimeInSeconds, int recheckFrequencyInMillis) throws InterruptedException {
        int elapsedTime = 0;
        while (elapsedTime < maxWaitTimeInSeconds * 1000) {
            boolean allPatternsFound = logPatterns.stream().allMatch(pattern -> container.getLogs().contains(pattern));
            if (allPatternsFound) {
                return true;
            }
            Thread.sleep(recheckFrequencyInMillis);
            elapsedTime += recheckFrequencyInMillis;
        }
        return false;
    }
}

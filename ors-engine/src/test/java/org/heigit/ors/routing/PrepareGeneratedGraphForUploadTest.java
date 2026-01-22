package org.heigit.ors.routing;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.BuildProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link RoutingProfile#prepareGeneratedGraphForUpload(ProfileProperties, String)}
 * covering success and error branches.
 */
class PrepareGeneratedGraphForUploadTest {
    private Path tempRoot;
    private static final String GRAPH_VERSION = "x";

    @AfterEach
    void cleanup() {
        if (tempRoot != null && Files.exists(tempRoot)) {
            try (Stream<Path> paths = Files.walk(tempRoot)) {
                paths.map(Path::toFile)
                    .sorted(Comparator.reverseOrder())
                    .forEach(File::delete);
            } catch (IOException ignored) {
                // ignore
            }
        }
    }

    private ProfileProperties makeProfileProps(Path graphsRoot, String profileName, String group, String extent) {
        ProfileProperties props = new ProfileProperties();
        BuildProperties build = new BuildProperties();
        build.setProfileGroup(group);
        build.setGraphExtent(extent);
        props.setBuild(build);
        props.setEncoderName(EncoderNameEnum.DEFAULT);
        props.setGraphPath(graphsRoot);
        props.setProfileName(profileName);
        return props;
    }

    @Test
    void success_createsArchive_and_deletesSource() throws Exception {
        tempRoot = Files.createTempDirectory("pgu-success");
        Path graphsRoot = tempRoot.resolve("graphs");
        Files.createDirectories(graphsRoot);
        Path profileDir = graphsRoot.resolve("profileA");
        Files.createDirectories(profileDir);

        // graph_build_info.yml must exist and at least one file to be archived
        Files.writeString(profileDir.resolve("graph_build_info.yml"), "info");
        Files.writeString(profileDir.resolve("data.bin"), "payload");

        ProfileProperties props = makeProfileProps(graphsRoot, "profileA", "group", "extent");

        boolean result = RoutingProfile.prepareGeneratedGraphForUpload(props, GRAPH_VERSION);

        String graphName = String.join("_", "group", "extent", GRAPH_VERSION, props.getEncoderName().toString());
        Path archive = graphsRoot.resolve(graphName + ".ghz");

        assertTrue(result, "Preparation should return true on success");
        assertTrue(Files.exists(archive), "Archive file should be created on success");
        assertFalse(Files.exists(profileDir), "Source profile directory should be deleted after successful archive");
    }

    @Test
    void missing_graph_build_info_will_not_create_archive_and_will_not_delete_source() throws Exception {
        tempRoot = Files.createTempDirectory("pgu-missinginfo");
        Path graphsRoot = tempRoot.resolve("graphs");
        Files.createDirectories(graphsRoot);
        Path profileDir = graphsRoot.resolve("profileB");
        Files.createDirectories(profileDir);

        // note: intentionally NOT creating graph_build_info.yml
        Files.writeString(profileDir.resolve("data.bin"), "payload");

        ProfileProperties props = makeProfileProps(graphsRoot, "profileB", "group", "extent");

        boolean result = RoutingProfile.prepareGeneratedGraphForUpload(props, GRAPH_VERSION);

        String graphName = String.join("_", "group", "extent", GRAPH_VERSION, props.getEncoderName().toString());
        Path archive = graphsRoot.resolve(graphName + ".ghz");

        assertFalse(result, "Preparation should return false when graph_build_info.yml is missing");
        assertFalse(Files.exists(archive), "Archive should NOT be created when graph_build_info.yml is missing");
        assertTrue(Files.exists(profileDir), "Source profile directory should remain when preparation aborted due to missing info file");
    }

    @Test
    void archive_creation_failure_is_handled_and_source_is_kept() throws Exception {
        tempRoot = Files.createTempDirectory("pgu-archivefail");
        Path graphsRoot = tempRoot.resolve("graphs");
        Files.createDirectories(graphsRoot);
        Path profileDir = graphsRoot.resolve("profileC");
        Files.createDirectories(profileDir);

        Files.writeString(profileDir.resolve("graph_build_info.yml"), "info");
        Files.writeString(profileDir.resolve("data.bin"), "payload");

        ProfileProperties props = makeProfileProps(graphsRoot, "profileC", "group", "extent");

        String graphName = String.join("_", "group", "extent", GRAPH_VERSION, props.getEncoderName().toString());
        Path archiveDir = graphsRoot.resolve(graphName + ".ghz");
        // create DIRECTORY where archive file should be to force a failure when opening FileOutputStream
        Files.createDirectories(archiveDir);

        boolean result = RoutingProfile.prepareGeneratedGraphForUpload(props, GRAPH_VERSION);

        assertFalse(result, "Preparation should return false when archive creation fails");
        assertTrue(Files.exists(archiveDir) && Files.isDirectory(archiveDir), "Archive destination was a directory and should remain (archive creation failed)");
        assertTrue(Files.exists(profileDir), "Source profile directory should remain when archive creation fails");
    }

    @Test
    void delete_failure_is_handled_archive_still_exists_and_source_is_kept() throws Exception {
        tempRoot = Files.createTempDirectory("pgu-deletefail");
        Path graphsRoot = tempRoot.resolve("graphs");
        Files.createDirectories(graphsRoot);
        Path profileDir = graphsRoot.resolve("profileD");
        Files.createDirectories(profileDir);

        Files.writeString(profileDir.resolve("graph_build_info.yml"), "info");
        Files.writeString(profileDir.resolve("data.bin"), "payload");

        ProfileProperties props = makeProfileProps(graphsRoot, "profileD", "group", "extent");

        // mock static FileSystemUtils.deleteRecursively to throw IOException when called with profileDir
        boolean result;
        try (MockedStatic<FileSystemUtils> mocked = Mockito.mockStatic(FileSystemUtils.class)) {
            mocked.when(() -> FileSystemUtils.deleteRecursively(profileDir)).thenThrow(new IOException("simulated delete failure"));

            result = RoutingProfile.prepareGeneratedGraphForUpload(props, GRAPH_VERSION);
        }

        String graphName = String.join("_", "group", "extent", GRAPH_VERSION, props.getEncoderName().toString());
        Path archive = graphsRoot.resolve(graphName + ".ghz");

        assertTrue(result, "Preparation should return true even if delete fails");
        assertTrue(Files.exists(archive), "Archive should still be created even if delete fails");
        assertTrue(Files.exists(profileDir), "Source directory should still exist when delete fails (method must catch and log the exception)");
    }
}


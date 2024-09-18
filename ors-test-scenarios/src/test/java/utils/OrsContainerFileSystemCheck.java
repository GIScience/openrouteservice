package utils;

import org.junit.jupiter.api.Assertions;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;

public class OrsContainerFileSystemCheck {

    public static void assertDirectoriesExist(GenericContainer<?> container, String... directoryPaths) throws IOException, InterruptedException {
        for (String directoryPath : directoryPaths) {
            assertDirectoryExists(container, directoryPath, true);
        }
    }

    public static void assertDirectoryExists(GenericContainer<?> container, String directoryPath, Boolean shouldExist) throws IOException, InterruptedException {
        int exitCode = container.execInContainer("test", "-d", directoryPath).getExitCode();
        if (shouldExist) {
            Assertions.assertEquals(0, exitCode, "Directory does not exist: " + directoryPath);
        } else {
            Assertions.assertNotEquals(0, exitCode, "Directory exists: " + directoryPath);
        }
    }

    public static void assertFilesExist(GenericContainer<?> container, String... filePaths) throws IOException, InterruptedException {
        for (String filePath : filePaths) {
            assertFileExists(container, filePath, true);
        }
    }

    public static void assertFileExists(GenericContainer<?> container, String filePath, Boolean shouldExist) throws IOException, InterruptedException {
        int exitCode = container.execInContainer("test", "-f", filePath).getExitCode();
        if (shouldExist) {
            Assertions.assertEquals(0, exitCode, "File does not exist: " + filePath);
        } else {
            Assertions.assertNotEquals(0, exitCode, "File exists: " + filePath);
        }
    }
}

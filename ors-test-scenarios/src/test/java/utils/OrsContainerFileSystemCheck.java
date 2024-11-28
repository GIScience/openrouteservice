package utils;

import org.junit.jupiter.api.Assertions;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;

public class OrsContainerFileSystemCheck {

    public static void assertDirectoryExists(GenericContainer<?> container, String directoryPath, Boolean shouldExist) {
        int exitCode;
        try {
            exitCode = container.execInContainer("test", "-d", directoryPath).getExitCode();
            if (shouldExist) {
                Assertions.assertEquals(0, exitCode, "Directory does not exist: " + directoryPath);
            } else {
                Assertions.assertNotEquals(0, exitCode, "Directory exists: " + directoryPath);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void assertFilesExist(GenericContainer<?> container, String... filePaths) {
        for (String filePath : filePaths) {
            assertFileExists(container, filePath, true);
        }
    }

    public static void assertFileExists(GenericContainer<?> container, String filePath, Boolean shouldExist) {
        int exitCode;
        try {
            exitCode = container.execInContainer("test", "-f", filePath).getExitCode();
            if (shouldExist) {
                Assertions.assertEquals(0, exitCode, "File does not exist: " + filePath);
            } else {
                Assertions.assertNotEquals(0, exitCode, "File exists: " + filePath);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}

package utils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.ShellStrategy;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;

import static utils.ContainerInitializer.initContainerWithSharedGraphs;

/**
 * This test function is a helper function to build the necessary container images for the test scenarios.
 * Ít is supposed to used in CI.
 */
@ExtendWith(TestcontainersExtension.class)
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class OneShotImageBuilderTest {

    @Test
    @Disabled("This test is supposed to be used in CI to cache the images")
    void oneShotImageBuilder() {
        GenericContainer<?> containerWar = initContainerWithSharedGraphs(ContainerInitializer.ContainerTestImageBare.WAR_CONTAINER_BARE, false);
        containerWar.setWaitStrategy(new ShellStrategy());
        containerWar.start();
        Assertions.assertTrue(containerWar.isRunning());
        containerWar.stop();
    }
}

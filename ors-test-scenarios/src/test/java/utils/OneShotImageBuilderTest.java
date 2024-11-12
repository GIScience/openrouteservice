package utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.ShellStrategy;

import static utils.ContainerInitializer.initContainerWithSharedGraphs;

/**
 * This test function is a helper function to build the necessary container images for the test scenarios.
 * Ít is supposed to used in CI.
 */
class OneShotImageBuilderTest {

    @Test
    void oneShotImageBuilder() {
        // Cache one
        GenericContainer<?> containerWar = initContainerWithSharedGraphs(ContainerInitializer.ContainerTestImageBare.WAR_CONTAINER_BARE, false);
        containerWar.setWaitStrategy(new ShellStrategy());
        containerWar.start();
        Assertions.assertTrue(containerWar.isRunning());
        containerWar.stop();
    }
}

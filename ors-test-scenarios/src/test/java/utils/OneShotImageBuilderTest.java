package utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
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
@EnabledIfEnvironmentVariable(named = "ONE_SHOT_IMAGE_BUILDER", matches = "true")
class OneShotImageBuilderTest {

    @Test
    void oneShotImageBuilder() {
        GenericContainer<?> containerWar;
        try {
            containerWar = initContainerWithSharedGraphs(ContainerInitializer.ContainerTestImageBare.WAR_CONTAINER_BARE, false);
            containerWar.setWaitStrategy(new ShellStrategy());
            containerWar.setDockerImageName(ContainerInitializer.ContainerTestImageBare.WAR_CONTAINER_BARE.getName());
            containerWar.start();
        } catch (Exception e) {
            containerWar = initContainerWithSharedGraphs(ContainerInitializer.ContainerTestImageBare.WAR_CONTAINER_BARE, false);
            containerWar.setWaitStrategy(new ShellStrategy());
            containerWar.start();
        }

        Assertions.assertTrue(containerWar.isRunning());
        containerWar.stop();
    }
}

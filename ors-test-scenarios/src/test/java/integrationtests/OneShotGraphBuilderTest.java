package integrationtests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;

import java.io.IOException;

import static utils.ContainerInitializer.buildSharedLayersAndGraphs;

/**
 * This test function is a helper function to build the necessary container images for the test scenarios.
 * Ít is supposed to used in CI.
 */
@ExtendWith(TestcontainersExtension.class)
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@EnabledIfEnvironmentVariable(named = "ONE_SHOT_GRAPH_BUILDER", matches = "true")
class OneShotGraphBuilderTest {

    @Test
    void oneShotGraphBuilder() throws IOException, InterruptedException {
        buildSharedLayersAndGraphs();
        Assertions.assertTrue(true);
    }
}

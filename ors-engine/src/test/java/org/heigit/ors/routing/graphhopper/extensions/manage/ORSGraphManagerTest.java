package org.heigit.ors.routing.graphhopper.extensions.manage;

import org.heigit.ors.routing.graphhopper.extensions.manage.local.FlatORSGraphFolderStrategy;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;
import org.heigit.ors.routing.graphhopper.extensions.manage.remote.NamedGraphsRepoStrategy;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ORSGraphManagerTest {

    @ParameterizedTest
    @CsvSource({
            "true,  true,  repoName, http://my.domain.com",
            "false, true,  repoName,                     ",
            "false, true,          , http://my.domain.com",
            "false, false, repoName, http://my.domain.com",
    })
    void useGraphRepository(boolean expectUseRepo, boolean enable, String repoName, String baseUri) {
        GraphManagementRuntimeProperties managementProps = GraphManagementRuntimeProperties.Builder.empty()
                .withEnabled(enable)
                .withRepoName(repoName)
                .withRepoBaseUri(baseUri)
                .withLocalGraphsRootAbsPath("target/test-output/graphs")
                .withLocalProfileName("useGraphRepository")
                .build();
        ORSGraphManager orsGraphManager = ORSGraphManager.initializeGraphManagement(managementProps);
        assertNotNull(orsGraphManager);
        assertEquals(expectUseRepo, orsGraphManager.useGraphRepository());
    }

    @ParameterizedTest
    @CsvSource({
            "HttpGraphRepoClient, http://my.domain.com",
            "HttpGraphRepoClient, https://my.domain.com/",
            "NullGraphRepoClient, file:relative/path",
            "NullGraphRepoClient, file://relative/path",
            "NullGraphRepoClient, file://relative/path.txt",
            "FileSystemGraphRepoClient, file:///absolute/path",
            "FileSystemGraphRepoClient, file:///absolute/path.txt",
            "FileSystemGraphRepoClient, relative/path",
            "FileSystemGraphRepoClient, relative/path.txt",
            "FileSystemGraphRepoClient, /absolute/path",
            "FileSystemGraphRepoClient, /absolute/path.txt",
            "FileSystemGraphRepoClient, ~/absolute/path",
            "FileSystemGraphRepoClient, ~/absolute/path.txt"
    })
    void getOrsGraphRepoClient(String className, String repoUri) {
        GraphManagementRuntimeProperties managementProps = GraphManagementRuntimeProperties.Builder.empty()
                .withLocalGraphsRootAbsPath("graphs")
                .withRepoBaseUri(repoUri)
                .withGraphVersion("1")
                .withLocalProfileName("driving-car")
                .build();
        FlatORSGraphFolderStrategy orsGraphFolderStrategy = new FlatORSGraphFolderStrategy(managementProps);
        ORSGraphFileManager orsGraphFileManager = new ORSGraphFileManager(managementProps, orsGraphFolderStrategy);
        NamedGraphsRepoStrategy orsGraphRepoStrategy = new NamedGraphsRepoStrategy(managementProps);
        assertEquals(className, ORSGraphManager.getOrsGraphRepoClient(managementProps, orsGraphRepoStrategy, orsGraphFileManager).getClass().getSimpleName());
    }
}
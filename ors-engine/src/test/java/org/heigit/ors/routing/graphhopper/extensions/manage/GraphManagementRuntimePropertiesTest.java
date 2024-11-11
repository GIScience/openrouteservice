package org.heigit.ors.routing.graphhopper.extensions.manage;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class GraphManagementRuntimePropertiesTest {

    @ParameterizedTest
    @CsvSource({
            "http://my.domain.com, HTTP",
            "https://my.domain.com/, HTTP",
            "file://relative/path, NULL",
            "file://relative/path.txt, NULL",
            "file:///absolute/path, FILESYSTEM",
            "file:///absolute/path.txt, FILESYSTEM",
            "relative/path, FILESYSTEM",
            "relative/path.txt, FILESYSTEM",
            "/absolute/path, FILESYSTEM",
            "/absolute/path.txt, FILESYSTEM",
            "~/absolute/path, FILESYSTEM",
            "~/absolute/path.txt, FILESYSTEM"
    })
    void deriveRepoBaseUrl(String repoBaseUri, GraphManagementRuntimeProperties.GraphRepoType expectedType) {
        GraphManagementRuntimeProperties managementRuntimeProperties = GraphManagementRuntimeProperties.Builder.empty().withRepoBaseUri(repoBaseUri).build();
        assertEquals(expectedType, managementRuntimeProperties.getDerivedRepoType());
        switch (expectedType) {
            case HTTP:
                assertNotNull(managementRuntimeProperties.getDerivedRepoBaseUrl());
                assertNull(managementRuntimeProperties.getDerivedRepoPath());
                break;
            case FILESYSTEM:
                assertNull(managementRuntimeProperties.getDerivedRepoBaseUrl());
                assertNotNull(managementRuntimeProperties.getDerivedRepoPath());
                break;
            case NULL:
                assertNull(managementRuntimeProperties.getDerivedRepoBaseUrl());
                assertNull(managementRuntimeProperties.getDerivedRepoPath());
                break;
        }
    }

}
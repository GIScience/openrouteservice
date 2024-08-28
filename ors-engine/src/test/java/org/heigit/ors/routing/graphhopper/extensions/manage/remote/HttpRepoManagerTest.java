package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.FlatORSGraphFolderStrategy;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class HttpRepoManagerTest {

    @Test
    void concatenateToUrl() {
        assertEquals("https://my.domain.com/repo/group1/germany/0/graph.ghz",
                HttpRepoManager.concatenateToUrlPath("https://my.domain.com", "repo", "/group1", "/germany/", "0", "./", "graph.ghz/"));
    }

    @Test
    void createDownloadUrl() {
        GraphManagementRuntimeProperties managementProps = GraphManagementRuntimeProperties.Builder.fromNew()
                .withRepoBaseUri("http://localhost:8080/")
                .withRepoName("repo")
                .withRepoProfileGroup("group1")
                .withRepoCoverage("germany")
                .withGraphVersion("0")
                .build();

        FlatORSGraphFolderStrategy orsGraphFolderStrategy = new FlatORSGraphFolderStrategy(managementProps);
        ORSGraphFileManager orsGraphFileManager = new ORSGraphFileManager(managementProps, orsGraphFolderStrategy);
        ORSGraphRepoStrategy orsGraphRepoStrategy = new NamedGraphsRepoStrategy(managementProps);
        HttpRepoManager httpRepoManager = new HttpRepoManager(managementProps, orsGraphRepoStrategy, orsGraphFileManager);
        URL downloadUrl = httpRepoManager.createDownloadUrl( "graph.ghz");
        assertEquals("http://localhost:8080/repo/group1/germany/0/graph.ghz", downloadUrl.toString());
    }
}
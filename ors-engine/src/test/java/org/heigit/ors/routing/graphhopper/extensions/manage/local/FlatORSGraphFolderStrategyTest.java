package org.heigit.ors.routing.graphhopper.extensions.manage.local;

import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlatORSGraphFolderStrategyTest {

    FlatORSGraphFolderStrategy strategy;

    @BeforeEach
    public void setUp() {
        GraphManagementRuntimeProperties managementProps = GraphManagementRuntimeProperties.Builder.empty()
                .withRepoName("vendor.org")
                .withRepoProfileGroup("fun")
                .withRepoCoverage("disneyland")
                .withLocalGraphsRootAbsPath("/data/graphs")
                .withLocalProfileName("bobbycar")
                .withGraphVersion("0")
                .build();

        strategy = new FlatORSGraphFolderStrategy(managementProps);
    }

    @Test
    void getProfileDescriptiveName() {
        assertEquals("bobbycar", strategy.getProfileDescriptiveName());
    }

    @Test
    void getGraphInfoFileNameInRepository() {
        assertEquals("bobbycar.yml", strategy.getGraphInfoFileNameInRepository());
    }

    @Test
    void getGraphsRootDirName() {
        assertEquals("graphs", strategy.getGraphsRootDirName());
    }

    @Test
    void getGraphsRootDirAbsPath() {
        assertEquals("/data/graphs", strategy.getGraphsRootDirAbsPath());
    }

    @Test
    void getProfileGraphsDirName() {
        assertEquals("graphs", strategy.getProfileGraphsDirName());
    }

    @Test
    void getProfileGraphsDirAbsPath() {
        assertEquals("/data/graphs", strategy.getProfileGraphsDirAbsPath());
    }

    @Test
    void getActiveGraphDirName() {
        assertEquals("bobbycar", strategy.getActiveGraphDirName());
    }

    @Test
    void getActiveGraphDirAbsPath() {
        assertEquals("/data/graphs/bobbycar", strategy.getActiveGraphDirAbsPath());
    }

    @Test
    void getActiveGraphInfoFileName() {
        assertEquals("graph_info.yml", strategy.getActiveGraphInfoFileName());
    }

    @Test
    void getDownloadedGraphInfoFileName() {
        assertEquals("vendor.org_fun_disneyland_0_bobbycar.yml", strategy.getDownloadedGraphInfoFileName());
    }

    @Test
    void getDownloadedGraphInfoFileAbsPath() {
        assertEquals("/data/graphs/vendor.org_fun_disneyland_0_bobbycar.yml", strategy.getDownloadedGraphInfoFileAbsPath());
    }

    @Test
    void getDownloadedCompressedGraphFileName() {
        assertEquals("vendor.org_fun_disneyland_0_bobbycar.ghz", strategy.getDownloadedCompressedGraphFileName());
    }

    @Test
    void getDownloadedCompressedGraphFileAbsPath() {
        assertEquals("/data/graphs/vendor.org_fun_disneyland_0_bobbycar.ghz", strategy.getDownloadedCompressedGraphFileAbsPath());
    }

    @Test
    void getDownloadedExtractedGraphDirName() {
        assertEquals("bobbycar_new", strategy.getDownloadedExtractedGraphDirName());
    }

    @Test
    void getDownloadedExtractedGraphDirAbsPath() {
        assertEquals("/data/graphs/bobbycar_new", strategy.getDownloadedExtractedGraphDirAbsPath());
    }
}
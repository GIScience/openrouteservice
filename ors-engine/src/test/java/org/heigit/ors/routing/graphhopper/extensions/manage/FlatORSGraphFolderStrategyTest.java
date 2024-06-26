package org.heigit.ors.routing.graphhopper.extensions.manage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlatORSGraphFolderStrategyTest {

    FlatORSGraphFolderStrategy strategy = new FlatORSGraphFolderStrategy("/data/graphs", "bobbycar", "the-repo");

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
        assertEquals("bobbycar_the-repo.yml", strategy.getDownloadedGraphInfoFileName());
    }

    @Test
    void getDownloadedGraphInfoFileAbsPath() {
        assertEquals("/data/graphs/bobbycar_the-repo.yml", strategy.getDownloadedGraphInfoFileAbsPath());
    }

    @Test
    void getDownloadedCompressedGraphFileName() {
        assertEquals("bobbycar_the-repo.ghz", strategy.getDownloadedCompressedGraphFileName());
    }

    @Test
    void getDownloadedCompressedGraphFileAbsPath() {
        assertEquals("/data/graphs/bobbycar_the-repo.ghz", strategy.getDownloadedCompressedGraphFileAbsPath());
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
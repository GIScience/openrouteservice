package org.heigit.ors.routing.graphhopper.extensions.manage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashSubDirBasedORSGraphFolderStrategyTest {

    HashSubDirBasedORSGraphFolderStrategy strategy = new HashSubDirBasedORSGraphFolderStrategy("/data/graphs", "scooter", "0f431af8efb8c4c2aafda05a781d977d");

    @Test
    void getProfileDescriptiveName() {
        assertEquals("scooter/0f431af8efb8c4c2aafda05a781d977d", strategy.getProfileDescriptiveName());
    }

    @Test
    void getGraphInfoFileNameInRepository() {
        assertEquals("0f431af8efb8c4c2aafda05a781d977d.yml", strategy.getGraphInfoFileNameInRepository());
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
        assertEquals("scooter", strategy.getProfileGraphsDirName());
    }

    @Test
    void getProfileGraphsDirAbsPath() {
        assertEquals("/data/graphs/scooter", strategy.getProfileGraphsDirAbsPath());
    }

    @Test
    void getActiveGraphDirName() {
        assertEquals("0f431af8efb8c4c2aafda05a781d977d", strategy.getActiveGraphDirName());
    }

    @Test
    void getActiveGraphDirAbsPath() {
        assertEquals("/data/graphs/scooter/0f431af8efb8c4c2aafda05a781d977d", strategy.getActiveGraphDirAbsPath());
    }

    @Test
    void getActiveGraphInfoFileName() {
        assertEquals("0f431af8efb8c4c2aafda05a781d977d.yml", strategy.getActiveGraphInfoFileName());
    }

    @Test
    void getDownloadedGraphInfoFileName() {
        assertEquals("0f431af8efb8c4c2aafda05a781d977d.yml", strategy.getDownloadedGraphInfoFileName());
    }

    @Test
    void getDownloadedGraphInfoFileAbsPath() {
        assertEquals("/data/graphs/scooter/0f431af8efb8c4c2aafda05a781d977d.yml", strategy.getDownloadedGraphInfoFileAbsPath());
    }

    @Test
    void getDownloadedCompressedGraphFileName() {
        assertEquals("0f431af8efb8c4c2aafda05a781d977d.ghz", strategy.getDownloadedCompressedGraphFileName());
    }

    @Test
    void getDownloadedCompressedGraphFileAbsPath() {
        assertEquals("/data/graphs/scooter/0f431af8efb8c4c2aafda05a781d977d.ghz", strategy.getDownloadedCompressedGraphFileAbsPath());
    }

    @Test
    void getDownloadedExtractedGraphDirName() {
        assertEquals("0f431af8efb8c4c2aafda05a781d977d_new", strategy.getDownloadedExtractedGraphDirName());
    }

    @Test
    void getDownloadedExtractedGraphDirAbsPath() {
        assertEquals("/data/graphs/scooter/0f431af8efb8c4c2aafda05a781d977d_new", strategy.getDownloadedExtractedGraphDirAbsPath());
    }
}
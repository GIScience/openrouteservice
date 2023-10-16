package org.heigit.ors.routing.graphhopper.extensions.manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.util.Unzipper;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class ORSGraphFileManager {

    private static final Logger LOGGER = Logger.getLogger(ORSGraphFileManager.class.getName());
    private static final String GRAPH_DOWNLOAD_FILE_EXTENSION = "ghz";
    private static final String GRAPH_INFO_FILE_EXTENSION = "json";
    private static final String INCOMPLETE_EXTENSION = "incomplete";
    private String hash;
    private String hashDirAbsPath;
    private String vehicleGraphDirAbsPath;
    private String routeProfileName;


    public ORSGraphFileManager(String hash, String hashDirAbsPath, String vehicleGraphDirAbsPath, String routeProfileName) {
        this.hash = hash;
        this.hashDirAbsPath = hashDirAbsPath;
        this.vehicleGraphDirAbsPath = vehicleGraphDirAbsPath;
        this.routeProfileName = routeProfileName;
    }

    public String getHash() {
        return hash;
    }

    public String getHashDirAbsPath() {
        return hashDirAbsPath;
    }

    public String getVehicleGraphDirAbsPath() {
        return vehicleGraphDirAbsPath;
    }

    public String getRouteProfileName() {
        return routeProfileName;
    }

    String createDynamicGraphInfoFileName() {
        return hash + "." + GRAPH_INFO_FILE_EXTENSION;
    }

    String createDynamicGraphDownloadFileName() {
        return hash + "." + GRAPH_DOWNLOAD_FILE_EXTENSION;
    }

    String createDynamicGraphDownloadFileName(String basename) {
        return basename + "." + GRAPH_DOWNLOAD_FILE_EXTENSION;
    }

    String createExtractionDirectoryName() {
        return hash + "_new";//jh: TODO: use date value instead of "new"?
    }

    File getExractionDirectory() {
        String extractionDirectoryName = createExtractionDirectoryName();
        return new File(vehicleGraphDirAbsPath, extractionDirectoryName);
    }

    File getGraphDownloadFile() {
        String downloadFileName = createDynamicGraphDownloadFileName();
        return new File(vehicleGraphDirAbsPath, downloadFileName);
    }

    File getGraphInfoDownloadFile() {
        String infoFileName = createDynamicGraphInfoFileName();
        return new File(vehicleGraphDirAbsPath, infoFileName);
    }

    private File getHashDirectory() {
        return new File(hashDirAbsPath);
    }

    boolean hasLocalGraph() {
        return getHashDirectory().exists() && getHashDirectory().isDirectory();
    }

    public boolean hasDownloadedExtractedGraph() {
        return getExractionDirectory().exists() && getExractionDirectory().isDirectory();
    }

    File asIncompleteFile(File file){
        return new File(file.getAbsolutePath() + "." + INCOMPLETE_EXTENSION);
    }

    File asIncompleteDirectory(File directory){
        return new File(directory.getAbsolutePath() + "_" + INCOMPLETE_EXTENSION);
    }

    void cleanupIncompleteFiles() {
        File incompleteDownloadFile = asIncompleteFile(getGraphDownloadFile());
        if (incompleteDownloadFile.exists()) {
            incompleteDownloadFile.delete();
        }

        File graphInfoDownloadFile = getGraphInfoDownloadFile();
        if (graphInfoDownloadFile.exists()) {
            graphInfoDownloadFile.delete();
        }

        File incompleteGraphInfoDownloadFile = asIncompleteFile(getGraphInfoDownloadFile());
        if (incompleteGraphInfoDownloadFile.exists()) {
            incompleteGraphInfoDownloadFile.delete();
        }
    }

    String createGraphUrlFromGraphInfoUrl(GraphInfo remoteGraphInfo) {
        String url = remoteGraphInfo.getRemoteUrl().toString();
        String urlWithoutExtension = url.substring(0, url.lastIndexOf('.'));
        return urlWithoutExtension + "." + GRAPH_DOWNLOAD_FILE_EXTENSION;
    }

    void backupExistingGraph() {
        backupExistingGraph(getHashDirectory());
    }


    void backupExistingGraph(File hashDirectory) {
        String origAbsPath = hashDirectory.getAbsolutePath();
        String newAbsPath = hashDirectory.getAbsolutePath() + "_bak";
        File backupFile = new File(newAbsPath);

        if (backupFile.exists()){
            LOGGER.debug("deleting old backup directory %s".formatted(newAbsPath));
            try {
                FileUtils.deleteDirectory(backupFile);
                backupFile = new File(newAbsPath);
            } catch (IOException e) {
                LOGGER.warn("old backup directory %s could not be deleted, caught %s".formatted(newAbsPath, e.getMessage()));
            }
        }

        if (hashDirectory.renameTo(backupFile)) {
            LOGGER.info("renamed old local graph directory %s to %s".formatted(origAbsPath, newAbsPath));
        } else {
            LOGGER.error("could not backup local graph directory %s to %s".formatted(origAbsPath, newAbsPath));
        }
    }

    GraphInfo getLocalGraphInfo() {
        LOGGER.debug("Checking local graph info for %s...".formatted(routeProfileName));
        File localDir = new File(hashDirAbsPath);

        if (!localDir.exists()) {
            LOGGER.debug("No local graph directory for %s found.".formatted(routeProfileName));
            return new GraphInfo().withLocalDirectory(localDir);
        }

        if (!localDir.isDirectory()) {
            throw new IllegalArgumentException("GraphHopperLocation cannot be an existing file. Has to be either non-existing or a folder.");
        }

        File graphInfoFile = new File(localDir, createDynamicGraphInfoFileName());
        if (!graphInfoFile.exists() || !graphInfoFile.isFile()) {
            LOGGER.debug("No %s found in %s".formatted(graphInfoFile.getName(), hashDirAbsPath));
            return new GraphInfo().withLocalDirectory(localDir);
        }

        ORSGraphInfoV1 graphInfoV1 = readOrsGraphInfoV1(graphInfoFile);
        LOGGER.debug("Found local graph info for %s with osmDate=%s".formatted(routeProfileName, graphInfoV1.getOsmDate()));
        return new GraphInfo().withLocalDirectory(localDir).withPersistedInfo(graphInfoV1);
    }

    ORSGraphInfoV1 readOrsGraphInfoV1(File inputFile) {
        try {
            return new ObjectMapper().readValue(inputFile, ORSGraphInfoV1.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void writeOrsGraphInfoV1(ORSGraphInfoV1 orsGraphInfoV1, File outputFile) {
        try {
            new ObjectMapper().writeValue(outputFile, orsGraphInfoV1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    ORSGraphInfoV1 getPreviouslyDownloadedRemoteGraphInfo() {
        LOGGER.debug("Checking graph info for %s of previous check ...".formatted(routeProfileName));
        String fileName = createDynamicGraphInfoFileName();
        File persistedGraphInfoFile = new File(vehicleGraphDirAbsPath, fileName);
        if (persistedGraphInfoFile.exists()) {
            return readOrsGraphInfoV1(persistedGraphInfoFile);
        }
        return null;
    }

    public void activateNewGraph() {
        getExractionDirectory().renameTo(getHashDirectory());
    }

    public boolean isActive() {
        return asIncompleteFile(getGraphDownloadFile()).exists() ||
                asIncompleteFile(getExractionDirectory()).exists();
    }

    public void extractDownloadedGraph() {
        try {
            (new Unzipper()).unzip(getGraphDownloadFile().getAbsolutePath(), getExractionDirectory().getAbsolutePath(), true);
        } catch (IOException ioException) {
            throw new RuntimeException("Couldn't extract file " + getGraphDownloadFile().getAbsolutePath() + " to " + getExractionDirectory().getAbsolutePath(), ioException);
        }

    }
}


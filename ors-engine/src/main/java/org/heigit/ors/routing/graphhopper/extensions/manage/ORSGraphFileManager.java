package org.heigit.ors.routing.graphhopper.extensions.manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.util.Unzipper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.log4j.Logger;
import org.heigit.ors.config.EngineConfig;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ORSGraphFileManager {

    private static final Logger LOGGER = Logger.getLogger(ORSGraphFileManager.class.getName());
    private static final String GRAPH_DOWNLOAD_FILE_EXTENSION = "ghz";
    private static final String GRAPH_INFO_FILE_EXTENSION = "json";
    private static final String INCOMPLETE_EXTENSION = "incomplete";
    private String hash;
    private String hashDirAbsPath;
    private String vehicleGraphDirAbsPath;
    private String routeProfileName;
    private int maxNumberOfGraphBackups;

    public ORSGraphFileManager(EngineConfig engineConfig, String hash, String hashDirAbsPath, String vehicleGraphDirAbsPath, String routeProfileName) {
        this.hash = hash;
        this.hashDirAbsPath = hashDirAbsPath;
        this.vehicleGraphDirAbsPath = vehicleGraphDirAbsPath;
        this.routeProfileName = routeProfileName;
        int maxBak = engineConfig.getMaxNumberOfGraphBackups();
        this.maxNumberOfGraphBackups = Math.max(maxBak, 0);
    }

    public void initialize() {
        File vehicleGraphDir = new File(hashDirAbsPath);
        if (!vehicleGraphDir.exists()) {
            LOGGER.info("[%s] Creating vehicle graph directory %s".formatted(getProfileWithHash(), hashDirAbsPath));
            if (!vehicleGraphDir.mkdirs()) {
                LOGGER.error("[%s] Could not create vehicle graph directory %s".formatted(getProfileWithHash(), hashDirAbsPath));
            }
        }
    }

    public String getHash() {
        return hash;
    }

    private File getHashDirectory() {
        return new File(hashDirAbsPath);
    }

    public String getVehicleGraphDirAbsPath() {
        return vehicleGraphDirAbsPath;
    }

    String createGraphInfoFileName() {
        return hash + "." + GRAPH_INFO_FILE_EXTENSION;
    }

    String createGraphDownloadFileName() {
        return hash + "." + GRAPH_DOWNLOAD_FILE_EXTENSION;
    }

    String createGraphExtractionDirectoryName() {
        return hash + "_new";
    }

    String getProfileWithHash() {return routeProfileName + "/" + hash;}

    File getGraphInfoDownloadFile() {
        String infoFileName = createGraphInfoFileName();
        return new File(vehicleGraphDirAbsPath, infoFileName);
    }

    File getGraphDownloadFile() {
        String downloadFileName = createGraphDownloadFileName();
        return new File(vehicleGraphDirAbsPath, downloadFileName);
    }

    File getGraphExractionDirectory() {
        String graphExtractionDirectoryName = createGraphExtractionDirectoryName();
        return new File(vehicleGraphDirAbsPath, graphExtractionDirectoryName);
    }

    boolean hasLocalGraph() {
        return getHashDirectory().exists() && getHashDirectory().isDirectory();
    }

    boolean hasGraphDownloadFile() {
        return getGraphDownloadFile().exists();
    }

    public boolean hasDownloadedExtractedGraph() {
        return getGraphExractionDirectory().exists() && getGraphExractionDirectory().isDirectory();
    }

    File asIncompleteFile(File file){
        return new File(file.getAbsolutePath() + "." + INCOMPLETE_EXTENSION);
    }

    File asIncompleteDirectory(File directory){
        return new File(directory.getAbsolutePath() + "_" + INCOMPLETE_EXTENSION);
    }

    public boolean isActive() {
        return asIncompleteFile(getGraphDownloadFile()).exists() ||
                asIncompleteFile(getGraphInfoDownloadFile()).exists() ||
                asIncompleteFile(getGraphExractionDirectory()).exists();
    }

    void cleanupIncompleteFiles() {
        File incompleteDownloadFile = asIncompleteFile(getGraphDownloadFile());
        if (incompleteDownloadFile.exists()) {
            LOGGER.info("[%s] Deleted incomplete graph download file from previous application run: %s".formatted(getProfileWithHash(), incompleteDownloadFile.getAbsolutePath()));
            incompleteDownloadFile.delete();
        }

        File graphInfoDownloadFile = getGraphInfoDownloadFile();
        if (graphInfoDownloadFile.exists()) {
            LOGGER.info("[%s] Deleted graph-info download file from previous application run: %s".formatted(getProfileWithHash(), graphInfoDownloadFile.getAbsolutePath()));
            graphInfoDownloadFile.delete();
        }

        File incompleteGraphInfoDownloadFile = asIncompleteFile(getGraphInfoDownloadFile());
        if (incompleteGraphInfoDownloadFile.exists()) {
            LOGGER.info("[%s] Deleted incomplete graph download file from previous application run: %s".formatted(getProfileWithHash(), incompleteGraphInfoDownloadFile.getAbsolutePath()));
            incompleteGraphInfoDownloadFile.delete();
        }

        File incompleteExtractionFolder = asIncompleteDirectory(getGraphExractionDirectory());
        if (incompleteExtractionFolder.exists() && incompleteExtractionFolder.isDirectory()) {
            try {
                FileUtils.deleteDirectory(incompleteExtractionFolder);
                LOGGER.info("[%s] Deleted incomplete graph graph extraction folder from previous application run: %s".formatted(getProfileWithHash(), incompleteExtractionFolder.getAbsolutePath()));
            } catch (IOException e) {
                LOGGER.error("[%s] Could not delete incomplete graph extraction folder from previous application run: %s".formatted(getProfileWithHash(), incompleteExtractionFolder.getAbsolutePath()));
            }
        }
    }

    String createGraphUrlFromGraphInfoUrl(GraphInfo remoteGraphInfo) {
        String url = remoteGraphInfo.getRemoteUrl().toString();
        String urlWithoutExtension = url.substring(0, url.lastIndexOf('.'));
        return urlWithoutExtension + "." + GRAPH_DOWNLOAD_FILE_EXTENSION;
    }

    void backupExistingGraph() {
        if (!hasLocalGraph()) {
            deleteOldestBackups();
            return;
        }
        File hashDirectory = getHashDirectory();
        String origAbsPath = hashDirectory.getAbsolutePath();
        String dateString = DateTimeFormatter.ofPattern("uuuu-MM-dd_HHmmss", Locale.getDefault()).format(LocalDateTime.now());
        String newAbsPath = hashDirectory.getAbsolutePath() + "_" + dateString;
        File backupFile = new File(newAbsPath);

        if (backupFile.exists()){
            LOGGER.debug("[%s] Deleting old backup directory %s".formatted(getProfileWithHash(), newAbsPath));
            try {
                FileUtils.deleteDirectory(backupFile);
                backupFile = new File(newAbsPath);
            } catch (IOException e) {
                LOGGER.warn("[%s] Old backup directory %s could not be deleted, caught %s".formatted(getProfileWithHash(), newAbsPath, e.getMessage()));
            }
        }

        if (hashDirectory.renameTo(backupFile)) {
            LOGGER.info("[%s] Renamed old local graph directory %s to %s".formatted(getProfileWithHash(), origAbsPath, newAbsPath));
        } else {
            LOGGER.error("[%s] Could not backup local graph directory %s to %s".formatted(getProfileWithHash(), origAbsPath, newAbsPath));
        }
        deleteOldestBackups();
    }

    void deleteOldestBackups() {
        List<File> existingBackups = findGraphBackupsSortedByName();
        int numBackupsToDelete = existingBackups.size() - Math.max(maxNumberOfGraphBackups, 0);
        if (numBackupsToDelete < 1) {
            return;
        }
        List<File> backupsToDelete = existingBackups.subList(0, numBackupsToDelete);
        for (File backupFile : backupsToDelete) {
            try {
                LOGGER.debug("[%s] Deleting old backup directory %s".formatted(getProfileWithHash(), backupFile.getAbsolutePath()));
                FileUtils.deleteDirectory(backupFile);
            } catch (IOException e) {
                LOGGER.warn("[%s] Old backup directory %s could not be deleted, caught %s".formatted(getProfileWithHash(), backupFile.getAbsolutePath(), e.getMessage()));
            }
        }
    }

    List<File> findGraphBackupsSortedByName() {
        File vehicleDir = new File(getVehicleGraphDirAbsPath());
        FilenameFilter filter = new RegexFileFilter("^%s_\\d{4}-\\d{2}-\\d{2}_\\d{6}$".formatted(hash));
        File[] obj = vehicleDir.listFiles(filter);
        if (obj == null)
            return Collections.emptyList();

        return Arrays.asList(Objects.requireNonNull(obj)).stream().sorted(Comparator.comparing(File::getName)).toList();
    }

    GraphInfo getLocalGraphInfo() {
        LOGGER.debug("[%s] Checking local graph info...".formatted(getProfileWithHash()));
        File hashDirectory = getHashDirectory();

        if (!hasLocalGraph()) {
            LOGGER.debug("[%s] No local graph directory found".formatted(getProfileWithHash()));
            return new GraphInfo().withLocalDirectory(hashDirectory);
        }

        File graphInfoFile = new File(hashDirectory, createGraphInfoFileName());
        if (!graphInfoFile.exists() || !graphInfoFile.isFile()) {
            LOGGER.debug("[%s] No graph info file %s found in %s".formatted(getProfileWithHash(), graphInfoFile.getName(), hashDirAbsPath));
            return new GraphInfo().withLocalDirectory(hashDirectory);
        }

        ORSGraphInfoV1 graphInfoV1 = readOrsGraphInfoV1(graphInfoFile);
        LOGGER.debug("[%s] Found local graph info with osmDate=%s".formatted(getProfileWithHash(), graphInfoV1.getOsmDate()));
        return new GraphInfo().withLocalDirectory(hashDirectory).withPersistedInfo(graphInfoV1);
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
        LOGGER.debug("[%s] Checking graph info for of previous check ...".formatted(getProfileWithHash()));
        String fileName = createGraphInfoFileName();
        File persistedGraphInfoFile = new File(vehicleGraphDirAbsPath, fileName);
        if (persistedGraphInfoFile.exists()) {
            return readOrsGraphInfoV1(persistedGraphInfoFile);
        }
        return null;
    }

    public void activateNewGraph() {
        LOGGER.info("[%s] Activating extracted downloaded graph".formatted(getProfileWithHash()));
        getGraphExractionDirectory().renameTo(getHashDirectory());
    }

    public void extractDownloadedGraph() {
        if (!hasGraphDownloadFile()){
            LOGGER.debug("[%s] No downloaded graph to extract".formatted(getProfileWithHash()));
            return;
        }

        File graphDownloadFile = getGraphDownloadFile();
        String graphDownloadFileAbsPath = graphDownloadFile.getAbsolutePath();
        File targetDirectory = getGraphExractionDirectory();
        String targetDirectoryAbsPath = targetDirectory.getAbsolutePath();
        File extractionDirectory = asIncompleteDirectory(targetDirectory);
        String extractionDirectoryAbsPath = extractionDirectory.getAbsolutePath();

        if (extractionDirectory.exists()){
            LOGGER.debug("[%s] Extraction already started".formatted(getProfileWithHash()));
            return;
        }

        try {

            LOGGER.debug("[%s] Extracting downloaded graph file to %s".formatted(getProfileWithHash(), extractionDirectoryAbsPath));
            long start = System.currentTimeMillis();
            (new Unzipper()).unzip(graphDownloadFileAbsPath, extractionDirectoryAbsPath, true);
            long end = System.currentTimeMillis();

            LOGGER.debug("[%s] Extraction of downloaded graph file finished after %d ms, deleting downloaded graph file %s".formatted(
                    getProfileWithHash(),
                    end-start,
                    graphDownloadFileAbsPath));
            graphDownloadFile.delete();

            LOGGER.debug("[%s] Renaming extraction directory to %s".formatted(
                    getProfileWithHash(),
                    targetDirectoryAbsPath));
            extractionDirectory.renameTo(targetDirectory);

        } catch (IOException ioException) {
            LOGGER.error("[%s] Error during extraction of %s to %s -> %s".formatted(
                    getProfileWithHash(),
                    graphDownloadFileAbsPath,
                    extractionDirectoryAbsPath,
                    targetDirectoryAbsPath));
            throw new RuntimeException("Caught ", ioException);
        }
    }
}
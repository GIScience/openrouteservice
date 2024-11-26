package org.heigit.ors.routing.graphhopper.extensions.manage.local;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.graphhopper.GraphHopper;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Unzipper;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.exceptions.ORSGraphFileManagerException;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphBuildInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.PersistedGraphBuildInfo;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.*;

@NoArgsConstructor
public class ORSGraphFileManager implements ORSGraphFolderStrategy {

    private static final Logger LOGGER = Logger.getLogger(ORSGraphFileManager.class.getName());

    GraphManagementRuntimeProperties graphManagementRuntimeProperties;
    private ORSGraphFolderStrategy orsGraphFolderStrategy;

    public ORSGraphFileManager(GraphManagementRuntimeProperties graphManagementRuntimeProperties, ORSGraphFolderStrategy orsGraphFolderStrategy) {
        this.graphManagementRuntimeProperties = graphManagementRuntimeProperties;
        this.orsGraphFolderStrategy = orsGraphFolderStrategy;
    }

    public void initialize() {
        File activeGraphDirectory = getActiveGraphDirectory();
        if (!activeGraphDirectory.exists()) {
            LOGGER.debug("[%s] Creating graph directory %s".formatted(getProfileDescriptiveName(), activeGraphDirectory.getAbsolutePath()));
            if (!activeGraphDirectory.mkdirs()) {
                LOGGER.error("[%s] Could not create graph directory %s".formatted(getProfileDescriptiveName(), activeGraphDirectory.getAbsolutePath()));
            }
        }
    }

    public boolean hasActiveGraph() {
        return isExistingDirectoryWithFiles(getActiveGraphDirectory());
    }

    public boolean hasActiveGraphDirectory() {
        return isExistingDirectory(getActiveGraphDirectory());
    }

    public boolean hasGraphDownloadFile() {
        return getDownloadedCompressedGraphFile().exists();
    }

    public boolean hasDownloadedExtractedGraph() {
        return isExistingDirectoryWithFiles(getDownloadedExtractedGraphDirectory());
    }

    boolean isExistingDirectory(File directory) {
        return directory.exists() && directory.isDirectory();
    }

    boolean isExistingDirectoryWithFiles(File directory) {
        return isExistingDirectory(directory) && directory.listFiles().length > 0;
    }

    public File asIncompleteFile(File file) {
        return new File(file.getAbsolutePath() + "." + INCOMPLETE_EXTENSION);
    }

    File asIncompleteDirectory(File directory) {
        return new File(directory.getAbsolutePath() + "_" + INCOMPLETE_EXTENSION);
    }

    public boolean isBusy() {
        return asIncompleteFile(getDownloadedCompressedGraphFile()).exists() ||
                asIncompleteFile(getDownloadedGraphBuildInfoFile()).exists() ||
                asIncompleteFile(getDownloadedExtractedGraphDirectory()).exists();
    }

    private void deleteFileWithLogging(File file, String successMessage, String errorMessage) {
        try {
            if (Files.deleteIfExists(file.toPath()))
                LOGGER.debug(successMessage.formatted(getProfileDescriptiveName(), file.getAbsolutePath()));
        } catch (IOException e) {
            LOGGER.error(errorMessage.formatted(e.getMessage()));
        }
    }

    public void cleanupIncompleteFiles() {
        File incompleteDownloadFile = asIncompleteFile(getDownloadedCompressedGraphFile());
        deleteFileWithLogging(incompleteDownloadFile,
                "[%s] Deleted incomplete graph download file from previous application run: %s",
                "Error deleting incomplete download file: %s");

        File graphBuildInfoDownloadFile = getDownloadedGraphBuildInfoFile();
        deleteFileWithLogging(graphBuildInfoDownloadFile,
                "[%s] Deleted graph-info download file from previous application run: %s",
                "Error deleting graph-info download file: %s");

        File incompleteGraphBuildInfoDownloadFile = asIncompleteFile(getDownloadedGraphBuildInfoFile());
        deleteFileWithLogging(incompleteGraphBuildInfoDownloadFile,
                "[%s] Deleted incomplete graph download file from previous application run: %s",
                "Error deleting incomplete graph download file: %s");

        File incompleteExtractionFolder = asIncompleteDirectory(getDownloadedExtractedGraphDirectory());
        if (incompleteExtractionFolder.exists() && incompleteExtractionFolder.isDirectory()) {
            try {
                FileUtils.deleteDirectory(incompleteExtractionFolder);
                LOGGER.debug("[%s] Deleted incomplete graph graph extraction folder from previous application run: %s".formatted(getProfileDescriptiveName(), incompleteExtractionFolder.getAbsolutePath()));
            } catch (IOException e) {
                LOGGER.error("[%s] Could not delete incomplete graph extraction folder from previous application run: %s".formatted(getProfileDescriptiveName(), incompleteExtractionFolder.getAbsolutePath()));
            }
        }
    }

    public void backupExistingGraph() {
        if (!hasActiveGraph()) {
            deleteOldestBackups();
            return;
        }
        File activeGraphDirectory = getActiveGraphDirectory();
        String origAbsPath = activeGraphDirectory.getAbsolutePath();
        String dateString = DateTimeFormatter.ofPattern("uuuu-MM-dd_HHmmss", Locale.getDefault()).format(LocalDateTime.now());
        String newAbsPath = activeGraphDirectory.getAbsolutePath() + "_" + dateString;
        File backupFile = new File(newAbsPath);

        if (backupFile.exists()) {
            LOGGER.debug("[%s] Deleting old backup directory %s".formatted(getProfileDescriptiveName(), newAbsPath));
            try {
                FileUtils.deleteDirectory(backupFile);
                backupFile = new File(newAbsPath);
            } catch (IOException e) {
                LOGGER.warn("[%s] Old backup directory %s could not be deleted, caught %s".formatted(getProfileDescriptiveName(), newAbsPath, e.getMessage()));
            }
        }

        if (activeGraphDirectory.renameTo(backupFile)) {
            LOGGER.debug("[%s] Renamed old local graph directory %s to %s".formatted(getProfileDescriptiveName(), origAbsPath, newAbsPath));
        } else {
            LOGGER.error("[%s] Could not backup local graph directory %s to %s".formatted(getProfileDescriptiveName(), origAbsPath, newAbsPath));
        }
        deleteOldestBackups();
    }

    public void deleteOldestBackups() {
        List<File> existingBackups = findGraphBackupsSortedByName();
        int numBackupsToDelete = existingBackups.size() - Math.max(graphManagementRuntimeProperties.getMaxNumberOfGraphBackups(), 0);
        if (numBackupsToDelete < 1) {
            return;
        }
        List<File> backupsToDelete = existingBackups.subList(0, numBackupsToDelete);
        for (File backupFile : backupsToDelete) {
            try {
                LOGGER.debug("[%s] Deleting old backup directory %s".formatted(getProfileDescriptiveName(), backupFile.getAbsolutePath()));
                FileUtils.deleteDirectory(backupFile);
            } catch (IOException e) {
                LOGGER.warn("[%s] Old backup directory %s could not be deleted, caught %s".formatted(getProfileDescriptiveName(), backupFile.getAbsolutePath(), e.getMessage()));
            }
        }
    }

    public List<File> findGraphBackupsSortedByName() {
        File vehicleDir = getProfileGraphsDirectory();
        FilenameFilter filter = new RegexFileFilter("^%s_\\d{4}-\\d{2}-\\d{2}_\\d{6}$".formatted(getActiveGraphDirName()));
        File[] obj = vehicleDir.listFiles(filter);
        if (obj == null)
            return Collections.emptyList();

        return Arrays.asList(Objects.requireNonNull(obj)).stream().sorted(Comparator.comparing(File::getName)).toList();
    }

    public GraphBuildInfo getActiveGraphBuildInfo() throws ORSGraphFileManagerException {
        LOGGER.trace("[%s] Checking active graph info...".formatted(getProfileDescriptiveName()));
        File activeGraphDirectory = getActiveGraphDirectory();

        if (!hasActiveGraph()) {
            LOGGER.trace("[%s] No active graph directory found.".formatted(getProfileDescriptiveName()));
            return new GraphBuildInfo().setLocalDirectory(activeGraphDirectory);
        }

        return getGraphBuildInfo(getActiveGraphBuildInfoFile());
    }

    public GraphBuildInfo getDownloadedExtractedGraphBuildInfo() throws ORSGraphFileManagerException {
        LOGGER.trace("[%s] Checking downloaded graph info...".formatted(getProfileDescriptiveName()));
        File downloadedExtractedGraphDirectory = getDownloadedExtractedGraphDirectory();

        if (!hasDownloadedExtractedGraph()) {
            LOGGER.trace("[%s] No downloaded graph directory found.".formatted(getProfileDescriptiveName()));
            return new GraphBuildInfo().setLocalDirectory(downloadedExtractedGraphDirectory);
        }

        return getGraphBuildInfo(getDownloadedExtractedGraphBuildInfoFile());
    }

    private GraphBuildInfo getGraphBuildInfo(File graphBuildInfoFile) throws ORSGraphFileManagerException {
        File graphDirectory = graphBuildInfoFile.getParentFile();
        if (!graphBuildInfoFile.exists() || !graphBuildInfoFile.isFile()) {
            LOGGER.trace("[%s] No graph info file %s found in %s".formatted(getProfileDescriptiveName(), graphBuildInfoFile.getName(), graphBuildInfoFile.getParentFile().getName()));
            return new GraphBuildInfo().setLocalDirectory(graphDirectory);
        }

        PersistedGraphBuildInfo graphBuildInfo = readOrsGraphBuildInfo(graphBuildInfoFile);
        LOGGER.trace("[%s] Found local graph info with importDate=%s".formatted(getProfileDescriptiveName(), graphBuildInfo.getGraphBuildDate()));
        return new GraphBuildInfo().setLocalDirectory(graphDirectory).setPersistedGraphBuildInfo(graphBuildInfo);
    }

    static ObjectMapper getYamlMapper() {
        YAMLFactory yf = new YAMLFactory()
                .disable(WRITE_DOC_START_MARKER)
                .disable(SPLIT_LINES)
                .disable(USE_NATIVE_TYPE_ID)
                .enable(INDENT_ARRAYS_WITH_INDICATOR)
                .enable(MINIMIZE_QUOTES);
        ObjectMapper mapper = new ObjectMapper(yf);
        mapper.configure(WRITE_BIGDECIMAL_AS_PLAIN, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, false);
        return mapper;
    }

    public PersistedGraphBuildInfo readOrsGraphBuildInfo(File graphBuildInfoFile) throws ORSGraphFileManagerException {
        try {
            return getYamlMapper()
                    .readValue(graphBuildInfoFile, PersistedGraphBuildInfo.class);
        } catch (IOException e) {
            LOGGER.error("Error reading ORS graph info: %s".formatted(e.getMessage()));
            throw new ORSGraphFileManagerException("Error reading ORS graph info: ", e);
        }
    }

    public static void writeOrsGraphBuildInfo(PersistedGraphBuildInfo persistedGraphBuildInfo, File outputFile) {
        try {
            getYamlMapper()
                    .writeValue(outputFile, persistedGraphBuildInfo);
        } catch (IOException e) {
            LOGGER.error("Could not write file %s".formatted(outputFile.getAbsolutePath()));
            throw new ORSGraphFileManagerException("Could not write file: ", e);
        }
    }

    public PersistedGraphBuildInfo getDownloadedGraphBuildInfo() throws ORSGraphFileManagerException {
        LOGGER.trace("[%s] Checking graph info of previous check...".formatted(getProfileDescriptiveName()));
        File downloadedGraphBuildInfoFile = getDownloadedGraphBuildInfoFile();
        if (downloadedGraphBuildInfoFile.exists()) {
            return readOrsGraphBuildInfo(downloadedGraphBuildInfoFile);
        }
        return null;
    }

    public void activateExtractedDownloadedGraph() {
        if (hasDownloadedExtractedGraph()) {
            LOGGER.info("[%s] Activating extracted downloaded graph.".formatted(getProfileDescriptiveName()));
            File downloadedExtractedGraphDirectory = getDownloadedExtractedGraphDirectory();
            if (downloadedExtractedGraphDirectory.renameTo(getActiveGraphDirectory())) {
                LOGGER.debug("[%s] Successfully activated extracted downloaded graph.".formatted(getProfileDescriptiveName()));
            } else {
                LOGGER.error("[%s] Failed to activate extracted downloaded graph.".formatted(getProfileDescriptiveName()));
            }
        }
    }

    public void extractDownloadedGraph() {
        File graphDownloadFile = getDownloadedCompressedGraphFile();
        if (!graphDownloadFile.exists()) {
            LOGGER.debug("[%s] No downloaded graph to extract.".formatted(getProfileDescriptiveName()));
            return;
        }

        String graphDownloadFileAbsPath = graphDownloadFile.getAbsolutePath();
        File targetDirectory = getDownloadedExtractedGraphDirectory();
        String targetDirectoryAbsPath = targetDirectory.getAbsolutePath();
        File extractionDirectory = asIncompleteDirectory(targetDirectory);
        String extractionDirectoryAbsPath = extractionDirectory.getAbsolutePath();

        if (isExistingDirectory(extractionDirectory)) {
            LOGGER.debug("[%s] Extraction already started.".formatted(getProfileDescriptiveName()));
            return;
        }

        try {
            LOGGER.info("[%s] Extracting downloaded graph file to %s".formatted(getProfileDescriptiveName(), extractionDirectoryAbsPath));
            long start = System.currentTimeMillis();
            (new Unzipper()).unzip(graphDownloadFileAbsPath, extractionDirectoryAbsPath, true);
            long end = System.currentTimeMillis();

            LOGGER.debug("[%s] Extraction of downloaded graph file finished after %d ms, deleting downloaded graph file %s".formatted(
                    getProfileDescriptiveName(),
                    end - start,
                    graphDownloadFileAbsPath));
            deleteFileWithLogging(graphDownloadFile,
                    "[%s] Deleted downloaded graph file %s".formatted(getProfileDescriptiveName(), graphDownloadFileAbsPath),
                    "Error deleting downloaded graph file: %s");

            LOGGER.debug("[%s] Renaming extraction directory to %s".formatted(
                    getProfileDescriptiveName(),
                    targetDirectoryAbsPath));
            if (targetDirectory.exists()) {
                FileUtils.deleteDirectory(targetDirectory);
            }
            if (!extractionDirectory.renameTo(targetDirectory)) {
                LOGGER.error("[%s] Could not rename extraction directory to %s".formatted(getProfileDescriptiveName(), targetDirectoryAbsPath));
            }

        } catch (IOException ioException) {
            LOGGER.error("[%s] Error during extraction of %s to %s -> %s".formatted(
                    getProfileDescriptiveName(),
                    graphDownloadFileAbsPath,
                    extractionDirectoryAbsPath,
                    targetDirectoryAbsPath));
            throw new ORSGraphFileManagerException("Error during extraction of downloaded graph file: ", ioException);
        }
        LOGGER.info("[%s] Downloaded graph was extracted and will be activated at next graph activation check or application start.".formatted(getProfileDescriptiveName()));
    }

    public void writeOrsGraphBuildInfoFileIfNotExists(ORSGraphHopper gh) {
        if (gh.getEngineProperties().getProfiles() == null)
            return;
        if (gh.getEngineProperties().getProfiles().isEmpty())
            return;

        File activeGraphDirectory = getActiveGraphDirectory();
        File activeGraphBuildInfoFile = getActiveGraphBuildInfoFile();
        if (!activeGraphDirectory.exists() || !activeGraphDirectory.isDirectory() || !activeGraphDirectory.canWrite()) {
            LOGGER.debug("Graph directory %s not existing or not writeable.".formatted(activeGraphBuildInfoFile.getName()));
            return;
        }
        if (activeGraphBuildInfoFile.exists()) {
            LOGGER.debug("GraphBuildInfo-File %s already existing.".formatted(activeGraphBuildInfoFile.getName()));
            return;
        }
        ProfileProperties profileProperties = gh.getProfileProperties();
        if (profileProperties == null) {
            LOGGER.debug("Configuration for profile %s does not exist, could not write GraphBuildInfo-File.".formatted(this.graphManagementRuntimeProperties.getLocalProfileName()));
            return;
        }
        PersistedGraphBuildInfo persistedGraphBuildInfo = PersistedGraphBuildInfo.withOsmDate(getDateFromGhProperty(gh, "datareader.data.date"));
        persistedGraphBuildInfo.setGraphBuildDate(getDateFromGhProperty(gh, "datareader.import.date"));
        persistedGraphBuildInfo.setProfileProperties(profileProperties);

        ORSGraphFileManager.writeOrsGraphBuildInfo(persistedGraphBuildInfo, activeGraphBuildInfoFile);
    }

    Date getDateFromGhProperty(GraphHopper gh, String ghProperty) {
        try {
            String importDateString = gh.getGraphHopperStorage().getProperties().get(ghProperty);
            if (StringUtils.isBlank(importDateString)) {
                return null;
            }
            DateFormat f = Helper.createFormatter();
            return f.parse(importDateString);
        } catch (ParseException e) {
            LOGGER.error("Could not parse date from GraphHopper property %s".formatted(ghProperty));
            return null;
        }
    }


    @Override
    public String getProfileDescriptiveName() {
        return orsGraphFolderStrategy.getProfileDescriptiveName();
    }

    @Override
    public String getGraphBuildInfoFileNameInRepository() {
        return orsGraphFolderStrategy.getGraphBuildInfoFileNameInRepository();
    }

    @Override
    public String getGraphsRootDirName() {
        return orsGraphFolderStrategy.getGraphsRootDirName();
    }

    @Override
    public String getGraphsRootDirAbsPath() {
        return orsGraphFolderStrategy.getGraphsRootDirAbsPath();
    }

    @Override
    public String getProfileGraphsDirName() {
        return orsGraphFolderStrategy.getProfileGraphsDirName();
    }

    @Override
    public String getProfileGraphsDirAbsPath() {
        return orsGraphFolderStrategy.getProfileGraphsDirAbsPath();
    }

    @Override
    public String getActiveGraphDirName() {
        return orsGraphFolderStrategy.getActiveGraphDirName();
    }

    @Override
    public String getActiveGraphDirAbsPath() {
        return orsGraphFolderStrategy.getActiveGraphDirAbsPath();
    }

    @Override
    public String getActiveGraphBuildInfoFileName() {
        return orsGraphFolderStrategy.getActiveGraphBuildInfoFileName();
    }

    @Override
    public String getDownloadedGraphBuildInfoFileName() {
        return orsGraphFolderStrategy.getDownloadedGraphBuildInfoFileName();
    }

    @Override
    public String getDownloadedGraphBuildInfoFileAbsPath() {
        return orsGraphFolderStrategy.getDownloadedGraphBuildInfoFileAbsPath();
    }

    @Override
    public String getDownloadedCompressedGraphFileName() {
        return orsGraphFolderStrategy.getDownloadedCompressedGraphFileName();
    }

    @Override
    public String getDownloadedCompressedGraphFileAbsPath() {
        return orsGraphFolderStrategy.getDownloadedCompressedGraphFileAbsPath();
    }

    @Override
    public String getDownloadedExtractedGraphDirName() {
        return orsGraphFolderStrategy.getDownloadedExtractedGraphDirName();
    }

    @Override
    public String getDownloadedExtractedGraphDirAbsPath() {
        return orsGraphFolderStrategy.getDownloadedExtractedGraphDirAbsPath();
    }

}
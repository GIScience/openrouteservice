package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.heigit.ors.exceptions.ORSGraphFileManagerException;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphBuildInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.PersistedGraphBuildInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class S3GraphRepoClient extends AbstractGraphRepoClient implements ORSGraphRepoClient {

    private static final Logger LOGGER = Logger.getLogger(S3GraphRepoClient.class.getName());
    private final GraphManagementRuntimeProperties managementProps;
    private final ORSGraphFileManager orsGraphFileManager;
    private final ORSGraphRepoStrategy orsGraphRepoStrategy;
    private S3Client s3Client;

    public S3GraphRepoClient(GraphManagementRuntimeProperties managementProps, ORSGraphRepoStrategy orsGraphRepoStrategy, ORSGraphFileManager orsGraphFileManager) {
        this.managementProps = managementProps;
        this.orsGraphRepoStrategy = orsGraphRepoStrategy;
        this.orsGraphFileManager = orsGraphFileManager;
    }

    @Override
    ORSGraphFileManager getOrsGraphFileManager() {
        return orsGraphFileManager;
    }

    @Override
    ORSGraphRepoStrategy getOrsGraphRepoStrategy() {
        return orsGraphRepoStrategy;
    }

    @Override
    GraphManagementRuntimeProperties getGraphManagementRuntimeProperties() {
        return managementProps;
    }

    @Override
    Logger getLogger() {
        return LOGGER;
    }

    @Override
    public boolean hasValidRepoConfig() {
        return isNotBlank(this.managementProps.getRepoName()) &&
                isNotBlank(this.managementProps.getRepoCoverage()) &&
                isNotBlank(this.managementProps.getGraphVersion()) &&
                isNotBlank(this.managementProps.getRepoUser()) &&
                isNotBlank(this.managementProps.getRepoPass()) &&
                isNotBlank(this.managementProps.getDerivedRepoBaseUrl().toString());
    }

    @Override
    protected void downloadCompressedGraphFromRepository() {
        Path latestCompressedGraphInRepoPath = Path.of(
                managementProps.getRepoProfileGroup(),
                managementProps.getRepoCoverage(),
                managementProps.getGraphVersion(),
                orsGraphRepoStrategy.getRepoCompressedGraphFileName());
        downloadFile(latestCompressedGraphInRepoPath, orsGraphFileManager.getDownloadedCompressedGraphFile());
    }

    private void deleteFileWithLogging(File file) {
        try {
            if (Files.deleteIfExists(file.toPath()))
                LOGGER.debug("[%s] Deleted old downloaded graphBuildInfo file: %s".formatted(getProfileDescriptiveName(), file.getAbsolutePath()));
        } catch (IOException _) {
            LOGGER.error("[%s] Could not delete old downloaded graphBuildInfo file: %s".formatted(getProfileDescriptiveName(), file.getAbsolutePath()));
        }
    }

    public static String concatenateToUrlPath(String... values) {
        return Stream.of(values)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(s -> s.replaceAll("^/", ""))
                .map(s -> s.replaceAll("/$", ""))
                .filter(s -> !s.equals("."))
                .collect(Collectors.joining("/"));
    }

    GraphBuildInfo downloadGraphBuildInfoFromRepository() throws ORSGraphFileManagerException {
        GraphBuildInfo graphBuildInfoInRepo = new GraphBuildInfo();
        LOGGER.debug("[%s] Checking latest graphBuildInfo in remote repository...".formatted(getProfileDescriptiveName()));

        Path latestGraphBuildInfoInRepoPath = Path.of(
                getRepoProfileGroup(),
                getRepoCoverage(),
                getGraphVersion(),
                getRepoGraphBuildInfoFileName());

        File downloadedGraphBuildInfoFile = getOrsGraphFileManager().getDownloadedGraphBuildInfoFile();
        deleteFileWithLogging(downloadedGraphBuildInfoFile);
        downloadFile(latestGraphBuildInfoInRepoPath, downloadedGraphBuildInfoFile);

        if (!downloadedGraphBuildInfoFile.exists()) {
            LOGGER.info("[%s] No graphBuildInfo found in remote repository.".formatted(getProfileDescriptiveName()));
            return graphBuildInfoInRepo;
        }

        graphBuildInfoInRepo.withRemoteUriString(concatenateToUrlPath(getRepoBaseUri(), getRepoName(), latestGraphBuildInfoInRepoPath.toString()));
        PersistedGraphBuildInfo persistedGraphBuildInfo = getPersistedGraphBuildInfo(downloadedGraphBuildInfoFile);
        graphBuildInfoInRepo.setPersistedGraphBuildInfo(persistedGraphBuildInfo);
        return graphBuildInfoInRepo;
    }

    public void downloadFile(Path repoPath, File outputFile) {
        if (repoPath == null || outputFile == null) {
            LOGGER.warn("[%s] Invalid download or local path: %s or %s".formatted(getProfileDescriptiveName(), repoPath, outputFile));
            return;
        }
        File tempDownloadFile = getIncompleteFile(outputFile);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("[%s] Downloading %s to local file %s...".formatted(getProfileDescriptiveName(), repoPath, tempDownloadFile.getAbsolutePath()));
        } else {
            LOGGER.info("[%s] Downloading %s...".formatted(getProfileDescriptiveName(), repoPath));
        }
        try {
            if (s3Client == null) {
                s3Client = S3Client.builder()
                        .endpointOverride(getDerivedRepoBaseUrl().toURI())
                        .region(Region.US_EAST_1) //RustFS default region
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(getRepoUser(), getRepoPass())
                        ))
                        .forcePathStyle(true) // RustFS uses path-style URLs by default; virtual-host style requires RUSTFS_SERVER_DOMAINS
                        .build();
            }
            s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(getRepoName())
                            .key(repoPath.toString()).build(),
                    Paths.get(tempDownloadFile.toString())
            );
            if (tempDownloadFile.renameTo(outputFile)) {
                LOGGER.debug("[%s] Renamed temp file to %s".formatted(getProfileDescriptiveName(), outputFile.getAbsolutePath()));
            } else {
                LOGGER.error("[%s] Could not rename temp file to %s".formatted(getProfileDescriptiveName(), outputFile.getAbsolutePath()));
            }
        } catch (URISyntaxException e) {
            LOGGER.warn("[%s] Caught %s when trying to use Url %s".formatted(getProfileDescriptiveName(), e, getDerivedRepoBaseUrl()));
            throw new IllegalArgumentException(e);
        } finally {
            deleteFileWithLogging(tempDownloadFile, "[%s] Deleted temp download file: %s", "[%s] Could not delete temp download file: %s");
        }
    }
}

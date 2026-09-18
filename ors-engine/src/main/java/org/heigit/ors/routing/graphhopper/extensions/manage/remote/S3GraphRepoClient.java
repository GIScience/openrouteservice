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

import static com.google.common.base.Strings.isNullOrEmpty;

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

    String getProfileDescriptiveName() {
        return orsGraphFileManager.getProfileDescriptiveName();
    }

    @Override
    public void downloadGraphIfNecessary() {
        if (isNullOrEmpty(String.valueOf(managementProps.getDerivedRepoBaseUrl())) || isNullOrEmpty(managementProps.getRepoName()) || isNullOrEmpty(managementProps.getRepoCoverage()) || isNullOrEmpty(managementProps.getGraphVersion())) {
            LOGGER.debug("[%s] ORSGraphManager is not configured - skipping check".formatted(getProfileDescriptiveName()));
            return;
        }
        if (orsGraphFileManager.isBusy()) {
            LOGGER.debug("[%s] ORSGraphManager is busy - skipping check".formatted(getProfileDescriptiveName()));
            return;
        }

        LOGGER.debug("[%s] Checking for possible graph update from remote repository...".formatted(getProfileDescriptiveName()));
        try {
            PersistedGraphBuildInfo previouslyDownloadedGraphBuildInfo = orsGraphFileManager.getDownloadedGraphBuildInfo();
            File downloadedCompressedGraphFile = orsGraphFileManager.getDownloadedCompressedGraphFile();
            GraphBuildInfo activeGraphBuildInfo = orsGraphFileManager.getActiveGraphBuildInfo();
            GraphBuildInfo downloadedExtractedGraphBuildInfo = orsGraphFileManager.getDownloadedExtractedGraphBuildInfo();
            GraphBuildInfo newlyDownloadedGraphBuildInfo = downloadLatestGraphBuildInfoFromRepository();
            LOGGER.trace(("[%s] Comparing dates, downloading if first is after the others:%n" +
                    "                         repo=%s%n" +
                    "              activeGraphBuildInfo=%s%n" +
                    " downloadedExtractedGraphBuildInfo=%s%n" +
                    "previouslyDownloadedGraphBuildInfo=%s").formatted(getProfileDescriptiveName(),
                    getDateOrEpocStart(newlyDownloadedGraphBuildInfo),
                    getDateOrEpocStart(activeGraphBuildInfo),
                    getDateOrEpocStart(downloadedExtractedGraphBuildInfo),
                    getDateOrEpocStart(downloadedCompressedGraphFile, previouslyDownloadedGraphBuildInfo)));

            if (!shouldDownloadGraph(
                    getDateOrEpocStart(newlyDownloadedGraphBuildInfo),
                    getDateOrEpocStart(activeGraphBuildInfo),
                    getDateOrEpocStart(downloadedExtractedGraphBuildInfo),
                    getDateOrEpocStart(downloadedCompressedGraphFile, previouslyDownloadedGraphBuildInfo))) {
                LOGGER.info("[%s] No newer graph found in repository.".formatted(getProfileDescriptiveName()));
                return;
            }

            Path latestCompressedGraphInRepoPath = Path.of(managementProps.getRepoProfileGroup(), managementProps.getRepoCoverage(), managementProps.getGraphVersion(), orsGraphRepoStrategy.getRepoCompressedGraphFileName());
            long start = System.currentTimeMillis();
            downloadFile(latestCompressedGraphInRepoPath, downloadedCompressedGraphFile);

            long end = System.currentTimeMillis();
            if (downloadedCompressedGraphFile.exists()) {
                LOGGER.info("[%s] Download of compressed graph file finished after %d ms".formatted(getProfileDescriptiveName(), end - start));
            } else {
                LOGGER.error("[%s] Invalid download path for compressed graph file: %s".formatted(getProfileDescriptiveName(), latestCompressedGraphInRepoPath));
            }
        } catch (Exception exception) {
            LOGGER.error("[%s] Caught an exception during graph download check or graph download:".formatted(getProfileDescriptiveName()), exception);
        }
    }

    private void deleteFileWithLogging(File file) {
        try {
            if (Files.deleteIfExists(file.toPath()))
                LOGGER.debug("[%s] Deleted old downloaded graphBuildInfo file: %s".formatted(getProfileDescriptiveName(), file.getAbsolutePath()));
        } catch (IOException ioException) {
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

    GraphBuildInfo downloadLatestGraphBuildInfoFromRepository() throws ORSGraphFileManagerException {
        GraphBuildInfo graphBuildInfoInRepo = new GraphBuildInfo();
        LOGGER.debug("[%s] Checking latest graphBuildInfo in remote repository...".formatted(getProfileDescriptiveName()));

        Path latestGraphBuildInfoInRepoPath = Path.of(managementProps.getRepoProfileGroup(), managementProps.getRepoCoverage(), managementProps.getGraphVersion(), orsGraphRepoStrategy.getRepoGraphBuildInfoFileName());

        File downloadedGraphBuildInfoFile = orsGraphFileManager.getDownloadedGraphBuildInfoFile();
        deleteFileWithLogging(downloadedGraphBuildInfoFile);
        downloadFile(latestGraphBuildInfoInRepoPath, downloadedGraphBuildInfoFile);

        if (!downloadedGraphBuildInfoFile.exists()) {
            LOGGER.info("[%s] No graphBuildInfo found in remote repository.".formatted(getProfileDescriptiveName()));
            return graphBuildInfoInRepo;
        }

        graphBuildInfoInRepo.withRemoteUriString(concatenateToUrlPath(managementProps.getRepoBaseUri(), managementProps.getRepoName(), latestGraphBuildInfoInRepoPath.toString()));
        PersistedGraphBuildInfo persistedGraphBuildInfo = orsGraphFileManager.readOrsGraphBuildInfo(downloadedGraphBuildInfoFile);
        graphBuildInfoInRepo.setPersistedGraphBuildInfo(persistedGraphBuildInfo);
        return graphBuildInfoInRepo;
    }

    public void downloadFile(Path repoPath, File localPath) {
        if (repoPath == null || localPath == null) {
            LOGGER.warn("[%s] Invalid download or local path: %s or %s".formatted(getProfileDescriptiveName(), repoPath, localPath));
            return;
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("[%s] Downloading %s to local file %s...".formatted(getProfileDescriptiveName(), repoPath, localPath.getAbsolutePath()));
        } else {
            LOGGER.info("[%s] Downloading %s...".formatted(getProfileDescriptiveName(), repoPath));
        }
        try {
            if (s3Client == null) {
                s3Client = S3Client.builder()
                        .endpointOverride(managementProps.getDerivedRepoBaseUrl().toURI())
                        .region(Region.US_EAST_1) //RustFS default region
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(managementProps.getRepoUser(), managementProps.getRepoPass())
                        ))
                        .forcePathStyle(true) // RustFS uses path-style URLs by default; virtual-host style requires RUSTFS_SERVER_DOMAINS
                        .build();
            }
            s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(managementProps.getRepoName())
                            .key(repoPath.toString()).build(),
                    Paths.get(localPath.toString())
            );
        } catch (URISyntaxException e) {
            LOGGER.warn("[%s] Caught %s when trying to use Url %s".formatted(getProfileDescriptiveName(), e, managementProps.getDerivedRepoBaseUrl()));
            throw new IllegalArgumentException(e);
        }
    }
}

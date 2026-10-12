package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

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
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class S3GraphRepoClient extends AbstractGraphRepoClient {

    private S3Client s3Client;

    public S3GraphRepoClient(GraphManagementRuntimeProperties managementProps, ORSGraphRepoStrategy orsGraphRepoStrategy, ORSGraphFileManager orsGraphFileManager) {
        super(managementProps, orsGraphRepoStrategy, orsGraphFileManager);
    }

    @Override
    public boolean hasValidRepoConfig() {
        return isNotBlank(getManagementProps().getRepoName()) &&
                isNotBlank(getManagementProps().getRepoProfileGroup()) &&
                isNotBlank(getManagementProps().getRepoCoverage()) &&
                isNotBlank(getManagementProps().getGraphVersion()) &&
                isNotBlank(getManagementProps().getRepoUser()) &&
                isNotBlank(getManagementProps().getRepoPass()) &&
                isNotBlank(getManagementProps().getDerivedRepoBaseUrl().toString());
    }

    @Override
    GraphBuildInfo downloadGraphBuildInfoFromRepository() throws ORSGraphFileManagerException {
        GraphBuildInfo graphBuildInfoInRepo = new GraphBuildInfo();
        //Log message is asserted in GraphRepoTest/TestContainersHelper - change with care!
        getLogger().debug("[%s] Checking latest graphBuildInfo in remote repository..."
                .formatted(getGraphFileManager().getProfileDescriptiveName()));

        Path latestGraphBuildInfoInRepoPath = Path.of(
                getManagementProps().getRepoProfileGroup(),
                getManagementProps().getRepoCoverage(),
                getManagementProps().getGraphVersion(),
                getGraphRepoStrategy().getRepoGraphBuildInfoFileName());

        File downloadedGraphBuildInfoFile = getGraphFileManager().getDownloadedGraphBuildInfoFile();
        deleteFileWithLogging(downloadedGraphBuildInfoFile,
                "[%s] Deleted old downloaded graphBuildInfo file: %s",
                "[%s] Could not delete old downloaded graphBuildInfo file: %s"
        );

        downloadFile(latestGraphBuildInfoInRepoPath, downloadedGraphBuildInfoFile);

        if (!downloadedGraphBuildInfoFile.exists()) {
            //Log message is asserted in GraphRepoTest/TestContainersHelper - change with care!
            getLogger().info("[%s] No graphBuildInfo found in remote repository."
                    .formatted(getGraphFileManager().getProfileDescriptiveName()));
            return graphBuildInfoInRepo;
        }

        graphBuildInfoInRepo.withRemoteUriString(concatenateToUrlPath(
                getManagementProps().getRepoBaseUri(),
                getManagementProps().getRepoName(),
                latestGraphBuildInfoInRepoPath.toString()));
        PersistedGraphBuildInfo persistedGraphBuildInfo = getGraphFileManager()
                .readOrsGraphBuildInfo(downloadedGraphBuildInfoFile);
        graphBuildInfoInRepo.setPersistedGraphBuildInfo(persistedGraphBuildInfo);
        return graphBuildInfoInRepo;
    }

    @Override
    protected void downloadCompressedGraphFromRepository() {
        Path latestCompressedGraphInRepoPath = Path.of(
                getManagementProps().getRepoProfileGroup(),
                getManagementProps().getRepoCoverage(),
                getManagementProps().getGraphVersion(),
                getGraphRepoStrategy().getRepoCompressedGraphFileName());
        downloadFile(latestCompressedGraphInRepoPath, getGraphFileManager().getDownloadedCompressedGraphFile());
    }

    public void downloadFile(Path repoPath, File outputFile) {
        if (repoPath == null || outputFile == null) {
            getLogger().warn("[%s] Invalid download or local path: %s or %s"
                    .formatted(getGraphFileManager().getProfileDescriptiveName(), repoPath, outputFile));
            return;
        }
        File tempDownloadFile = getGraphFileManager().asIncompleteFile(outputFile);
        if (getLogger().isTraceEnabled()) {
            //Log message is asserted in GraphRepoTest/TestContainersHelper - change with care!
            getLogger().trace("[%s] Downloading %s to local file %s..."
                    .formatted(
                            getGraphFileManager().getProfileDescriptiveName(),
                            repoPath,
                            tempDownloadFile.getAbsolutePath()));
        } else {
            //Log message is asserted in GraphRepoTest/TestContainersHelper - change with care!
            getLogger().info("[%s] Downloading %s...".formatted(
                    getGraphFileManager().getProfileDescriptiveName(),
                    repoPath));
        }
        try {
            if (s3Client == null) {
                s3Client = S3Client.builder()
                        .endpointOverride(getManagementProps().getDerivedRepoBaseUrl().toURI())
                        .region(Region.US_EAST_1) //RustFS default region
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        getManagementProps().getRepoUser(),
                                        getManagementProps().getRepoPass())
                        ))
                        .forcePathStyle(true) // RustFS uses path-style URLs by default; virtual-host style requires RUSTFS_SERVER_DOMAINS
                        .build();
            }
            s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(getManagementProps().getRepoName())
                            .key(repoPath.toString()).build(),
                    Paths.get(tempDownloadFile.toString())
            );
            if (tempDownloadFile.renameTo(outputFile)) {
                getLogger().debug("[%s] Renamed temp file to %s".formatted(
                        getGraphFileManager().getProfileDescriptiveName(),
                        outputFile.getAbsolutePath()));
            } else {
                getLogger().error("[%s] Could not rename temp file to %s".formatted(
                        getGraphFileManager().getProfileDescriptiveName(),
                        outputFile.getAbsolutePath()));
            }
        } catch (URISyntaxException e) {
            getLogger().warn("[%s] Caught %s when trying to use Url %s".formatted(
                    getGraphFileManager().getProfileDescriptiveName(),
                    e,
                    getManagementProps().getDerivedRepoBaseUrl()));
            throw new IllegalArgumentException(e);
        } finally {
            deleteFileWithLogging(tempDownloadFile, "[%s] Deleted temp download file: %s", "[%s] Could not delete temp download file: %s");
        }
    }
}

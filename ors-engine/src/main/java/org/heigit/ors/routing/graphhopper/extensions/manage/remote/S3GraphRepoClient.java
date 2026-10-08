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
        getLogger().debug("[%s] Checking latest graphBuildInfo in remote repository...".formatted(getProfileDescriptiveName()));

        Path latestGraphBuildInfoInRepoPath = Path.of(
                getRepoProfileGroup(),
                getRepoCoverage(),
                getGraphVersion(),
                getRepoGraphBuildInfoFileName());

        File downloadedGraphBuildInfoFile = getGraphFileManager().getDownloadedGraphBuildInfoFile();
        deleteFileWithLogging(downloadedGraphBuildInfoFile,
                "[%s] Deleted old downloaded graphBuildInfo file: %s",
                "[%s] Could not delete old downloaded graphBuildInfo file: %s"
        );

        downloadFile(latestGraphBuildInfoInRepoPath, downloadedGraphBuildInfoFile);

        if (!downloadedGraphBuildInfoFile.exists()) {
            //Log message is asserted in GraphRepoTest/TestContainersHelper - change with care!
            getLogger().info("[%s] No graphBuildInfo found in remote repository.".formatted(getProfileDescriptiveName()));
            return graphBuildInfoInRepo;
        }

        graphBuildInfoInRepo.withRemoteUriString(concatenateToUrlPath(getRepoBaseUri(), getRepoName(), latestGraphBuildInfoInRepoPath.toString()));
        PersistedGraphBuildInfo persistedGraphBuildInfo = getPersistedGraphBuildInfo(downloadedGraphBuildInfoFile);
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
            getLogger().warn("[%s] Invalid download or local path: %s or %s".formatted(getProfileDescriptiveName(), repoPath, outputFile));
            return;
        }
        File tempDownloadFile = getIncompleteFile(outputFile);
        if (getLogger().isTraceEnabled()) {
            //Log message is asserted in GraphRepoTest/TestContainersHelper - change with care!
            getLogger().trace("[%s] Downloading %s to local file %s...".formatted(getProfileDescriptiveName(), repoPath, tempDownloadFile.getAbsolutePath()));
        } else {
            //Log message is asserted in GraphRepoTest/TestContainersHelper - change with care!
            getLogger().info("[%s] Downloading %s...".formatted(getProfileDescriptiveName(), repoPath));
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
                getLogger().debug("[%s] Renamed temp file to %s".formatted(getProfileDescriptiveName(), outputFile.getAbsolutePath()));
            } else {
                getLogger().error("[%s] Could not rename temp file to %s".formatted(getProfileDescriptiveName(), outputFile.getAbsolutePath()));
            }
        } catch (URISyntaxException e) {
            getLogger().warn("[%s] Caught %s when trying to use Url %s".formatted(getProfileDescriptiveName(), e, getDerivedRepoBaseUrl()));
            throw new IllegalArgumentException(e);
        } finally {
            deleteFileWithLogging(tempDownloadFile, "[%s] Deleted temp download file: %s", "[%s] Could not delete temp download file: %s");
        }
    }
}

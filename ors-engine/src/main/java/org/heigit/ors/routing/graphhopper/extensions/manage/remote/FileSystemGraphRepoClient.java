package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import org.heigit.ors.exceptions.ORSGraphFileManagerException;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphBuildInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.PersistedGraphBuildInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class FileSystemGraphRepoClient extends AbstractGraphRepoClient {

    public FileSystemGraphRepoClient(GraphManagementRuntimeProperties graphManagementRuntimeProperties,
                                     ORSGraphRepoStrategy orsGraphRepoStrategy,
                                     ORSGraphFileManager orsGraphFileManager) {
        super(graphManagementRuntimeProperties, orsGraphRepoStrategy, orsGraphFileManager);
    }

    @Override
    public boolean hasValidRepoConfig() {
        return isNotBlank(getManagementProps().getRepoName()) &&
                isNotBlank(getManagementProps().getRepoProfileGroup()) &&
                isNotBlank(getManagementProps().getRepoCoverage()) &&
                isNotBlank(getManagementProps().getGraphVersion()) &&
                isNotBlank(getManagementProps().getDerivedRepoPath().toAbsolutePath().toString());
    }

    @Override
    protected void downloadCompressedGraphFromRepository() {
        Path latestCompressedGraphInRepoPath = Path.of(
                getRepoPath(),
                getRepoName(),
                getRepoProfileGroup(),
                getRepoCoverage(),
                getGraphVersion(),
                getRepoCompressedGraphFileName());
        downloadFile(latestCompressedGraphInRepoPath, getGraphFileManager().getDownloadedCompressedGraphFile());
    }

    @Override
    GraphBuildInfo downloadGraphBuildInfoFromRepository() throws ORSGraphFileManagerException {
        GraphBuildInfo latestGraphBuildInfoInRepo = new GraphBuildInfo();
        //Log message is asserted in GraphRepoTest/TestContainersHelper - change with care!
        getLogger().debug("[%s] Checking latest graphBuildInfo in remote repository...".formatted(getProfileDescriptiveName()));

        Path latestGraphBuildInfoInRepoPath = Path.of(
                getRepoPath(),
                getRepoName(),
                getRepoProfileGroup(),
                getRepoCoverage(),
                getGraphVersion(),
                getRepoGraphBuildInfoFileName());

        if (!latestGraphBuildInfoInRepoPath.toFile().exists()) {
            //Log message is asserted in GraphRepoTest/TestContainersHelper - change with care!
            getLogger().info("[%s] No graphBuildInfo found in remote repository: %s".formatted(getProfileDescriptiveName(), latestGraphBuildInfoInRepoPath.toFile().getAbsolutePath()));
            return latestGraphBuildInfoInRepo;
        }

        File downloadedGraphBuildInfoFile = getGraphFileManager().getDownloadedGraphBuildInfoFile();
        downloadFile(latestGraphBuildInfoInRepoPath, downloadedGraphBuildInfoFile);

        if (downloadedGraphBuildInfoFile.exists()) {
            Path latestCompressedGraphInRepoPath = Path.of(getRepoPath(), getRepoName(), getRepoProfileGroup(), getRepoCoverage(), getGraphVersion(), getRepoCompressedGraphFileName());
            URI uri = latestCompressedGraphInRepoPath.toUri();
            latestGraphBuildInfoInRepo.setRemoteUri(uri);

            PersistedGraphBuildInfo persistedGraphBuildInfo = getGraphFileManager().readOrsGraphBuildInfo(downloadedGraphBuildInfoFile);
            latestGraphBuildInfoInRepo.setPersistedGraphBuildInfo(persistedGraphBuildInfo);
        } else {
            getLogger().error("[%s] Invalid download path for graphBuildInfo file: %s".formatted(getProfileDescriptiveName(), latestGraphBuildInfoInRepoPath));
        }

        return latestGraphBuildInfoInRepo;
    }

    public void downloadFile(Path repoPath, File localPath) {
        if (repoPath == null || localPath == null) {
            getLogger().warn("[%s] Invalid download or local path: %s or %s".formatted(getProfileDescriptiveName(), repoPath, localPath));
            return;
        }
        if (getLogger().isTraceEnabled()) {
            //Log message is asserted in GraphRepoTest/TestContainersHelper - change with care!
            getLogger().trace("[%s] Downloading %s to local file %s...".formatted(getProfileDescriptiveName(), repoPath.toFile().getAbsolutePath(), localPath.getAbsolutePath()));
        } else {
            //Log message is asserted in GraphRepoTest/TestContainersHelper - change with care!
            getLogger().info("[%s] Downloading %s...".formatted(getProfileDescriptiveName(), repoPath.toFile().getName()));
        }
        try {
            Files.copy(repoPath, localPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            getLogger().warn("[%s] Caught %s when trying to download %s".formatted(getProfileDescriptiveName(), e, repoPath.toFile().getAbsolutePath()));
            throw new IllegalArgumentException(e);
        }
    }
}

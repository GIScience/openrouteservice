package org.heigit.ors.routing.graphhopper.extensions.manage;

import java.io.File;
import java.net.URL;
import java.util.Objects;

public class GraphInfo {
    public GraphInfo() {
    }

    private URL remoteUrl = null;
    private File localDirectory = null;
    private ORSGraphInfoV1 persistedGraphInfo;

    public URL getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(URL remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public File getLocalDirectory() {
        return localDirectory;
    }

    public void setLocalDirectory(File localDirectory) {
        this.localDirectory = localDirectory;
    }

    public ORSGraphInfoV1 getPersistedGraphInfo() {
        return persistedGraphInfo;
    }

    public void setPersistedGraphInfo(ORSGraphInfoV1 persistedGraphInfo) {
        this.persistedGraphInfo = persistedGraphInfo;
    }

    public boolean exists() {
        return !Objects.isNull(persistedGraphInfo);
    }

    public boolean isRemote() {
        return remoteUrl != null;
    }

    GraphInfo withRemoteUrl(URL url) {
        this.remoteUrl = url;
        return this;
    }

    GraphInfo withLocalDirectory(File directory) {
        this.localDirectory = directory;
        return this;
    }

    GraphInfo withPersistedInfo(ORSGraphInfoV1 persistedGraphInfo) {
        this.persistedGraphInfo = persistedGraphInfo;
        return this;
    }
}

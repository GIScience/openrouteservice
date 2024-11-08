package org.heigit.ors.routing.graphhopper.extensions.manage;

import lombok.Getter;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

@Getter
public class GraphInfo {

    public GraphInfo() {
    }

    private URI remoteUri = null;
    private File localDirectory = null;

    private PersistedGraphInfo persistedGraphInfo;

    public GraphInfo withRemoteUri(URI remoteUri) {
        this.remoteUri = remoteUri;
        return this;
    }

    public boolean exists() {
        return !Objects.isNull(persistedGraphInfo);
    }

    public boolean isRemote() {
        return remoteUri != null;
    }

    public GraphInfo withRemoteUrl(URL url) {
        try {
            this.remoteUri = url.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public GraphInfo withLocalDirectory(File directory) {
        this.localDirectory = directory;
        return this;
    }

    public GraphInfo withPersistedInfo(PersistedGraphInfo persistedGraphInfo) {
        this.persistedGraphInfo = persistedGraphInfo;
        return this;
    }
}

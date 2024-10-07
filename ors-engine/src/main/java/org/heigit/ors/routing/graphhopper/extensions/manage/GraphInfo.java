package org.heigit.ors.routing.graphhopper.extensions.manage;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public class GraphInfo {
    public GraphInfo() {
    }

    private URI remoteUri = null;
    private File localDirectory = null;
    private Integer compressedGraphBytes;
    private String compressedGraphMd5Sum;

    private PersistedGraphInfo persistedGraphInfo;

    public URI getRemoteUri() {
        return remoteUri;
    }

    public void setRemoteUri(URI remoteUri) {
        this.remoteUri = remoteUri;
    }

    public URL getRemoteUrl() {//TODO remove usages and method
        try {
            return remoteUri.toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setRemoteUrl(URL remoteUrl) {//TODO remove usages and method
        try {
            this.remoteUri = remoteUrl.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public File getLocalDirectory() {
        return localDirectory;
    }

    public void setLocalDirectory(File localDirectory) {
        this.localDirectory = localDirectory;
    }

    public PersistedGraphInfo getPersistedGraphInfo() {
        return persistedGraphInfo;
    }

    public void setPersistedGraphInfo(PersistedGraphInfo persistedGraphInfo) {
        this.persistedGraphInfo = persistedGraphInfo;
    }

    public Integer getCompressedGraphBytes() {
        return compressedGraphBytes;
    }

    public void setCompressedGraphBytes(Integer compressedGraphBytes) {
        this.compressedGraphBytes = compressedGraphBytes;
    }

    public String getCompressedGraphMd5Sum() {
        return compressedGraphMd5Sum;
    }

    public void setCompressedGraphMd5Sum(String compressedGraphMd5Sum) {
        this.compressedGraphMd5Sum = compressedGraphMd5Sum;
    }

    public boolean exists() {
        return !Objects.isNull(persistedGraphInfo);
    }

    public boolean isRemote() {
        return remoteUri != null;
    }

    public GraphInfo withRemoteUrl(URL url) {//TODO remove usages and method
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

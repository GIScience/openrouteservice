package org.heigit.ors.routing.graphhopper.extensions.manage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Accessors(chain = true)
@Getter
@Setter
@NoArgsConstructor
public class GraphInfo {

    private URI remoteUri = null;
    private File localDirectory = null;

    private PersistedGraphInfo persistedGraphInfo;

    public GraphInfo withRemoteUrl(URL url) {
        try {
            this.remoteUri = url.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return this;
    }
}

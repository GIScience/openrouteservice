package org.heigit.ors.routing.graphhopper.extensions.manage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.log4j.Logger;
import org.heigit.ors.exceptions.ORSGraphFileManagerExceptionException;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Accessors(chain = true)
public class GraphBuildInfo {

    private URI remoteUri;
    @Getter
    private File localDirectory = null;
    Logger logger = Logger.getLogger(GraphBuildInfo.class.getName());

    @Getter
    private PersistedGraphBuildInfo persistedGraphBuildInfo;

    public GraphBuildInfo withRemoteUrl(URL url) {
        try {
            this.remoteUri = url.toURI();
        } catch (URISyntaxException e) {
            logger.error("Error while parsing remote URL %s with message %s".formatted(url, e.getMessage()));
            throw new ORSGraphFileManagerExceptionException("Error while parsing remote URL %s.".formatted(url), e);
        }
        return this;
    }
}

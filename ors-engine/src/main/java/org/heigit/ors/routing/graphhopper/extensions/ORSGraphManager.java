package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.util.Unzipper;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Objects;

public class ORSGraphManager {

    private static final Logger LOGGER = Logger.getLogger(ORSGraphManager.class.getName());

    private final String hash;
    private final String localPath;
    private final String routeProfileName;
    public ORSGraphManager(String routeProfileName, String hash, String localPath) {
        this.hash = hash;
        this.localPath = localPath;
        this.routeProfileName = routeProfileName;
    }

    public static class GraphInfo {

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

        public boolean exists(){
            return !Objects.isNull(persistedGraphInfo);
        }

        public boolean isRemote(){
            return remoteUrl != null;
        }

        GraphInfo withRemoteUrl(URL url){
            this.remoteUrl = url;
            return this;
        }

        GraphInfo withLocalDirectory(File directory){
            this.localDirectory = directory;
            return this;
        }

        GraphInfo withPersistedInfo(ORSGraphInfoV1 persistedGraphInfo) {
            this.persistedGraphInfo = persistedGraphInfo;
            return this;
        }
    }

    public static class ORSGraphInfoV1 {
        private Instant osmDate;

        public Instant getOsmDate() {
            return osmDate;
        }

        public void setOsmDate(Instant osmDate) {
            this.osmDate = osmDate;
        }
        //TODO define graph properties
    }


    public void downloadGraphIfNecessary() {
        LOGGER.info("Checking for possible graph update for %s/%s from remote repository...".formatted(routeProfileName, hash));
        GraphInfo localGraphInfo = getLocalGraphInfo();
        GraphInfo remoteGraphInfo = findLatestGraphInRepository();

        if (!shouldDownloadGraph(localGraphInfo, remoteGraphInfo)) {
            return;
        }

        File localDirectory = localGraphInfo.getLocalDirectory();
        if (!localDirectory.isDirectory() && localDirectory.exists()) {
            throw new IllegalArgumentException("GraphHopperLocation cannot be an existing file. Has to be either non-existing or a folder.");
        } else {
            File compressed = new File(localPath + ".ghz");
            if (compressed.exists() && !compressed.isDirectory()) {
                try {
                    (new Unzipper()).unzip(compressed.getAbsolutePath(), localPath, true);
                } catch (IOException var10) {
                    throw new RuntimeException("Couldn't extract file " + compressed.getAbsolutePath() + " to " + localPath, var10);
                }
            }
        }
    }

    public GraphInfo findLatestGraphInRepository() {
        LOGGER.debug("Checking latest graph for %s in remote repository...".formatted(routeProfileName));
        // https://repo.heigit.org/ors-graphs-traffic/planet-latest/1.2.3.3/2023-07-02/car/gljsakdgjsdlkfj
        LOGGER.debug("No graph for %s found in remote repository".formatted(routeProfileName));
        return new GraphInfo();
    }

    public GraphInfo getLocalGraphInfo() {
        LOGGER.debug("Checking local graph info for %s...".formatted(routeProfileName));
        File localDir = new File(localPath);

        if (!localDir.exists()){
            return new GraphInfo().withLocalDirectory(localDir);
        }

        if (!localDir.isDirectory()) {
            throw new IllegalArgumentException("GraphHopperLocation cannot be an existing file. Has to be either non-existing or a folder.");
        }

        File graphInfoYML = new File(localDir, "ors_graph_info.yml");
        if (!graphInfoYML.exists() || !graphInfoYML.isFile()){
            LOGGER.debug("No ors_graph_info.yml found in %s".formatted(localPath));
            return new GraphInfo().withLocalDirectory(localDir);
        }

        Yaml yaml = new Yaml();
        InputStream inputStream = ORSGraphManager.class
                .getClassLoader()
                .getResourceAsStream(graphInfoYML.getAbsolutePath());
        ORSGraphInfoV1 graphInfoV1 = yaml.load(inputStream);
        return new GraphInfo().withLocalDirectory(localDir).withPersistedInfo(graphInfoV1);
    }

    public boolean shouldDownloadGraph(GraphInfo localGraphInfo, GraphInfo remoteGraphInfo) {
        if (!remoteGraphInfo.exists()) {
            LOGGER.info("There is no graph for %s/%s in remote repository.".formatted(routeProfileName, hash));
            return false;
        }
        if (!localGraphInfo.exists()) {
            return true;
        }
        if (!remoteGraphInfo.getPersistedGraphInfo().getOsmDate().isAfter(localGraphInfo.getPersistedGraphInfo().getOsmDate())) {
            LOGGER.info("OSM date of graph for %s/%s in remote repository is not newer than local graph - keeping local graph".formatted(routeProfileName, hash));
            return false;
        }
        LOGGER.info("OSM date of graph for %s/%s in remote repository is newer than local graph - should be downloaded".formatted(routeProfileName, hash));
        return true;
    }
}

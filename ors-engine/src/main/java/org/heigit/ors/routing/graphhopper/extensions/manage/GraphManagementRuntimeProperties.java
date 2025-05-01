package org.heigit.ors.routing.graphhopper.extensions.manage;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.GraphManagementProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.config.profile.RepoProperties;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import static java.util.Optional.ofNullable;

@Getter
/*
 * This class must not be used as configuration class.
 * It is created at runtime as container of properties for managing a named graph
 * which is used in constructors or methods of classes in the graph management package.
 */
public class GraphManagementRuntimeProperties {
    private boolean enabled;

    private String graphVersion;
    private String encoderName;

    private String localGraphsRootAbsPath;
    private String localProfileName;

    private String repoBaseUri;
    private String repoName;
    private String repoProfileGroup;
    private String repoCoverage;

    private String repoUser;
    private String repoPass;
    private int maxNumberOfGraphBackups = 0;

    private GraphRepoType derivedRepoType = GraphRepoType.NULL;
    private URL derivedRepoBaseUrl;
    private Path derivedRepoPath;

    private GraphManagementRuntimeProperties() {
    }

    public static class Builder {

        private boolean enabled;
        private String graphVersion;
        private String encoderName;
        private String localGraphsRootAbsPath;
        private String localProfileName;
        private String repoBaseUri;
        private String repoName;
        private String repoProfileGroup;
        private String repoCoverage;
        private String repoUser;
        private String repoPass;
        private int maxNumberOfGraphBackups = 0;

        public static Builder empty() {
            return new Builder();
        }

        public static Builder from(EngineProperties engineProperties, ProfileProperties profileProperties, String graphVersion) {
            Builder builder = new Builder();
            builder.enabled = ofNullable(engineProperties).map(EngineProperties::getGraphManagement).map(GraphManagementProperties::getEnabled).orElse(false);

            builder.repoBaseUri = ofNullable(profileProperties).map(ProfileProperties::getRepo).map(RepoProperties::getRepositoryUri).orElseGet(() ->
                    ofNullable(engineProperties).map(EngineProperties::getProfileDefault).map(ProfileProperties::getRepo).map(RepoProperties::getRepositoryUri).orElse(null));

            builder.repoName = ofNullable(profileProperties).map(ProfileProperties::getRepo).map(RepoProperties::getRepositoryName).orElseGet(() ->
                    ofNullable(engineProperties).map(EngineProperties::getProfileDefault).map(ProfileProperties::getRepo).map(RepoProperties::getRepositoryName).orElse(null));

            builder.repoCoverage = ofNullable(profileProperties).map(ProfileProperties::getRepo).map(RepoProperties::getGraphExtent).orElseGet(() ->
                    ofNullable(engineProperties).map(EngineProperties::getProfileDefault).map(ProfileProperties::getRepo).map(RepoProperties::getGraphExtent).orElse(null));

            builder.repoProfileGroup = ofNullable(profileProperties).map(ProfileProperties::getRepo).map(RepoProperties::getRepositoryProfileGroup).orElseGet(() ->
                    ofNullable(engineProperties).map(EngineProperties::getProfileDefault).map(ProfileProperties::getRepo).map(RepoProperties::getRepositoryProfileGroup).orElse(null));

            builder.repoUser = ofNullable(profileProperties).map(ProfileProperties::getRepo).map(RepoProperties::getRepositoryUser).orElseGet(() ->
                    ofNullable(engineProperties).map(EngineProperties::getProfileDefault).map(ProfileProperties::getRepo).map(RepoProperties::getRepositoryUser).orElse(null));
            builder.repoPass = ofNullable(profileProperties).map(ProfileProperties::getRepo).map(RepoProperties::getRepositoryPass).orElseGet(() ->
                    ofNullable(engineProperties).map(EngineProperties::getProfileDefault).map(ProfileProperties::getRepo).map(RepoProperties::getRepositoryPass).orElse(null));

            builder.maxNumberOfGraphBackups = ofNullable(engineProperties).map(EngineProperties::getGraphManagement).map(GraphManagementProperties::getMaxBackups).orElse(0);
            builder.graphVersion = graphVersion;
            builder.localProfileName = ofNullable(profileProperties).map(ProfileProperties::getProfileName).map(String::valueOf).orElse(null);

            builder.localGraphsRootAbsPath = ofNullable(profileProperties).map(ProfileProperties::getGraphPath).map(Path::toString).orElseGet(() ->
                    ofNullable(engineProperties).map(EngineProperties::getProfileDefault).map(ProfileProperties::getGraphPath).map(Path::toString).orElse(null));

            builder.encoderName = ofNullable(profileProperties).map(ProfileProperties::getEncoderName).map(String::valueOf).orElse(null);
            return builder;
        }


        public Builder withEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder withGraphVersion(String graphVersion) {
            this.graphVersion = graphVersion;
            return this;
        }

        public Builder withEncoderName(String encoderName) {
            this.encoderName = encoderName;
            return this;
        }

        public Builder withLocalGraphsRootAbsPath(String localGraphsRootAbsPath) {
            this.localGraphsRootAbsPath = localGraphsRootAbsPath;
            return this;
        }

        public Builder withLocalProfileName(String localProfileName) {
            this.localProfileName = localProfileName;
            return this;
        }

        public Builder withRepoBaseUri(String repoBaseUri) {
            this.repoBaseUri = repoBaseUri;
            return this;
        }

        public Builder withRepoName(String repoName) {
            this.repoName = repoName;
            return this;
        }

        public Builder withRepoProfileGroup(String repoProfileGroup) {
            this.repoProfileGroup = repoProfileGroup;
            return this;
        }

        public Builder withRepoCoverage(String repoCoverage) {
            this.repoCoverage = repoCoverage;
            return this;
        }

        public Builder withRepoUser(String repoUser) {
            this.repoUser = repoUser;
            return this;
        }

        public Builder withRepoPass(String repoPass) {
            this.repoPass = repoPass;
            return this;
        }

        public Builder withMaxNumberOfGraphBackups(int maxNumberOfGraphBackups) {
            this.maxNumberOfGraphBackups = maxNumberOfGraphBackups;
            return this;
        }

        public GraphManagementRuntimeProperties build() {
            GraphManagementRuntimeProperties properties = new GraphManagementRuntimeProperties();
            properties.enabled = this.enabled;
            properties.localGraphsRootAbsPath = this.localGraphsRootAbsPath;
            properties.graphVersion = this.graphVersion;
            properties.localProfileName = this.localProfileName;
            properties.repoBaseUri = this.repoBaseUri;
            properties.repoName = this.repoName;
            properties.repoProfileGroup = this.repoProfileGroup;
            properties.repoCoverage = this.repoCoverage;
            properties.repoUser = this.repoUser;
            properties.repoPass = this.repoPass;
            properties.encoderName = this.encoderName;
            properties.maxNumberOfGraphBackups = this.maxNumberOfGraphBackups;

            properties.deriveData();

            return properties;
        }
    }

    public enum GraphRepoType {
        HTTP, FILESYSTEM, MINIO, NULL
    }

    private void deriveData() {
        if (StringUtils.isNotBlank(repoBaseUri)) {
            try {
                URI uri = toUri(repoBaseUri);
                if (isSupportedUrlScheme(uri)) {
                    derivedRepoBaseUrl = toURL(uri);
                    derivedRepoType = GraphRepoType.HTTP;
                } else if (isSupportedFileScheme(uri)) {
                    derivedRepoPath = Path.of(uri);
                    derivedRepoType = GraphRepoType.FILESYSTEM;
                } else if (isSupportedMinioScheme(uri)) {
                    derivedRepoBaseUrl = toURL(uri);
                    derivedRepoType = GraphRepoType.MINIO;
                } else {
                    derivedRepoPath = Path.of(repoBaseUri);
                    derivedRepoType = GraphRepoType.FILESYSTEM;
                }
            } catch (Exception e) {
                derivedRepoType = GraphRepoType.NULL;
            }
        } else {
            derivedRepoType = GraphRepoType.NULL;
        }
    }

    private boolean isSupportedUrlScheme(URI uri) {
        if (uri == null) return false;
        return Arrays.asList("http", "https").contains(uri.getScheme());
    }

    private boolean isSupportedFileScheme(URI uri) {
        if (uri == null) return false;
        return Objects.equals("file", uri.getScheme());
    }

    private boolean isSupportedMinioScheme(URI uri) {
        if (uri == null) return false;
        return Objects.equals("minio", uri.getScheme());
    }

    private URI toUri(String string) {
        if (StringUtils.isBlank(string))
            return null;

        URI uri = URI.create(string);
        if (isSupportedUrlScheme(uri) || isSupportedFileScheme(uri) || isSupportedMinioScheme(uri)) {
            return uri;
        }

        return null;
    }

    private URL toURL(URI uri) {
        if (isSupportedUrlScheme(uri)) {
            try {
                return uri.toURL();
            } catch (MalformedURLException e) {
                return null;
            }
        }
        return null;
    }
}

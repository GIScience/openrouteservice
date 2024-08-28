package org.heigit.ors.routing.graphhopper.extensions.manage;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.profile.ProfileProperties;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

@Getter
/*
    * This class must not be used as configuration class.
    * It is created at runtime as container of properties for managing a named graph
    * which is used in constructors or methods of classes in the graph management package.
 */
public class GraphManagementRuntimeProperties {

    private String graphVersion;
    private String encoderName;

    private String localGraphsRootAbsPath;
    private String localProfileName;

    private String repoBaseUri;
    private String repoName;
    private String repoProfileGroup;
    private String repoCoverage;

    private GraphRepoType derivedRepoType = GraphRepoType.NULL;
    private URL derivedRepoBaseUrl;
    private Path derivedRepoPath;

    private GraphManagementRuntimeProperties() {}

    public static class Builder {

        private String graphVersion;
        private String encoderName;
        private String localGraphsRootAbsPath;
        private String localProfileName;
        private String repoBaseUri;
        private String repoName;
        private String repoProfileGroup;
        private String repoCoverage;

        public static Builder fromNew() {
            return new Builder();
        }

        public static Builder fromEngineProperties(EngineProperties engineProperties) {
            Builder builder = new Builder();
            builder.repoBaseUri = engineProperties.getGraphManagement().getRepositoryUri();
            builder.repoName = engineProperties.getGraphManagement().getRepositoryName();
            builder.repoCoverage = engineProperties.getGraphManagement().getGraphExtent();
            builder.repoProfileGroup = engineProperties.getGraphManagement().getRepositoryProfileGroup();
            return builder;

        }

        public static Builder from(EngineProperties engineProperties, String routeProfileName, String graphVersion) {
            Builder builder = new Builder();
            builder.repoBaseUri = engineProperties.getGraphManagement().getRepositoryUri();
            builder.repoName = engineProperties.getGraphManagement().getRepositoryName();
            builder.repoCoverage = engineProperties.getGraphManagement().getGraphExtent();
            builder.repoProfileGroup = engineProperties.getGraphManagement().getRepositoryProfileGroup();
            builder.graphVersion = graphVersion;
            builder.localProfileName = routeProfileName;
            ProfileProperties profileProperties = engineProperties.getProfiles().get(routeProfileName);
            builder.encoderName = Optional.ofNullable(profileProperties).map(ProfileProperties::getEncoderName).map(String::valueOf).orElse(null);
            return builder;
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

        public GraphManagementRuntimeProperties build() {
            GraphManagementRuntimeProperties properties = new GraphManagementRuntimeProperties();
            properties.localGraphsRootAbsPath = this.localGraphsRootAbsPath;
            properties.graphVersion = this.graphVersion;
            properties.localProfileName = this.localProfileName;
            properties.repoBaseUri = this.repoBaseUri;
            properties.repoName = this.repoName;
            properties.repoProfileGroup = this.repoProfileGroup;
            properties.repoCoverage = this.repoCoverage;
            properties.encoderName = this.encoderName;
            properties.deriveData();
            return properties;
        }
    }
    public enum GraphRepoType {
        HTTP, FILESYSTEM, NULL
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
        return Arrays.asList("file").contains(uri.getScheme());
    }

    URI toUri(String string) {
        if (StringUtils.isBlank(string))
            return null;

        URI uri = URI.create(string);
        if (isSupportedUrlScheme(uri) || isSupportedFileScheme(uri)) {
            return uri;
        }

        return null;
    }

    URL toURL(URI uri){
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

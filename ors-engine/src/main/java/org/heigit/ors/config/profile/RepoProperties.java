package org.heigit.ors.config.profile;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepoProperties {
    @JsonProperty("repository_uri")
    private String repositoryUri;
    @JsonProperty("repository_name")
    private String repositoryName;
    @JsonProperty("repository_profile_group")
    private String repositoryProfileGroup;
    @JsonProperty("graph_extent")
    private String graphExtent;

    public RepoProperties() {
    }

    public RepoProperties(String ignored) {
    }

    @JsonIgnore
    public boolean isEmpty() {
        return repositoryUri == null || repositoryName == null || repositoryProfileGroup == null || graphExtent == null;
    }
}

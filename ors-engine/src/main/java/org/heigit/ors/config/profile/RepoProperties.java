package org.heigit.ors.config.profile;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepoProperties {
    private String repositoryUri;
    private String repositoryName;
    private String repositoryProfileGroup;
    private String graphExtent;
    private String repositoryUser;
    private String repositoryPass;

    public RepoProperties() {
    }

    public RepoProperties(String ignored) {
        // This constructor is used to create an empty object for the purpose of ignoring it in the JSON serialization.
    }

    public boolean isEmpty() {
        return repositoryUri == null || repositoryName == null || repositoryProfileGroup == null || graphExtent == null;
    }
}

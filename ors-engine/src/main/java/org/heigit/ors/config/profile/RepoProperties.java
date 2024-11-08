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

    public RepoProperties() {
    }

    public RepoProperties(String ignored) {
    }

    public boolean isEmpty() {
        return repositoryUri == null || repositoryName == null || repositoryProfileGroup == null || graphExtent == null;
    }
}

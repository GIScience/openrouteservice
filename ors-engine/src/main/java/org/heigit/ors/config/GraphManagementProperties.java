package org.heigit.ors.config;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GraphManagementProperties {// TODO move to new package 'repo' and rename to GraphManagementConfigProperties

    @JsonProperty
    private Boolean enabled = false;

    @JsonProperty("graph_extent")
    private String graphExtent; // TODO move to ProfileRepoProperties

    @JsonProperty("repository_uri")
    private String repositoryUri; // TODO move to ProfileRepoProperties

    @JsonProperty("repository_name")
    private String repositoryName; // TODO move ProfileRepoProperties

    @JsonProperty("repository_profile_group")
    private String repositoryProfileGroup; // TODO move to ProfileRepoProperties

    @JsonProperty("download_schedule")
    private String downloadSchedule = "0 0 0 31 2 *";
    @JsonProperty("activation_schedule")
    private String activationSchedule = "0 0 0 31 2 *";
    @JsonProperty("max_backups")
    private Integer maxBackups = 0;
}

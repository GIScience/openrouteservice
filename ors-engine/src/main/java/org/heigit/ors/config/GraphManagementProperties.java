package org.heigit.ors.config;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class GraphManagementProperties {// TODO move to new package 'repo' and rename to GraphManagementConfigProperties


    //TODO add
//    @JsonProperty("profiles")
//    private Map<String,ProfileRepoProperties> profiles;

    //TODO add
//    @JsonProperty("encoder_name")
//    private EncoderNameEnum encoderName;

    @JsonProperty("graph_extent")
    private String graphExtent; // TODO move to ProfileRepoProperties

    @JsonProperty("repository_uri")
    private String repositoryUri; // TODO move to ProfileRepoProperties

    @JsonProperty("repository_name")
    private String repositoryName; // TODO move ProfileRepoProperties

    @JsonProperty("repository_profile_group")
    private String repositoryProfileGroup; // TODO move to ProfileRepoProperties

    @JsonProperty("download_schedule")
    private String downloadSchedule;

    @JsonProperty("activation_schedule")
    private String activationSchedule;

    @JsonProperty("max_backups")
    private Integer maxBackups;

}

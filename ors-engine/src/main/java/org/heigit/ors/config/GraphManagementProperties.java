package org.heigit.ors.config;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GraphManagementProperties {// TODO move to new package 'repo' and rename to GraphManagementConfigProperties
    private Boolean enabled;
    private String downloadSchedule;
    private String activationSchedule;
    private Integer maxBackups;
}

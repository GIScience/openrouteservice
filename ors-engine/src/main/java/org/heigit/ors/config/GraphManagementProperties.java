package org.heigit.ors.config;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GraphManagementProperties {
    private Boolean enabled;
    private String downloadSchedule;
    private String activationSchedule;
    private Integer maxBackups;
}

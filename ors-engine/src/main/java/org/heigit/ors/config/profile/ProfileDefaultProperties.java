package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@Deprecated //TODO remove
@JsonIgnoreProperties({"encoder_name"})
public class ProfileDefaultProperties extends ProfileProperties {
}

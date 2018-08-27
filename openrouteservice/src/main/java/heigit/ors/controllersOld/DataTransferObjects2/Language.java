package heigit.ors.controllersOld.DataTransferObjects2;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Language {
    @JsonProperty("en") ENGLISH,
    @JsonProperty("de") GERMAN,
    @JsonProperty("ch") CHINESE
}

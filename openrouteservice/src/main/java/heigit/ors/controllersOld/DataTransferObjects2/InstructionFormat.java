package heigit.ors.controllersOld.DataTransferObjects2;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum InstructionFormat {
    @JsonProperty("html") HTML,
    @JsonProperty("text") TEXT
}

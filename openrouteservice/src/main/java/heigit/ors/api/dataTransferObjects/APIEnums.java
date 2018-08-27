package heigit.ors.api.dataTransferObjects;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

public class APIEnums {
    @ApiModel(value = "Specify which type of border crossing to avoid", description = "blah blah blah")
    public enum AvoidBorders {
        @ApiModelProperty(value = "Avoid all borders")
        ALL {
            public String toString() {
                return "all";
            }
        },
        @ApiModelProperty(value = "Avoid controlled borders")
        CONTROLLED {
            public String toString() {
                return "controlled";
            }
        },
        @ApiModelProperty(value = "Do not avoid border crossings")
        NONE {
            public String toString() {
                return "none";
            }
        }
    }
}

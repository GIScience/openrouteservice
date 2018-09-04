package heigit.ors.api.requests.routing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Restrictions", parent = RequestProfileParams.class)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RequestProfileParamsRestrictions {
    @ApiModelProperty(value = "Only for avoided hills or specified steepness_difficulty. Specifies the maximum route steepness in percent. Values range from 1 to 15. Routes with a higher gradient are avoided.",
            example = "5")
    @JsonProperty("gradient")
    private Integer gradient;
    @JsonIgnore
    private boolean hasGradient = false;

    @ApiModelProperty(value = "Length restriction in metres.", example = "8.4")
    @JsonProperty("length")
    private Float length;
    private boolean hasLength = false;

    @ApiModelProperty(value = "Width restriction in metres.", example = "5.6")
    @JsonProperty("width")
    private Float width;
    @JsonIgnore
    private boolean hasWidth = false;

    @ApiModelProperty(value = "Height restriction in metres", example = "4.2")
    @JsonProperty("height")
    private Float height;
    @JsonIgnore
    private boolean hasHeight = false;

    @ApiModelProperty(value = "Axleload restriction in tons", example = "50")
    @JsonProperty("axleload")
    private Float axleLoad;
    @JsonIgnore
    private boolean hasAxleLoad = false;

    @ApiModelProperty(value = "Weight restriction in tons", example = "40")
    @JsonProperty("weight")
    private Float weight;
    @JsonIgnore
    private boolean hasWeight = false;

    @ApiModelProperty(value = "Specifies whether to use appropriate routing for delivering hazardous goods and avoiding water protected areas. Default is false.")
    @JsonProperty(value = "hazmat", defaultValue = "false")
    private Boolean hazardousMaterial = false;
    @JsonIgnore
    private boolean hasHazardousMaterial = false;

    @ApiModelProperty(value = "Specifies the minimum surface type. Default is \"cobblestone:flattened\".", example = "asphalt")
    @JsonProperty(value = "surface_type", defaultValue = "cobblestone:flattened")
    private String surfaceType = "cobblestone:flattened";
    @JsonIgnore
    private boolean hasSurfaceType = false;

    @ApiModelProperty(value = "Specifies the minimum grade of the route. Default is \"grade1\".", example = "grade2")
    @JsonProperty(value = "track_type", defaultValue = "grade1")
    private String trackType = "grade1";
    @JsonIgnore
    private boolean hasTrackType = false;

    @ApiModelProperty(value = "Specifies the minimum smoothness of the route. Default is \"good\".", example = "best")
    @JsonProperty(value = "smoothness_type", defaultValue = "good")
    private String smoothnessType = "good";
    @JsonIgnore
    private boolean hasSmoothnessType = false;

    @ApiModelProperty(value = "Specifies the maximum height of the sloped curb in metres. Values are 0.03, 0.06(default), 0.1.", example = "0.03")
    @JsonProperty(value = "maximum_sloped_kerb", defaultValue = "0.1")
    private Float maxSlopedKerb = 0.1f;
    @JsonIgnore
    private boolean hasMaxSlopedKerb = false;

    @ApiModelProperty(value = "Specifies the maximum incline as a percentage. 3, 6(default), 10, 15", example = "3")
    @JsonProperty(value = "maximum_incline", defaultValue = "6")
    private Integer maxIncline = 6;
    @JsonIgnore
    private boolean hasMaxIncline = false;

    @ApiModelProperty(value = "Specifies the minimum width of the footway in metres", example = "2.5")
    @JsonProperty(value = "minimum:width")
    private Integer minWidth;
    @JsonIgnore
    private boolean hasMinWidth = false;

    @ApiModelProperty(value = "Specifies the maximum trail difficulty", example = "3")
    @JsonProperty(value = "trail_difficulty", defaultValue = "1")
    private Integer trailDifficulty = 1;
    @JsonIgnore
    private boolean hasTrailDifficulty = false;

    public Integer getGradient() {
        return gradient;
    }

    public void setGradient(Integer gradient) {
        this.gradient = gradient;
        hasGradient = true;
    }

    public Float getLength() {
        return length;
    }

    public void setLength(Float length) {
        this.length = length;
        hasLength = true;
    }

    public Float getWidth() {
        return width;
    }

    public void setWidth(Float width) {
        this.width = width;
        hasWidth = true;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
        hasHeight = true;
    }

    public Float getAxleLoad() {
        return axleLoad;
    }

    public void setAxleLoad(Float axleLoad) {
        this.axleLoad = axleLoad;
        hasAxleLoad = true;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
        hasWeight = true;
    }

    public Boolean hazardousMaterial() {
        return hazardousMaterial;
    }

    public void setHazardousMaterial(Boolean hazardousMaterial) {
        this.hazardousMaterial = hazardousMaterial;
        hasHazardousMaterial = true;
    }

    public String getSurfaceType() {
        return surfaceType;
    }

    public void setSurfaceType(String surfaceType) {
        this.surfaceType = surfaceType;
        hasSurfaceType = true;
    }

    public String getTrackType() {
        return trackType;
    }

    public void setTrackType(String trackType) {
        this.trackType = trackType;
        hasTrackType = true;
    }

    public String getSmoothnessType() {
        return smoothnessType;
    }

    public void setSmoothnessType(String smoothnessType) {
        this.smoothnessType = smoothnessType;
        hasSmoothnessType = true;
    }

    public Float getMaxSlopedKerb() {
        return maxSlopedKerb;
    }

    public void setMaxSlopedKerb(Float maxSlopedKerb) {
        this.maxSlopedKerb = maxSlopedKerb;
        hasMaxSlopedKerb = true;
    }

    public Integer getMaxIncline() {
        return maxIncline;
    }

    public void setMaxIncline(Integer maxIncline) {
        this.maxIncline = maxIncline;
        hasMaxIncline = true;
    }

    public Integer getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(Integer minWidth) {
        this.minWidth = minWidth;
        this.hasMinWidth = true;
    }

    public Integer getTrailDifficulty() {
        return trailDifficulty;
    }

    public void setTrailDifficulty(Integer trailDifficulty) {
        this.trailDifficulty = trailDifficulty;
        hasTrailDifficulty = true;
    }

    public boolean hasGradient() {
        return hasGradient;
    }

    public boolean hasLength() {
        return hasLength;
    }

    public boolean hasWidth() {
        return hasWidth;
    }

    public boolean hasHeight() {
        return hasHeight;
    }

    public boolean hasAxleLoad() {
        return hasAxleLoad;
    }

    public boolean hasWeight() {
        return hasWeight;
    }

    public Boolean getHazardousMaterial() {
        return hazardousMaterial;
    }

    public boolean hasHazardousMaterial() {
        return hasHazardousMaterial;
    }

    public boolean hasSurfaceType() {
        return hasSurfaceType;
    }

    public boolean hasTrackType() {
        return hasTrackType;
    }

    public boolean hasSmoothnessType() {
        return hasSmoothnessType;
    }

    public boolean hasMaxSlopedKerb() {
        return hasMaxSlopedKerb;
    }

    public boolean hasMaxIncline() {
        return hasMaxIncline;
    }

    public boolean hasMinWidth() {
        return hasMinWidth;
    }

    public boolean hasTrailDifficulty() {
        return hasTrailDifficulty;
    }
}

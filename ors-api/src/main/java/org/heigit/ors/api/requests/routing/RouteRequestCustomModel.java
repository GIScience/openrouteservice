package org.heigit.ors.api.requests.routing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.graphhopper.jackson.StatementDeserializer;
import com.graphhopper.json.Statement;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.JsonFeature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteRequestCustomModel {
    @JsonProperty("distance_influence")
    private double distanceInfluence;

    @JsonProperty("heading_penalty")
    private double headingPenalty = (double) 300.0F;

    @JsonProperty("speed")
    @JsonDeserialize(contentUsing = StatementDeserializer.class)
    private List<Statement> speedStatements = new ArrayList<>();

    @JsonProperty("priority")
    @JsonDeserialize(contentUsing = StatementDeserializer.class)
    private List<Statement> priorityStatements = new ArrayList<>();

    @JsonProperty("areas")
    private Map<String, JsonFeature> areas = new HashMap();

    public CustomModel toGHCustomModel() {
        CustomModel customModel = new CustomModel();
        customModel.setDistanceInfluence(this.distanceInfluence);
        customModel.setHeadingPenalty(this.headingPenalty);
        this.speedStatements.forEach(customModel::addToSpeed);
        this.priorityStatements.forEach(customModel::addToPriority);
        customModel.setAreas(this.areas);
        return customModel;
    }
}
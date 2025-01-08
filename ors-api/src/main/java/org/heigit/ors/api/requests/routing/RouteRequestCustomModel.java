package org.heigit.ors.api.requests.routing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.graphhopper.jackson.StatementDeserializer;
import com.graphhopper.json.Statement;
import com.graphhopper.util.CustomModel;

import java.util.ArrayList;
import java.util.List;

public class RouteRequestCustomModel {
    @JsonProperty("distance_influence")
    private Double distanceInfluence;

    @JsonProperty("heading_penalty")
    private double headingPenalty = (double) 300.0F;

    @JsonProperty("speed")
    @JsonDeserialize(contentUsing = StatementDeserializer.class)
    private List<Statement> speedStatements = new ArrayList<>();

    @JsonProperty("priority")
    @JsonDeserialize(contentUsing = StatementDeserializer.class)
    private List<Statement> priorityStatements = new ArrayList<>();

    public CustomModel toGHCustomModel() {
        CustomModel customModel = new CustomModel();
        customModel.setDistanceInfluence(this.distanceInfluence);
        customModel.setHeadingPenalty(this.headingPenalty);
        this.speedStatements.forEach(customModel::addToSpeed);
        this.priorityStatements.forEach(customModel::addToPriority);
        return customModel;
    }
}

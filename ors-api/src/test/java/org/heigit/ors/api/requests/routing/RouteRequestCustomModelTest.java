/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package org.heigit.ors.api.requests.routing;

import com.graphhopper.json.Statement;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.JsonFeature;
import org.heigit.ors.api.config.ApiConfig;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The request-side custom model and its conversion to the GraphHopper one. The conditions themselves are not
 * interpreted here, they are passed on as written and checked when GraphHopper compiles the model.
 */
class RouteRequestCustomModelTest {

    // the mapper the API itself uses, so the areas go through the same JTS module as in production
    private final JsonMapper mapper = new ApiConfig().objectMapper();

    private CustomModel convert(String json) {
        return mapper.readValue(json, RouteRequestCustomModel.class).toGHCustomModel();
    }

    @Test
    void convertsSpeedAndPriorityStatements() {
        CustomModel customModel = convert("""
                {
                  "distance_influence": 120,
                  "speed": [{"if": "road_class == MOTORWAY", "limit_to": 100}],
                  "priority": [
                    {"if": "road_environment == TUNNEL", "multiply_by": 0},
                    {"else_if": "road_class == PRIMARY", "multiply_by": 0.5}
                  ]
                }
                """);

        assertEquals(120, customModel.getDistanceInfluence(), 1.e-6);

        List<Statement> speed = customModel.getSpeed();
        assertEquals(1, speed.size());
        assertEquals("road_class == MOTORWAY", speed.get(0).getCondition());
        assertEquals(Statement.Keyword.IF, speed.get(0).getKeyword());
        assertEquals(Statement.Op.LIMIT, speed.get(0).getOperation());
        assertEquals(100, speed.get(0).getValue(), 1.e-6);

        List<Statement> priority = customModel.getPriority();
        assertEquals(2, priority.size());
        assertEquals("road_environment == TUNNEL", priority.get(0).getCondition());
        assertEquals(Statement.Keyword.IF, priority.get(0).getKeyword());
        assertEquals(Statement.Keyword.ELSEIF, priority.get(1).getKeyword());
        assertEquals(Statement.Op.MULTIPLY, priority.get(1).getOperation());
        assertEquals(0.5, priority.get(1).getValue(), 1.e-6);
    }

    @Test
    void convertsAreas() {
        CustomModel customModel = convert("""
                {
                  "priority": [{"if": "in_custom1", "multiply_by": 0}],
                  "areas": {
                    "custom1": {
                      "type": "Feature",
                      "geometry": {"type": "Polygon", "coordinates": [[[8.7, 49.4], [8.8, 49.4], [8.8, 49.5], [8.7, 49.4]]]}
                    }
                  }
                }
                """);

        Map<String, JsonFeature> areas = customModel.getAreas();
        assertEquals(1, areas.size());
        assertTrue(areas.containsKey("custom1"));
        assertEquals("Polygon", areas.get("custom1").getGeometry().getGeometryType());
        assertEquals("in_custom1", customModel.getPriority().get(0).getCondition());
    }

    @Test
    void emptyModelKeepsGraphHopperDefaults() {
        CustomModel customModel = convert("{}");

        assertEquals(0, customModel.getDistanceInfluence(), 1.e-6);
        assertTrue(customModel.getSpeed().isEmpty());
        assertTrue(customModel.getPriority().isEmpty());
        assertTrue(customModel.getAreas().isEmpty());
    }

    @Test
    void conditionsArePassedOnUnchanged() {
        // whatever the condition looks like, the request layer must not rewrite, trim or escape it: GraphHopper
        // validates it and builds the code from the parsed expression
        String condition = "road_class ==\\n\\tPRIMARY || road_environment == TUNNEL";
        CustomModel customModel = convert("""
                {"priority": [{"if": "%s", "multiply_by": 0.5}]}
                """.formatted(condition));

        assertEquals("road_class ==\n\tPRIMARY || road_environment == TUNNEL", customModel.getPriority().get(0).getCondition());
    }
}

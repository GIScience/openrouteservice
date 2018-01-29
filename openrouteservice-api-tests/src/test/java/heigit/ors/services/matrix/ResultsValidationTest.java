/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   	 http://www.giscience.uni-hd.de
 *   	 http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.services.matrix;

import static io.restassured.RestAssured.*;

import org.junit.Test;

import org.json.JSONArray;
import org.json.JSONObject;

import heigit.ors.services.common.EndPointAnnotation;
import heigit.ors.services.common.ServiceTest;
import io.restassured.response.Response;
import junit.framework.Assert;


@EndPointAnnotation(name = "matrix")
public class ResultsValidationTest extends ServiceTest {
    public ResultsValidationTest() {
        addParameter("locations", "8.690733,49.387283|8.692771,49.385118|8.686409,49.426272");
        addParameter("sources1", "0,1");
        addParameter("destinations1", "2");
        addParameter("destinations2", "2,3");
        addParameter("preference", "fastest");
        addParameter("carProfile", "driving-car");
        addParameter("manyLocations",
                "8.676882,49.425245|" +
                        "8.680882,49.425255|" +
                        "8.685882,49.425245|" +
                        "8.690882,49.425245|" +
                        "8.695882,49.425245|" +
                        "8.706882,49.425245|" +

                        "8.690882,49.425245|" +
                        "8.690882,49.415245|" +
                        "8.690882,49.405245|" +
                        "8.690882,49.395245|" +
                        "8.690882,49.385245|" +
                        "8.690882,49.375245");
        addParameter("manyLocationsArray", new String[]
                {"8.676882,49.425245",
                        "8.680882,49.425255",
                        "8.685882,49.425245",
                        "8.690882,49.425245",
                        "8.695882,49.425245",
                        "8.706882,49.425245",

                        "8.690882,49.425245",
                        "8.690882,49.415245",
                        "8.690882,49.405245",
                        "8.690882,49.395245",
                        "8.690882,49.385245",
                        "8.690882,49.375245"});
    }

    @Test
    public void emptySourcesAndDestinationsTest() {
        Response response = given()
                .param("locations", getParameter("locations"))
                .param("metrics", "distance")
                .param("profile", "driving-car")
                .when()
                .get(getEndPointName());

        Assert.assertEquals(response.getStatusCode(), 200);
        JSONObject jResponse = new JSONObject(response.body().asString());
        checkTableDimensions(jResponse, "distances", 3, 3);
    }
    /*
	@Test
	public void nonExistingLocationEntryTest() {
		Response response = given()
				.param("locations", "8.690733,49.387283|8.686409,49.426272|18.686409,49.426272")
				.param("sources", "0")
				.param("destinations", "1,2")
				.param("metrics", "distance")
				.param("profile", "driving-car") 
				.when()
				.get(getEndPointName());

		Assert.assertEquals(response.getStatusCode(), 200);
		JSONObject jResponse = new JSONObject(response.body().asString());
		checkTableDimensions(jResponse, "distances", 1, 2);
		JSONArray jDistances = jResponse.getJSONArray("distances").getJSONArray(0);
		Assert.assertEquals(jDistances.get(1), JSONObject.NULL);
	}*/
/*	 
	@Test
	public void emptyLocationEntryTest() {
		Response response = given()
				.param("locations", "8.690733,49.387283|8.686409,49.426272")
				.param("sources", "0")
				.param("destinations", "1")
				.param("metrics", "duration|distance")
				.param("profile", "driving-car") 
				.when()
				.get(getEndPointName());

		Assert.assertEquals(response.getStatusCode(), 200);
		JSONObject jResponse = new JSONObject(response.body().asString());
		checkTableDimensions(jResponse, "distances", 2, 1);
	}
	*/

    @Test
    public void distanceTableTest() {
        Response response = given()
                .param("locations", getParameter("locations"))
                .param("sources", getParameter("sources1"))
                .param("destinations", getParameter("destinations1"))
                .param("metrics", "distance")
                .param("profile", "driving-car")
                .when()
                .get(getEndPointName());

        Assert.assertEquals(response.getStatusCode(), 200);
        JSONObject jResponse = new JSONObject(response.body().asString());
        checkTableDimensions(jResponse, "distances", 2, 1);
    }

    @Test
    public void durationTableTest() {
        Response response = given()
                .param("locations", getParameter("locations"))
                .param("sources", getParameter("sources1"))
                .param("destinations", getParameter("destinations1"))
                .param("metrics", "duration")
                .param("profile", "driving-car")
                .when()
                .get(getEndPointName());

        Assert.assertEquals(response.getStatusCode(), 200);
        JSONObject jResponse = new JSONObject(response.body().asString());
        checkTableDimensions(jResponse, "durations", 2, 1);
    }

    @Test
    public void durationAndDistanceTablesTest() {
        Response response = given()
                .param("locations", getParameter("locations"))
                .param("sources", getParameter("sources1"))
                .param("destinations", getParameter("destinations1"))
                .param("metrics", "distance|duration")
                .param("profile", "driving-car")
                .when()
                .log().all()
                .get(getEndPointName());

        Assert.assertEquals(response.getStatusCode(), 200);
        JSONObject jResponse = new JSONObject(response.body().asString());
        checkTableDimensions(jResponse, "durations", 2, 1);
        checkTableDimensions(jResponse, "distances", 2, 1);
    }

    @Test
    public void idParameterTest() {
        Response response = given()
                .param("locations", getParameter("locations"))
                .param("sources", getParameter("sources1"))
                .param("destinations", getParameter("destinations1"))
                .param("profile", "driving-car")
                .param("id", "34629723410")
                .when()
                .get(getEndPointName());

        Assert.assertEquals(response.getStatusCode(), 200);
        JSONObject jResponse = new JSONObject(response.body().asString());
        Assert.assertEquals(jResponse.getJSONObject("info").getJSONObject("query").get("id"), "34629723410");
    }

    @Test
    public void resolveNamesParameterTest() {
        Response response = given()
                .param("locations", getParameter("locations"))
                .param("sources", getParameter("sources1"))
                .param("destinations", getParameter("destinations1"))
                .param("profile", "driving-car")
                .param("resolve_locations", "true")
                .when()
                .get(getEndPointName());

        Assert.assertEquals(response.getStatusCode(), 200);
        JSONObject jResponse = new JSONObject(response.body().asString());
        Assert.assertEquals(true, jResponse.getJSONArray("sources").getJSONObject(0).has("name"));
    }

    private void checkTableDimensions(JSONObject json, String tableName, int rows, int columns) {
        Assert.assertEquals(true, json.has(tableName));

        JSONArray jTable = json.getJSONArray(tableName);
        Assert.assertEquals(jTable.length(), rows);
        Assert.assertEquals(jTable.getJSONArray(0).length(), columns);
    }
    /**
     * Queries the matrix API with 12x12 symmetrical matrix. Queries the routing API
     * with the same 12x12 single queries. Compares results. If results are within .2m of
     * each other, test passes. This way the result of the matrix API is bound to be
     * the same as the routing API
     */
    @Test
    public void distanceTest() {
        //Query Matrix API
        Response response = given()
                .param("locations", getParameter("manyLocations"))
                .param("sources", "all")
                .param("destinations","all")
                .param("metrics", "distance")
                .param("profile", "driving-car")
                .param("resolve_locations", "true")
                .when()
                .get(getEndPointName());

        String[] locations = (String[]) getParameter("manyLocationsArray");

        Assert.assertEquals(response.getStatusCode(), 200);
        JSONObject jResponse = new JSONObject(response.body().asString());
        JSONArray jDistances = jResponse.getJSONArray("distances");
        //Query Routing API 12x12 times
        for(int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                Response response2 = given()
                        .param("coordinates", locations[i] + "|" + locations[j])
                        .param("instructions", "false")
                        .param("preference", getParameter("preference"))
                        .param("profile", getParameter("carProfile"))
                        .when()
                        .get("routes");

                JSONObject jResponseRouting = new JSONObject(response2.body().asString());
                JSONObject jRoute = (jResponseRouting.getJSONArray("routes")).getJSONObject(0);
                double routeDistance = jRoute.getJSONObject("summary").getDouble("distance");
                double matrixDistance = jDistances.getJSONArray(i).getDouble(j);
                Assert.assertTrue( matrixDistance - .1 < routeDistance);
                Assert.assertTrue( matrixDistance + .1 > routeDistance);

            }
        }
    }
}

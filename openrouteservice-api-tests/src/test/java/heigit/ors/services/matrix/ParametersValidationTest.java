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
import static org.hamcrest.Matchers.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import heigit.ors.services.common.EndPointAnnotation;
import heigit.ors.services.common.ServiceTest;

@EndPointAnnotation(name="matrix")
public class ParametersValidationTest extends ServiceTest {

	public ParametersValidationTest() {
	}

	@Test
	public void profileWrongValueTest() {
		given()
		.param("profile", "driving-car2")
		.param("sources", "8.5,48.7|8.6,49.1")
		.param("destinations", "10.5,48.7")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(MatrixErrorCodes.INVALID_PARAMETER_VALUE))
		.statusCode(400);
	}
	
	@Test
	public void profileWrongValueTest_POST() {
		JSONObject jRequest = new JSONObject();
		jRequest.put("profile", "driving-car2");
		JSONArray jLocations = new JSONArray();
		jLocations.put(1);
		jRequest.put("locations", jLocations);
		
		given()
		.contentType("application/json")
		.body(jRequest.toString())
		.when()
		.post(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(MatrixErrorCodes.INVALID_PARAMETER_VALUE))
		.statusCode(400);
	}

	@Test
	public void missingProfileFormatTest() {
		given()
		.param("profile2", "driving-car")
		.param("locations", "8.5,48.7|8.6,49.1")
		.param("sources", "all")
		.param("destinations", "all")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(MatrixErrorCodes.MISSING_PARAMETER))
		.statusCode(400);
	}
	
	@Test
	public void missingProfileFormatTest_POST() {
		JSONObject jRequest = new JSONObject();
		jRequest.put("profile2", "driving-car");
		JSONArray jLocations = new JSONArray();
		jLocations.put(1);
		jRequest.put("locations", jLocations);
		
		given()
		.contentType("application/json")
		.body(jRequest.toString())
		.when()
		.post(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(MatrixErrorCodes.MISSING_PARAMETER))
		.statusCode(400);
	}
	
	@Test
	public void locationsEmptyTest() {
		given()
		.param("profile", "driving-car")
		.param("locations", "")
		.param("sources", "all")
		.param("destinations", "all")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(MatrixErrorCodes.MISSING_PARAMETER))
		.statusCode(400);
	}
	
	@Test
	public void locationsEmptyTest_POST() {
		JSONObject jRequest = new JSONObject();
		jRequest.put("profile", "driving-car");
		JSONArray jLocations = new JSONArray();
		jRequest.put("locations", jLocations);
		
		given()
		.contentType("application/json")
		.body(jRequest.toString())
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(MatrixErrorCodes.MISSING_PARAMETER))
		.statusCode(400);
		
		jRequest = new JSONObject();
		jRequest.put("profile", "driving-car");
		
		given()
		.contentType("application/json")
		.body(jRequest.toString())
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(MatrixErrorCodes.MISSING_PARAMETER))
		.statusCode(400);
	}
	
	@Test
	public void locationsFormatTest() {
		given()
		.param("profile", "driving-car")
		.param("locations", "8.5,48.7|8.6,49.1b")
		.param("sources", "all")
		.param("destinations", "all")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(MatrixErrorCodes.INVALID_PARAMETER_FORMAT))
		.statusCode(400);
	}
	
	@Test
	public void locationsFormatTest_POST() {
		JSONObject jRequest = new JSONObject();
		jRequest.put("profile", "driving-car");
		JSONArray jLocations = new JSONArray();
		jLocations.put(new JSONArray(new double[] {8.5,48.7}));
		jLocations.put(new JSONArray(new String[] {"8.6","49.1b"}));
		jRequest.put("locations", jLocations);
		jRequest.put("sources", "all");
		jRequest.put("destinations", "all");
		
		given()
		.contentType("application/json")
		.body(jRequest.toString())
		.when()
		.post(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(MatrixErrorCodes.INVALID_PARAMETER_FORMAT))
		.statusCode(400);
	}
	
	@Test
	public void destinationsFormatTest() {
		given()
		.param("profile", "driving-car")
		.param("locations", "8.5,48.7|8.6,49.1")
		.param("sources", "all")
		.param("destinations", "0b,1")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(MatrixErrorCodes.INVALID_PARAMETER_FORMAT))
		.statusCode(400);
	}
	
	@Test
	public void destinationsFormatTest_POST() {
		JSONObject jRequest = new JSONObject();
		jRequest.put("profile", "driving-car");
		JSONArray jLocations = new JSONArray();
		jLocations.put(new JSONArray(new double[] {8.5,48.7}));
		jLocations.put(new JSONArray(new String[] {"8.6","49.1b"}));
		jRequest.put("locations", jLocations);
		jRequest.put("sources", "all");
		JSONArray jDestinations = new JSONArray();
		jDestinations.put("0");
		jDestinations.put("1b");
		jRequest.put("destinations", jDestinations);
		
		given()
		.contentType("application/json")
		.body(jRequest.toString())
		.when()
		.post(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(MatrixErrorCodes.INVALID_PARAMETER_FORMAT))
		.statusCode(400);
	}
	
	@Test
	public void sourcesFormatTest() {
		given()
		.param("profile", "driving-car")
		.param("locations", "8.5,48.7|8.6,49.1")
		.param("sources", "0,1c")
		.param("destinations", "0")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(MatrixErrorCodes.INVALID_PARAMETER_FORMAT))
		.statusCode(400);
	}
	
	@Test
	public void sourcesFormatTest_POST() {
		JSONObject jRequest = new JSONObject();
		jRequest.put("profile", "driving-car");
		JSONArray jLocations = new JSONArray();
		jLocations.put(new JSONArray(new double[] {8.5,48.7}));
		jLocations.put(new JSONArray(new double[] {8.6, 49.1}));
		jRequest.put("locations", jLocations);
		JSONArray jSources = new JSONArray();
		jSources.put("0");
		jSources.put("1b");
		jRequest.put("sources", jSources);
		jRequest.put("destinations", "all");
		
		given()
		.contentType("application/json")
		.body(jRequest.toString())
		.when()
		.post(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(MatrixErrorCodes.INVALID_PARAMETER_FORMAT))
		.statusCode(400);
	}
	
	@Test
	public void sourcesOutOfRangeTest() {
		given()
		.param("profile", "driving-car")
		.param("locations", "8.5,48.7|8.6,49.1")
		.param("sources", "0,3")
		.param("destinations", "0")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(MatrixErrorCodes.INVALID_PARAMETER_FORMAT))
		.statusCode(400);
	}
	
	@Test
	public void sourcesOutOfRangeTest_POST() {
		JSONObject jRequest = new JSONObject();
		jRequest.put("profile", "driving-car");
		JSONArray jLocations = new JSONArray();
		jLocations.put(new JSONArray(new double[] {8.5,48.7}));
		jLocations.put(new JSONArray(new double[] {8.6, 49.1}));
		jRequest.put("locations", jLocations);
		JSONArray jSources = new JSONArray();
		jSources.put("0");
		jSources.put("3");
		jRequest.put("sources", jSources);
		jRequest.put("destinations", "all");
		
		given()
		.contentType("application/json")
		.body(jRequest.toString())
		.when()
		.post(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(MatrixErrorCodes.INVALID_PARAMETER_FORMAT))
		.statusCode(400);
	}
	
	@Test
	public void destinationsOutOfRangeTest() {
		given()
		.param("profile", "driving-car")
		.param("locations", "8.5,48.7|8.6,49.1")
		.param("sources", "0,1")
		.param("destinations", "-1")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(MatrixErrorCodes.INVALID_PARAMETER_FORMAT))
		.statusCode(400);
	}

	@Test
	public void metricsUnknownValueTest() {
		given()
		.param("profile", "driving-car")
		.param("locations", "8.5,48.7|8.6,49.1")
		.param("sources", "all")
		.param("destinations", "all")
		.param("metrics", "time|durationO")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(MatrixErrorCodes.INVALID_PARAMETER_VALUE))
		.statusCode(400);
	}

	@Test
	public void metricsUnknownValueTest_POST() {
		JSONObject jRequest = new JSONObject();
		jRequest.put("profile", "driving-car");
		JSONArray jLocations = new JSONArray();
		jLocations.put(new JSONArray(new double[] {8.5,48.7}));
		jLocations.put(new JSONArray(new double[] {8.6, 49.1}));
		jRequest.put("locations", jLocations);
		jRequest.put("sources", "all");
		jRequest.put("destinations", "all");
		jRequest.put("metrics", "time|durationO");
		
		given()
		.contentType("application/json")
		.body(jRequest.toString())
		.when()
		.post(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(MatrixErrorCodes.INVALID_PARAMETER_VALUE))
		.statusCode(400);
	}

	@Test
	public void resolveLocationsFormatTest() {
		given()
		.param("profile", "driving-car")
		.param("locations", "8.5,48.7|8.6,49.1")
		.param("sources", "all")
		.param("destinations", "all")
		.param("resolve_locations", "trues")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(MatrixErrorCodes.INVALID_PARAMETER_FORMAT))
		.statusCode(400);
	}

	@Test
	public void pointOutOfBoundsTest() {
		given()
				.param("profile", "driving-car")
				.param("locations", "9.0,48.7|9.0,49.1")
				.param("sources", "all")
				.param("destinations", "all")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("error.code", is(MatrixErrorCodes.POINT_NOT_FOUND))
				.statusCode(404);
	}
}

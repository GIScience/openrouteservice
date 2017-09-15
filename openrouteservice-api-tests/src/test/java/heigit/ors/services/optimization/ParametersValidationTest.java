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
package heigit.ors.services.optimization;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

import heigit.ors.services.common.EndPointAnnotation;
import heigit.ors.services.common.ServiceTest;

@EndPointAnnotation(name="optimized_routes")
public class ParametersValidationTest extends ServiceTest {

	public ParametersValidationTest() {
	}

	@Test
	public void profileWrongValueTest() {
		given()
		.param("profile", "driving-car2")
		.param("locations", "8.5,48.7|8.6,49.1")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(703))
		.statusCode(400);
	}
	
	@Test
	public void missingProfileFormatTest() {
		given()
		.param("profile2", "driving-car")
		.param("locations", "8.5,48.7|8.6,49.1")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(701))
		.statusCode(400);
	}
	
	@Test
	public void locationsEmptyTest() {
		given()
		.param("profile", "driving-car")
		.param("locations", "")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(701))
		.statusCode(400);
	}
	
	@Test
	public void locationsFormatTest() {
		given()
		.param("profile", "driving-car")
		.param("locations", "8.5,48.7|8.6,49.1b")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(702))
		.statusCode(400);
	}
	
	@Test
	public void sourceFormatTest() {
		given()
		.param("profile", "driving-car")
		.param("locations", "8.5,48.7|8.6,49.1")
		.param("source", "any2")
		.param("destination", "any")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(702))
		.statusCode(400);
	}
	
	@Test
	public void destinationFormatTest() {
		given()
		.param("profile", "driving-car")
		.param("locations", "8.5,48.7|8.6,49.1")
		.param("source", 0)
		.param("destination", "any2")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(702))
		.statusCode(400);
	}
	
	@Test
	public void sourceOutOfRangeTest() {
		given()
		.param("profile", "driving-car")
		.param("locations", "8.5,48.7|8.6,49.1")
		.param("source", 5)
		.param("destination", "any")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(703))
		.statusCode(400);
	}
	
	@Test
	public void destinationOutOfRangeTest() {
		given()
		.param("profile", "driving-car")
		.param("locations", "8.5,48.7|8.6,49.1")
		.param("source", 0)
		.param("destination", 3)
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(703))
		.statusCode(400);
	}
	
	@Test
	public void roudTripFormatTest() {
		given()
		.param("profile", "driving-car")
		.param("locations", "8.5,48.7|8.6,49.1")
		.param("source", "any")
		.param("destination", "any")
		.param("roundtrip", "trues")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(702))
		.statusCode(400);
	}
	
	@Test
	public void metricFormatTest() {
		given()
		.param("profile", "driving-car")
		.param("locations", "8.5,48.7|8.6,49.1")
		.param("source", "any")
		.param("destination", "any")
		.param("roundtrip", "true")
		.param("metric", "distance_")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(703))
		.statusCode(400);
	}
	
	@Test
	public void unitsFormatTest() {
		given()
		.param("profile", "driving-car")
		.param("locations", "8.5,48.7|8.6,49.1")
		.param("source", "any")
		.param("destination", "any")
		.param("roundtrip", "true")
		.param("metric", "distance")
		.param("units", "kml")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(703))
		.statusCode(400);
	}
}

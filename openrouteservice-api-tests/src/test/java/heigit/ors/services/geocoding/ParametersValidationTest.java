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
package heigit.ors.services.geocoding;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

import heigit.ors.services.common.EndPointAnnotation;
import heigit.ors.services.common.ServiceTest;

@EndPointAnnotation(name="geocode")
public class ParametersValidationTest extends ServiceTest {

	public ParametersValidationTest() {
	}

	@Test
	public void postRequestTest() {
		given()
		.param("query", "Heidelberg")
		.when()
		.post(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(199))
		.statusCode(405);
	}
	
	@Test
	public void mandatoryParametersTest() {
		given()
		.param("query2", "Heidelberg")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(101))
		.statusCode(400);
	}
	
	@Test
	public void locationWrongValueTest() {
		given()
		.param("location", "8.67678")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(102))
		.statusCode(400);
		
		given()
		.param("location", "8.67678k,49.662")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(103))
		.statusCode(400);
	}

	@Test
	public void limitWrongValueTest() {
		given()
		.param("query", "Heidelberg")
		.param("limit", "10b")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(103))
		.statusCode(400);
	}
	
	@Test
	public void limitOutOfRangeValueTest() {
		given()
		.param("query", "Heidelberg")
		.param("limit", "1000")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(104))
		.statusCode(400);
	}
	
	@Test
	public void boundaryTypeUnknownValuesTest() {
		given()
		.param("query", "Heidelberg")
		.param("limit", "10")
		.param("boundary_type", "rectt")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(103))
		.statusCode(400);
		
		given()
		.param("query", "Heidelberg")
		.param("limit", "10")
		.param("boundary_type", "rect")
		.param("rectg", "4,5,4.5,6")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(101))
		.statusCode(400);
		
		given()
		.param("query", "Heidelberg")
		.param("limit", "10")
		.param("boundary_type", "circle")
		.param("circlt", "4,5,4.5")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(101))
		.statusCode(400);
	}
	
	@Test
	public void boundaryTypeWrongRectValueTest() {
		given()
		.param("query", "Heidelberg")
		.param("limit", "10")
		.param("boundary_type", "rect")
		.param("rect", "8.45,12.5,8.47,12.6hhhh")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(102))
		.statusCode(400);
	}
	
	@Test
	public void boundaryTypeWrongCircleValueTest() {
		given()
		.param("query", "Heidelberg")
		.param("limit", "10")
		.param("boundary_type", "circle")
		.param("circle", "8.45,12.5,8.47,12.6")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(103))
		.statusCode(400);
		
		given()
		.param("query", "Heidelberg")
		.param("limit", "10")
		.param("boundary_type", "circle")
		.param("circle", "b8.45,12.5,8.47")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(102))
		.statusCode(400);
	}
	
	@Test
	public void addressNoValidParametersTest() {
		given()
		.param("query", "{}")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(103))
		.statusCode(400);
		
		given()
		.param("query", "{\"address2\":\"Berliner Straße\"}")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(103))
		.statusCode(400);		
	}
}

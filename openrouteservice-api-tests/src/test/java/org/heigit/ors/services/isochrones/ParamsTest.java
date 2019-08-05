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
package heigit.ors.services.isochrones;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import heigit.ors.services.common.EndPointAnnotation;
import heigit.ors.services.common.ServiceTest;

@EndPointAnnotation(name = "isochrones")
public class ParamsTest extends ServiceTest {

	public ParamsTest() {

		addParameter("location", "8.684177,49.423034");
		addParameter("locations", "8.684177,49.423034|8.684177,49.421034|8.684177,49.421034");
		addParameter("preference", "fastest");
		addParameter("profile", "cycling-regular");

	}

	@Test
	public void testObligatoryParams() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "1800")
				.when()
				.get(getEndPointName())
				.then()
				.body("any { it.key == 'type' }", is(true))
				.body("any { it.key == 'features' }", is(true))
				.statusCode(200);
	}

	@Test
	public void testNotEnoughParams() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.when()
				.get(getEndPointName())
				.then()
				.body("error.code", is(IsochronesErrorCodes.MISSING_PARAMETER))
				.statusCode(400);
	}

	@Test
	public void testParamSpelling() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("rangeee", "1800")
				.when()
				.get(getEndPointName())
				.then()
				.body("error.code", is(IsochronesErrorCodes.MISSING_PARAMETER))
				.statusCode(400);
	}

	@Test
	public void testRangeInput() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "1800sdf")
				.when()
				.get(getEndPointName())
				.then()
				.body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT))
				.statusCode(400);
	}

	@Test
	public void testWrongLocationType() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "1800")
				.param("range_type", "time")
				.param("interval", "900")
				.param("location_type", "start123123")
				.when()
				.get(getEndPointName())
				.then()
				.body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}

	// too many intervals
	@Test
	public void testTooManyIntervals() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "1800")
				.param("range_type", "time")
				.param("interval", "100")
				.when()
				.get(getEndPointName())
				.then()
				.body("error.code", is(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM))
				.statusCode(400);
	}

	// too many locations
	@Test
	public void testTooManyLocations() {

		given()
				.param("locations", getParameter("locations"))
				.param("profile", getParameter("profile"))
				.param("range", "1800")
				.param("range_type", "time")
				.param("interval", "100")
				.when()
				.get(getEndPointName())
				.then()
				.body("error.code", is(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM))
				.statusCode(400);
	}

	// unknown units
	@Test
	public void testUnknownUnits() {

		given()
				.param("locations", getParameter("locations"))
				.param("profile", getParameter("profile"))
				.param("range", "1800")
				.param("range_type", "distance")
				.param("units", "mfff")
				.param("interval", "100")
				.when()
				.get(getEndPointName())
				.then()
				.body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}

	@Test
	public void testDestination() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "1800")
				.param("range_type", "time")
				.param("interval", "200")
				.param("location_type", "start")
				.when()
				.get(getEndPointName())
				.then()
				.statusCode(200);
	}

	@Test
	public void testStart() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "1800")
				.param("range_type", "time")
				.param("interval", "200")
				.param("location_type", "destination")
				.when()
				.get(getEndPointName())
				.then()
				.statusCode(200);
	}

	// km
	@Test
	public void testRangetypeUnitsKm() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "1800")
				.param("range_type", "distance")
				.param("interval", "200")
				.param("units", "km")
				.param("location_type", "start")
				.log()
				.all()
				.when()
				.get(getEndPointName())
				.then()
				.statusCode(200);
	}

	// m
	@Test
	public void testRangetypeUnitsM() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "1800")
				.param("range_type", "distance")
				.param("interval", "200")
				.param("units", "m")
				.param("location_type", "start")
				.log()
				.all()
				.when()
				.get(getEndPointName())
				.then()
				.statusCode(200);
	}

	// mi
	@Test
	public void testRangetypeUnitsMi() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "1800")
				.param("range_type", "distance")
				.param("interval", "200")
				.param("units", "mi")
				.param("location_type", "start")
				.log()
				.all()
				.when()
				.get(getEndPointName())
				.then()
				.statusCode(200);
	}

	@Test
	public void testRanges() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "600,400,300")
				.param("range_type", "time")
				.param("interval", "200")
				.param("location_type", "destination")
				.when()
				.get(getEndPointName())
				.then()
				.body("features[0].properties.value", is(300))
				.body("features[1].properties.value", is(400))
				.body("features[2].properties.value", is(600))
				.statusCode(200);
	}

	@Test
	public void testRangesUserUnits() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "1")
				.param("range_type", "distance")
				.param("units", "km")
				.param("location_type", "destination")
				.when()
				.get(getEndPointName())
				.then()
				.body("info.query.ranges", is("1.0"))
				.statusCode(200);
	}

	@Test
	public void testRangeRestrictionTime() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "23700")
				.param("range_type", "time")
				.when()
				.get(getEndPointName())
				.then()
				.statusCode(400)
				.body("error.code", is(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM));
	}

	@Test
	public void testRangeRestrictionDistance() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "1100000")
				.param("range_type", "distance")
				.when()
				.get(getEndPointName())
				.then()
				.statusCode(400)
				.body("error.code", is(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM));
	}

	@Test
	public void testTravellers_POST() {
		JSONObject json = new JSONObject();
		json.put("travellers1", new JSONArray());
		
		// check incorrect name 'travellers'
		given()
				.body(json.toString())
				.when()
				.post(getEndPointName())
				.then()
				.body("error.code", is(IsochronesErrorCodes.MISSING_PARAMETER))
				.statusCode(400);
	}
	
	@Test
	public void testProfile_POST() {
		JSONObject json = new JSONObject();
		JSONArray jTravellers = new JSONArray();
		json.put("travellers", jTravellers);
		JSONObject jTraveller = new JSONObject();
		jTraveller.put("profile", "driving-car2");
		jTravellers.put(jTraveller);
		
		given()
			.body(json.toString())
			.when()
			.post(getEndPointName())
			.then()
			.body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE))
			.statusCode(400);
	}
	
	@Test
	public void testLocation_POST() {
		JSONObject json = new JSONObject();
		JSONArray jTravellers = new JSONArray();
		json.put("travellers", jTravellers);
		JSONObject jTraveller = new JSONObject();
		jTraveller.put("profile", "driving-car");
		jTravellers.put(jTraveller);
		jTraveller.put("location", new JSONArray().put(8.7).put("18.7j"));
		
		given()
			.body(json.toString())
			.when()
			.post(getEndPointName())
			.then()
			.body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT))
			.statusCode(400);
	}
	
	@Test
	public void testLocationType_POST() {
		JSONObject json = new JSONObject();
		JSONArray jTravellers = new JSONArray();
		json.put("travellers", jTravellers);
		JSONObject jTraveller = new JSONObject();
		jTraveller.put("profile", "driving-car");
		jTravellers.put(jTraveller);
		jTraveller.put("location", new JSONArray().put(8.7).put("18.7"));
		jTraveller.put("location_type", "start1");
		
		given()
			.body(json.toString())
			.when()
			.post(getEndPointName())
			.then()
			.body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE))
			.statusCode(400);
	}
	
	@Test
	public void testRange_POST() {
		JSONObject json = new JSONObject();
		JSONArray jTravellers = new JSONArray();
		json.put("travellers", jTravellers);
		JSONObject jTraveller = new JSONObject();
		jTraveller.put("profile", "driving-car");
		jTravellers.put(jTraveller);
		jTraveller.put("location", new JSONArray().put(8.7).put("18.7"));
		jTraveller.put("location_type", "start");
		jTraveller.put("range", new JSONArray().put(120).put("200l"));
		
		given()
			.body(json.toString())
			.when()
			.post(getEndPointName())
			.then()
			.body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT))
			.statusCode(400);
	}
	
	@Test
	public void testRangeType_POST() {
		JSONObject json = new JSONObject();
		JSONArray jTravellers = new JSONArray();
		json.put("travellers", jTravellers);
		JSONObject jTraveller = new JSONObject();
		jTraveller.put("profile", "driving-car");
		jTravellers.put(jTraveller);
		jTraveller.put("location", new JSONArray().put(8.7).put("18.7"));
		jTraveller.put("location_type", "start");
		jTraveller.put("range", new JSONArray().put(120).put(200));
		jTraveller.put("range_type", "timee");
		
		given()
			.body(json.toString())
			.when()
			.post(getEndPointName())
			.then()
			.body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE))
			.statusCode(400);
	}
	
	@Test
	public void testUnits_POST() {
		JSONObject json = new JSONObject();
		JSONArray jTravellers = new JSONArray();
		json.put("travellers", jTravellers);
		JSONObject jTraveller = new JSONObject();
		jTraveller.put("profile", "driving-car");
		jTravellers.put(jTraveller);
		jTraveller.put("location", new JSONArray().put(8.7).put("18.7"));
		jTraveller.put("location_type", "start");
		jTraveller.put("range", new JSONArray().put(120).put(200));
		jTraveller.put("range_type", "time");
		
		json.put("units", "mm");
		
		given()
			.body(json.toString())
			.when()
			.post(getEndPointName())
			.then()
			.body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE))
			.statusCode(400);
	}

	@Test
	public void testAttributes_POST() {
		JSONObject json = new JSONObject();
		JSONArray jTravellers = new JSONArray();
		json.put("travellers", jTravellers);
		JSONObject jTraveller = new JSONObject();
		jTraveller.put("profile", "driving-car");
		jTravellers.put(jTraveller);
		jTraveller.put("location", new JSONArray().put(8.7).put("18.7"));
		jTraveller.put("location_type", "start");
		jTraveller.put("range", new JSONArray().put(120).put(200));
		jTraveller.put("range_type", "time");
		
		json.put("units", "m");
		json.put("attributes", "area|reachfactorrr");
		
		given()
			.body(json.toString())
			.when()
			.post(getEndPointName())
			.then()
			.body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE))
			.statusCode(400);
	}

	@Test
	public void testAttributes() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "400")
				.param("range_type", "time")
				.param("attributes", "area|reachfactor")
				.when()
				.get(getEndPointName())
				.then()
				.log()
				.all()
				.body("features[0].containsKey('properties')", is(true))
				.body("features[0].properties.containsKey('area')", is(true))
				.body("features[0].properties.containsKey('reachfactor')", is(true))
				.statusCode(200);
	}

	@Test
	public void testWrongAttributes() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "400")
				.param("range_type", "time")
				.param("attributes", "area|reachfactorrr")
				.when()
				.get(getEndPointName())
				.then()
				.log()
				.all()
				.statusCode(400)
				.body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE));
	}

	@Test
	public void testSmoothingFactor() {
		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "2000")
				.param("range_type", "distance")
				.param("smoothing", "50")
				.when()
				.get(getEndPointName())
				.then()
				.log()
				.all()
				.statusCode(200);
	}

	@Test
	public void testSmoothingInvalidValue() {
		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "2000")
				.param("range_type", "distance")
				.param("smoothing", "ten")
				.when()
				.get(getEndPointName())
				.then()
				.log()
				.all()
				.statusCode(400)
				.body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE));

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "2000")
				.param("range_type", "distance")
				.param("smoothing", "101")
				.when()
				.get(getEndPointName())
				.then()
				.log()
				.all()
				.statusCode(400)
				.body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE));

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "2000")
				.param("range_type", "distance")
				.param("smoothing", "-1")
				.when()
				.get(getEndPointName())
				.then()
				.log()
				.all()
				.statusCode(400)
				.body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE));
	}

}

package heigit.ors.services.isochrones;

import static io.restassured.RestAssured.*;
//import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;
//import java.util.Arrays;
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
				.body("error.code", is(301))
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
				.body("error.code", is(301))
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
				.body("error.code", is(302))
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
				.body("error.code", is(303))
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
				.body("error.code", is(304))
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
				.body("error.code", is(304))
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
				.body("error.code", is(303))
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
	public void testRangeRestrictionTime() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "3700")
				.param("range_type", "time")
				.when()
				.get(getEndPointName())
				.then()
				.statusCode(400)
				.body("error.code", is(304));
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
				.body("error.code", is(304));
	}

	@Test
	public void testPostMethod() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "11000")
				.param("range_type", "distance")
				.when()
				.post(getEndPointName())
				.then()
				.statusCode(500);
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
				.body("error.code", is(303));
	}

}

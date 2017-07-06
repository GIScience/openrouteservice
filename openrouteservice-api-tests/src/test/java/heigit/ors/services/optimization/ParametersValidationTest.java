package heigit.ors.services.optimization;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import org.json.JSONArray;
import org.json.JSONObject;
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
}

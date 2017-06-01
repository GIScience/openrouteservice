package heigit.ors.services.locations;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

import heigit.ors.services.common.EndPointAnnotation;
import heigit.ors.services.common.ServiceTest;

@EndPointAnnotation(name="locations")
public class ParametersValidationTest extends ServiceTest {

	public ParametersValidationTest() {
	}

	@Test
	public void requestUnknownValueTest() {
		given()
		.param("request", "POISd")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(403))
		.statusCode(400);
	}
	
	@Test
	public void geometryWrongValueTest() {
		given()
		.param("request", "pois")
		.param("category_ids", "518")
		.param("geometry", "{\"type2\":\"Point\", \"coordinates\": [8.676826, 49.418675]}")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(402))
		.statusCode(400);
	}
	
	@Test
	public void radiusMissingTest() {
		given()
		.param("request", "pois")
		.param("geometry", "{\"type\":\"Point\", \"coordinates\": [8.676826, 49.418675]}")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(401))
		.statusCode(400);
	}
	
	@Test
	public void categoryIdsWrongValuesTest() {
		given()
		.param("request", "pois")
		.param("category_ids", "518, 567b")
		.param("geometry", "{\"type\":\"Point\", \"coordinates\": [8.676826, 49.418675]}")
		.param("radius", "5000")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(402))
		.statusCode(400);
		
		given()
		.param("request", "pois")
		.param("category_ids", "518, 160")
		.param("geometry", "{\"type\":\"Point\", \"coordinates\": [8.676826, 49.418675]}")
		.param("radius", "5000")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(403))
		.statusCode(400);
	}
	
	@Test
	public void categoryGroupIdsWrongValuesTest() {
		given()
		.param("request", "pois")
		.param("category_group_ids", "100, 120, 930")
		.param("geometry", "{\"type\":\"Point\", \"coordinates\": [8.676826, 49.418675]}")
		.param("radius", "5000")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(403))
		.statusCode(400);
	}
	
	@Test
	public void pointSearchWithoutCategoryFilterTest() {
		given()
		.param("request", "pois")
		.param("geometry", "{\"type\":\"Point\", \"coordinates\": [8.676826, 49.418675]}")
		.param("radius", "5000")
		.param("limit", "200")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(401))
		.statusCode(400);
	}
}

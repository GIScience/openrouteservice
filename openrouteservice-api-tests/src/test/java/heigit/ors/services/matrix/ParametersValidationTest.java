package heigit.ors.services.matrix;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

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
		.body("error.code", is(603))
		.statusCode(400);
	}

	@Test
	public void missingProfileFormatTest() {
		given()
		.param("profile2", "driving-car")
		.param("sources", "8.5,48.7|8.6,49.1")
		.param("destinations", "10.5,48.7")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(601))
		.statusCode(400);
	}
/*
	@Test
	public void sourcesEmptyTest() {
		given()
		.param("profile", "driving-car")
		.param("sources", "")
		.param("destinations", "10.5,48.7")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(602))
		.statusCode(400);
	}
*/	
	@Test
	public void sourcesFormatTest() {
		given()
		.param("profile", "driving-car")
		.param("sources", "8.5,48.7|8.6,49.1b")
		.param("destinations", "10.5,48.7")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(602))
		.statusCode(400);
	}
	
	@Test
	public void destinationsFormatTest() {
		given()
		.param("profile", "driving-car")
		.param("sources", "8.5,48.7|8.6,49.1b")
		.param("destinations", "10.5,48.7k|8.6,51.132")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(602))
		.statusCode(400);
	}
	
	@Test
	public void metricsUnknownValueTest() {
		given()
		.param("profile", "driving-car")
		.param("sources", "8.5,48.7|8.6,49.1")
		.param("destinations", "10.5,48.7|8.6,51.132")
		.param("metrics", "time|durationO")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(603))
		.statusCode(400);
	}
}

package heigit.ors.services.geocoding;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import java.net.URLEncoder;

import org.junit.Test;

import heigit.ors.services.common.EndPointAnnotation;
import heigit.ors.services.common.ServiceTest;
import junit.framework.Assert;

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
		String query = "";
		try {
			query = URLEncoder.encode("{}", "UTF-8");
		} catch (Exception e) {
			Assert.assertEquals(true, false);
		}
		given()
		.param("query", query)
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(103))
		.statusCode(400);
		
		try {
			query = URLEncoder.encode("{\"address2\":\"Berliner Straße\"}", "UTF-8");
		} catch (Exception e) {
			Assert.assertEquals(true, false);
		}
		given()
		.param("query", query)
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(103))
		.statusCode(400);		
	}
}

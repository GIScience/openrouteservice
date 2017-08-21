package heigit.ors.services.geocoding;

import static io.restassured.RestAssured.*;

import org.junit.Test;
import org.json.JSONArray;
import org.json.JSONObject;

import heigit.ors.services.common.EndPointAnnotation;
import heigit.ors.services.common.ServiceTest;
import io.restassured.response.Response;
import junit.framework.Assert;

@EndPointAnnotation(name="geocode")
public class ResultsValidationTest extends ServiceTest {
	public ResultsValidationTest() {
	}

	@Test
	public void queryParameterTest() {
		Response response = given()
		.param("query", "Heidelberg")
		.param("limit", "10")
		.when()
		.get(getEndPointName());
		
		Assert.assertEquals(response.getStatusCode(), 200);
        JSONObject jResponse = new JSONObject(response.body().asString());
        Assert.assertEquals(jResponse.getJSONArray("features").length(), 10);
	}
	
	@Test
	public void locationParameterTest() {
		Response response = given()
		.log()
		.all()
		.param("location", "8.67678, 49.4187")
		.param("limit", "1")
		.when()
		.get(getEndPointName());
		
		Assert.assertEquals(response.getStatusCode(), 200);
        JSONObject jResponse = new JSONObject(response.body().asString());
        Assert.assertEquals(jResponse.getJSONArray("features").length(), 1);
        JSONObject jAddress = jResponse.getJSONArray("features").getJSONObject(0);
        JSONObject jAddrProperties = jAddress.getJSONObject("properties");
        Assert.assertEquals(jAddrProperties.get("country"), "Germany");
        Assert.assertEquals(jAddrProperties.get("county"), "Karlsruhe");
        Assert.assertEquals(jAddrProperties.get("region"), "Baden-Württemberg");
        Assert.assertEquals(jAddrProperties.get("locality"), "Heidelberg");
        Assert.assertEquals(jAddrProperties.get("name"), "Ruprecht-Karls-Universität");
        Assert.assertEquals(jAddrProperties.get("distance"), 4.34);
        Assert.assertEquals(jAddrProperties.get("confidence"), 0.9);
        
        JSONArray jAddrLocation = jAddress.getJSONObject("geometry").getJSONArray("coordinates");
        Assert.assertEquals(jAddrLocation.getDouble(0), 8.676826);
        Assert.assertEquals(jAddrLocation.getDouble(1), 49.418675);
	}
	
	@Test
	public void boundaryTypeRectParameterTest() {
		Response response = given()
		.param("query", "Heidelberg")
		.param("limit", "20")
		.param("boundary_type", "rect")
		.param("rect", "8.276826,49.218675,8.976826,49.718675")
		.when()
		.get(getEndPointName());
		
		Assert.assertEquals(response.getStatusCode(), 200);
        JSONObject jResponse = new JSONObject(response.body().asString());
        Assert.assertEquals(jResponse.getJSONArray("features").length(), 10);
	}
	
	@Test
	public void boundaryTypeCircleParameterTest() {
		Response response = given()
		.param("query", "Heidelberg")
		.param("limit", "10")
		.param("boundary_type", "circle")
		.param("circle", "8.676826, 49.418675, 4")
		.when()
		.get(getEndPointName());
		
		Assert.assertEquals(response.getStatusCode(), 200);
        JSONObject jResponse = new JSONObject(response.body().asString());
        Assert.assertEquals(jResponse.getJSONArray("features").length(), 7);
	}
	
	@Test
	public void idParameterTest() {
		Response response = given()
		.param("query", "Heidelberg")
		.param("limit", "10")
		.param("id", "829723410")
		.when()
		.get(getEndPointName());
		
		Assert.assertEquals(response.getStatusCode(), 200);
        JSONObject jResponse = new JSONObject(response.body().asString());
        Assert.assertEquals(jResponse.getJSONObject("info").getJSONObject("query").get("id"), "829723410");
	}
	
	@Test
	public void addressPostCodeTest() {
		Response response = given()
		.param("query", "{\"postalcode\":\"69120\"}")
		.param("limit",  "10")
		.when()
		.get(getEndPointName());
		
		Assert.assertEquals(200, response.getStatusCode());
		JSONObject jResponse = new JSONObject(response.body().asString());
		Assert.assertTrue(jResponse.getJSONArray("features").length() > 0);

	}
	
	@Test 
	public void addressStructuredCombinedTest() {
		Response response = given()
		.param("query", "{\"locality\":\"Mannheim\",\"country\":\"Germany\"}")
		.param("limit",  "10")
		.when()
		.get(getEndPointName());
				
		Assert.assertEquals(200, response.getStatusCode());
		JSONObject jResponse = new JSONObject(response.body().asString());
		Assert.assertEquals(1, jResponse.getJSONArray("features").length());
	}
	
	@Test
	public void addressStructuredSingleParameterTest() {
		Response response = given()
		.param("query", "{\"locality\":\"Heidelberg\"}")
		.param("limit",  "10")
		.when()
		.get(getEndPointName());
				
		Assert.assertEquals(200, response.getStatusCode());
		JSONObject jResponse = new JSONObject(response.body().asString());
		Assert.assertTrue(jResponse.getJSONArray("features").length() > 1);
	}
}

package heigit.ors.services.geocoding;

import static io.restassured.RestAssured.*;

import java.net.URLEncoder;

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
        Assert.assertEquals(jAddrProperties.get("state"), "Baden-Württemberg");
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
		String query = "";
		try {
			query = URLEncoder.encode("{\"postalcode\":\"69120\"}", "UTF-8");
		} catch (Exception e) {
			Assert.assertEquals(true, false);
		}
		Response response = given()
		.param("query", query)
		.param("limit",  "10")
		.when()
		.get(getEndPointName());
		
		Assert.assertEquals(200, response.getStatusCode());
		JSONObject jResponse = new JSONObject(response.body().asString());
		Assert.assertTrue(jResponse.getJSONArray("features").length() > 0);

	}
	
	@Test 
	public void addressStructuredCombinedTest() {
		String query = "";
		try {
			query = URLEncoder.encode("{\"locality\":\"Mannheim\",\"country\":\"Germany\"}", "UTF-8");
		} catch (Exception e) {
			Assert.assertEquals(true, false);
		}
		Response response = given()
		.param("query", query)
		.param("limit",  "10")
		.when()
		.get(getEndPointName());
				
		Assert.assertEquals(200, response.getStatusCode());
		JSONObject jResponse = new JSONObject(response.body().asString());
		Assert.assertEquals(jResponse.getJSONArray("features").length(), 1);
	}
	
	@Test
	public void addressStructuredSingleParameterTest() {
		String query = "";
		try {
			query = URLEncoder.encode("{\"locality\":\"Heidelberg\"}", "UTF-8");
		} catch (Exception e) {
			Assert.assertEquals(true, false);
		}
		Response response = given()
		.param("query", query)
		.param("limit",  "10")
		.when()
		.get(getEndPointName());
				
		Assert.assertEquals(200, response.getStatusCode());
		JSONObject jResponse = new JSONObject(response.body().asString());
		Assert.assertTrue(jResponse.getJSONArray("features").length() > 1);
	}
}

package heigit.ors.services.locations;

import static io.restassured.RestAssured.*;

import org.junit.Test;

import static org.hamcrest.Matchers.is;

import org.json.JSONArray;
import org.json.JSONObject;

import heigit.ors.services.common.EndPointAnnotation;
import heigit.ors.services.common.ServiceTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import junit.framework.Assert;

@EndPointAnnotation(name="locations")
public class ResultsValidationTest extends ServiceTest {
	public ResultsValidationTest() {
	}

	public RequestSpecification createGetRequest(String geometry, String id)
	{
		return given()
				.param("geometry", geometry)
				.param("id", id)
				.when();
	}

	@Test
	public void requestCategoryListTest() {
		Response response = given()
				.param("request", "category_list")
				.when()
				.get(getEndPointName());

		Assert.assertEquals(response.getStatusCode(), 200);
		JSONObject jResponse = new JSONObject(response.body().asString());

		JSONObject jCategories = jResponse.getJSONObject("categories");
		String[] categories = new String[] {"education", "historic", "natural", "financial", "leisure_and_entertainment", "sustenance", "tourism", "transport", "arts_and_culture", "healthcare", "service", "accomodation", "shops", "animals", "facilities", "public_places"};
		for(String cat : categories)
			Assert.assertNotNull(jCategories.get(cat));
	}
	
	@Test
	public void pointSearchWithoutNameFilterTest() {
		Response response = given()
		.param("request", "pois")
		.param("geometry", "{\"type\":\"Point\", \"coordinates\": [8.676826, 49.418675]}")
		.param("radius", "5000")
		.param("category_ids", "518")
		.param("limit", "200")
		.when()
		.get(getEndPointName());
		
		Assert.assertEquals(response.getStatusCode(), 200);
		JSONObject jResponse = new JSONObject(response.body().asString());
		
	    Assert.assertEquals(jResponse.getJSONArray("features").length(), 42);
	}
	
	@Test
	public void pointSearchWithNameFilterTest() {
		Response response = given()
		.param("request", "pois")
		.param("geometry", "{\"type\":\"Point\", \"coordinates\": [8.676826, 49.418675]}")
		.param("radius", "5000")
		.param("name", "Rewe*")
		.param("category_ids", "518")
		.param("limit", "200")
		.when()
		.get(getEndPointName());
		
		Assert.assertEquals(response.getStatusCode(), 200);
		JSONObject jResponse = new JSONObject(response.body().asString());
		
	    Assert.assertEquals(jResponse.getJSONArray("features").length(), 6);
	}
	
	@Test
	public void idParameterTest() {
		Response response = given()
				.param("request", "category_list")
				.param("id", "34629723410")
				.when()
				.get(getEndPointName());

		Assert.assertEquals(response.getStatusCode(), 200);
		JSONObject jResponse = new JSONObject(response.body().asString());
		Assert.assertEquals(jResponse.getJSONObject("info").getJSONObject("query").get("id"), "34629723410");
	}
}

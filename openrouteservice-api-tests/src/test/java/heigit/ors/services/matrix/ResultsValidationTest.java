package heigit.ors.services.matrix;

import static io.restassured.RestAssured.*;

import org.junit.Test;

import org.json.JSONArray;
import org.json.JSONObject;

import heigit.ors.services.common.EndPointAnnotation;
import heigit.ors.services.common.ServiceTest;
import io.restassured.response.Response;
import junit.framework.Assert;

@EndPointAnnotation(name="matrix")
public class ResultsValidationTest extends ServiceTest {
	public ResultsValidationTest() {
		addParameter("locations", "8.690733,49.387283|8.692771,49.385118|8.686409,49.426272");
		addParameter("sources1", "0,1");
		addParameter("destinations1", "2");
		addParameter("destinations2", "2,3");
	}

	@Test
	public void emptySourcesAndDestinationsTest() {
		Response response = given()
				.param("locations", getParameter("locations"))
				.param("metrics", "distance")
				.param("profile", "driving-car")
				.when()
				.get(getEndPointName());

		Assert.assertEquals(response.getStatusCode(), 200);
		JSONObject jResponse = new JSONObject(response.body().asString());
		checkTableDimensions(jResponse, "distances", 3, 3);
	}
	/*
	@Test
	public void nonExistingLocationEntryTest() {
		Response response = given()
				.param("locations", "8.690733,49.387283|8.686409,49.426272|18.686409,49.426272")
				.param("sources", "0")
				.param("destinations", "1,2")
				.param("metrics", "distance")
				.param("profile", "driving-car") 
				.when()
				.get(getEndPointName());

		Assert.assertEquals(response.getStatusCode(), 200);
		JSONObject jResponse = new JSONObject(response.body().asString());
		checkTableDimensions(jResponse, "distances", 1, 2);
		JSONArray jDistances = jResponse.getJSONArray("distances").getJSONArray(0);
		Assert.assertEquals(jDistances.get(1), JSONObject.NULL);
	}*/
/*	 
	@Test
	public void emptyLocationEntryTest() {
		Response response = given()
				.param("locations", "8.690733,49.387283|8.686409,49.426272")
				.param("sources", "0")
				.param("destinations", "1")
				.param("metrics", "duration|distance")
				.param("profile", "driving-car") 
				.when()
				.get(getEndPointName());

		Assert.assertEquals(response.getStatusCode(), 200);
		JSONObject jResponse = new JSONObject(response.body().asString());
		checkTableDimensions(jResponse, "distances", 2, 1);
	}
	*/
	
	@Test
	public void distanceTableTest() {
		Response response = given()
				.param("locations", getParameter("locations"))
				.param("sources", getParameter("sources1"))
				.param("destinations", getParameter("destinations1"))
				.param("metrics", "distance")
				.param("profile", "driving-car")
				.when()
				.get(getEndPointName());

		Assert.assertEquals(response.getStatusCode(), 200);
		JSONObject jResponse = new JSONObject(response.body().asString());
		checkTableDimensions(jResponse, "distances", 2, 1);
	}
	
	@Test
	public void durationTableTest() {
		Response response = given()
				.param("locations", getParameter("locations"))
				.param("sources", getParameter("sources1"))
				.param("destinations", getParameter("destinations1"))
				.param("metrics", "duration")
				.param("profile", "driving-car")
				.when()
				.get(getEndPointName());

		Assert.assertEquals(response.getStatusCode(), 200);
		JSONObject jResponse = new JSONObject(response.body().asString());
		checkTableDimensions(jResponse, "durations", 2, 1);
	}
	
	@Test
	public void durationAndDistanceTablesTest() {
		Response response = given()
				.param("locations", getParameter("locations"))
				.param("sources", getParameter("sources1"))
				.param("destinations", getParameter("destinations1"))
				.param("metrics", "distance|duration")
				.param("profile", "driving-car")
				.when()
				.log().all()
				.get(getEndPointName());

		Assert.assertEquals(response.getStatusCode(), 200);
		JSONObject jResponse = new JSONObject(response.body().asString());
		checkTableDimensions(jResponse, "durations", 2, 1);
		checkTableDimensions(jResponse, "distances", 2, 1);
	}
	
	@Test
	public void idParameterTest() {
		Response response = given()
				.param("locations", getParameter("locations"))
				.param("sources", getParameter("sources1"))
				.param("destinations", getParameter("destinations1"))
				.param("profile", "driving-car")
				.param("id", "34629723410")
				.when()
				.get(getEndPointName());

		Assert.assertEquals(response.getStatusCode(), 200);
		JSONObject jResponse = new JSONObject(response.body().asString());
		Assert.assertEquals(jResponse.getJSONObject("info").getJSONObject("query").get("id"), "34629723410");
	}
	
	@Test
	public void resolveNamesParameterTest() {
		Response response = given()
				.param("locations", getParameter("locations"))
				.param("sources", getParameter("sources1"))
				.param("destinations", getParameter("destinations1"))
				.param("profile", "driving-car")
				.param("resolve_locations", "true")
				.when()
				.get(getEndPointName());

		Assert.assertEquals(response.getStatusCode(), 200);
		JSONObject jResponse = new JSONObject(response.body().asString());
		Assert.assertEquals(true, jResponse.getJSONArray("sources").getJSONObject(0).has("name"));
	}

	private void checkTableDimensions(JSONObject json, String tableName, int rows, int columns)
	{
		Assert.assertEquals(true, json.has(tableName));

		JSONArray jTable = json.getJSONArray(tableName);
		Assert.assertEquals(jTable.length(), rows);
		Assert.assertEquals(jTable.getJSONArray(0).length(), columns);
	}
}

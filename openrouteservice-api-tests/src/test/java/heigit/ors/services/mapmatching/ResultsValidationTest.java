package heigit.ors.services.mapmatching;

import static io.restassured.RestAssured.*;

import org.junit.Test;
import org.json.JSONArray;
import org.json.JSONObject;

import heigit.ors.services.common.EndPointAnnotation;
import heigit.ors.services.common.ServiceTest;
import io.restassured.response.Response;
import junit.framework.Assert;

@EndPointAnnotation(name="matching")
public class ResultsValidationTest extends ServiceTest {
	public ResultsValidationTest() {
	}
}

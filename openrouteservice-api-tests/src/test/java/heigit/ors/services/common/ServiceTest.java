package heigit.ors.services.common;

import io.restassured.RestAssured;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;

public class ServiceTest {
	private final Map<String, Object> dictUrlParams;
	private final String serviceName;

	public ServiceTest(String serviceName) {
		dictUrlParams = new HashMap<String, Object>();
		this.serviceName = serviceName;
	}

	protected Object getParameter(String paramName) {
		return dictUrlParams.get(paramName);
	}

	protected void addParameter(String paramName, Object paramValue) {
		dictUrlParams.put(paramName, paramValue);
	}

	protected String getServiceName() {
		return serviceName;
	}

	@BeforeClass
	public static void setup() {
		String port = System.getProperty("server.port");
		RestAssured.port = (port == null) ? 8082 : Integer.valueOf(port);

		String basePath = System.getProperty("server.base");
		if (basePath == null) 
			basePath = "/openrouteservice-4.0.0/";
		
		RestAssured.basePath = basePath;

		String baseHost = System.getProperty("server.host");
		if (baseHost == null) 
			baseHost = "http://localhost";
		
		RestAssured.baseURI = baseHost;
	}
}
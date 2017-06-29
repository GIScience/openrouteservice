package heigit.ors.services.common;

import io.restassured.RestAssured;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;

public abstract class ServiceTest {
	private final Map<String, Object> dictUrlParams;
	private String endPointName;

	public ServiceTest() {
		dictUrlParams = new HashMap<String, Object>();
		
		Annotation[] annotations = getClass().getAnnotations();
		for(Annotation annotation : annotations){
		    if(annotation instanceof EndPointAnnotation){
		    	EndPointAnnotation epa = (EndPointAnnotation) annotation;
		        endPointName = epa.name();
		    }
		}
	}

	protected Object getParameter(String paramName) {
		return dictUrlParams.get(paramName);
	}

	protected void addParameter(String paramName, Object paramValue) {
		dictUrlParams.put(paramName, paramValue);
	}

	protected String getEndPointName() {
		return endPointName;
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
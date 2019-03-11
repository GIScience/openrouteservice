/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   	 http://www.giscience.uni-hd.de
 *   	 http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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

		String baseHost = System.getProperty("server.host");
		if (baseHost == null)
			baseHost = "http://localhost";

		RestAssured.baseURI = baseHost;

		if (RestAssured.get("/status").statusCode() != 200) {
			String basePath = System.getProperty("server.base");
			if (basePath == null) {
				basePath = "/ors/";
			}
			RestAssured.basePath = basePath;
		}

	}
}
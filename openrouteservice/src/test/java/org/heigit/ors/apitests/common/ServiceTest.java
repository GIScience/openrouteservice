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
package org.heigit.ors.apitests.common;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(InitializeGraphsOnce.class)
public abstract class ServiceTest {
	private final Map<String, Object> dictUrlParams;
	private String endPointName;
	private String version;

	@Autowired
	private TestRestTemplate testRestTemplate;

	@LocalServerPort
	private Integer port;

	@BeforeEach
	void setupRestAssured() {
		RestAssured.port = port;
		RestAssured.baseURI = testRestTemplate.getRootUri();
	}

	public ServiceTest() {
		dictUrlParams = new HashMap<>();
		
		Annotation[] annotations = getClass().getAnnotations();
		for(Annotation annotation : annotations){
		    if(annotation instanceof EndPointAnnotation){
		    	EndPointAnnotation epa = (EndPointAnnotation) annotation;
		        endPointName = epa.name();
		    }
		    if(annotation instanceof VersionAnnotation) {
		    	VersionAnnotation va = (VersionAnnotation) annotation;
		    	version = va.version();
			}
		}
	}

	protected Object getParameter(String paramName) {
		return dictUrlParams.get(paramName);
	}

	protected void addParameter(String paramName, Object paramValue) {
		dictUrlParams.put(paramName, paramValue);
	}

	protected String getEndPointPath(String altName) {
		String tmp = endPointName;
		endPointName = altName;
		String ret = getEndPointPath();
		endPointName = tmp;
		return ret;
	}

	protected String getEndPointPath() {
		String path = "";
		if(version != null && !version.isEmpty())
			path = version + "/";
		path = path + endPointName;

		return path;
	}
}
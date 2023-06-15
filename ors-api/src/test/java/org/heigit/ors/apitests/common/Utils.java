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

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {

	public synchronized static final String getORSVersion() {

		String version = null;

		try {
			String curDir = System.getProperty("user.dir");
			Path pomFile = Paths.get(Paths.get(curDir).getParent().toString(), "openrouteservice").resolve("pom.xml");
			
			try (InputStream is = Files.newInputStream(pomFile)) 
			{
				Document doc = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder().parse(is);
				doc.getDocumentElement().normalize();
				version = (String) XPathFactory.newInstance().newXPath().compile("/project/version").evaluate(doc, XPathConstants.STRING);
				if (version != null) {
					version = version.trim();
				}
			}
		} catch (Exception e) {
		}

		return version;
	}
}

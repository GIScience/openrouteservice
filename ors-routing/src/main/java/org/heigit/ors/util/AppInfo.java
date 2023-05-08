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
package org.heigit.ors.util;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Properties;

public class AppInfo {
	private static final Logger LOGGER = Logger.getLogger(AppInfo.class.getName());
	/**
	 * The value of <tt>System.getProperty("java.version")</tt>. *
	 */
	public static final String JAVA_VERSION = System.getProperty("java.version");
	/**
	 * The value of <tt>System.getProperty("os.name")</tt>. *
	 */
	public static final String OS_NAME = System.getProperty("os.name", "unknown");
	/**
	 * True iff running on Linux.
	 */
	public static final boolean LINUX = OS_NAME.startsWith("Linux");
	/**
	 * True iff running on Windows.
	 */
	public static final boolean WINDOWS = OS_NAME.startsWith("Windows");
	/**
	 * True iff running on SunOS.
	 */
	public static final boolean SUN_OS = OS_NAME.startsWith("SunOS");
	/**
	 * True iff running on Mac OS X
	 */
	public static final boolean MAC_OS_X = OS_NAME.startsWith("Mac OS X");
	public static final String OS_ARCH = System.getProperty("os.arch");
	public static final String OS_VERSION = System.getProperty("os.version");
	public static final String JAVA_VENDOR = System.getProperty("java.vendor");

	public static final String VERSION;
	public static final String BUILD_DATE;
	public static final boolean SNAPSHOT;

	static {
		String version = "0.0";
		Properties prop = new Properties();

		try (InputStream in = Thread.currentThread().getContextClassLoader().getResource("resources/version.properties").openStream()) {
			prop.load(in);
			version = prop.getProperty("version");
		} catch (Exception e) {
			LOGGER.error("Initialization ERROR: cannot read version!? " + e.getMessage());
		}

		int indexM = version.indexOf('-');
		if ("${project.version}".equals(version)) {
			VERSION = "0.0";
			SNAPSHOT = true;
			LOGGER.error("OpenRouteService Initialization WARNING: maven did not preprocess the version file! Do not use the jar for a release!");
		} else if ("0.0".equals(version)) {
			VERSION = "0.0";
			SNAPSHOT = true;
			LOGGER.error("OpenRouteService Initialization WARNING: cannot get version!?");
		} else {
			String tmp = version;
			// throw away the "-SNAPSHOT"
			if (indexM >= 0)
				tmp = version.substring(0, indexM);

			SNAPSHOT = version.toLowerCase().contains("-snapshot");
			VERSION = tmp;
		}

		String buildDate = "";
		try {
			buildDate = prop.getProperty("buildDate");
		} catch (Exception e) {
			LOGGER.error(e);
		}

		BUILD_DATE = buildDate;
	}

	private AppInfo() {}

	public static JSONObject getEngineInfo() {
		JSONObject json = new JSONObject(true);
		json.put("version", VERSION);
		json.put("build_date", BUILD_DATE);
		return json;
	}
}

/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
package org.heigit.ors.logging;

import org.heigit.ors.config.AppConfig;

public class LoggingSettings {
	private static boolean enabled;
	private static String levelFile = "DEFAULT_LOGGING.properties";
	private static String location = "logs/output/ors.log";
	private static boolean stdout = false;

	public static final String PARAM_LOGGING = "logging";

	static {
		String value = AppConfig.getGlobal().getParameter(PARAM_LOGGING, "level_file");
		if (value != null)
			levelFile = value;
		value = AppConfig.getGlobal().getParameter(PARAM_LOGGING, "enabled");
		if (value != null)
			enabled = Boolean.parseBoolean(value);	
		value = AppConfig.getGlobal().getParameter(PARAM_LOGGING, "location");
		if (value != null)
			location = value;
		value = AppConfig.getGlobal().getParameter(PARAM_LOGGING, "stdout");
		if (value != null)
			stdout = Boolean.parseBoolean(value);
	}

	private LoggingSettings() {}

	public static boolean getEnabled() {
		return enabled;
	}
	
	public static String getLevelFile() {
		return levelFile;
	}

	public static String getLocation() {
		return location;
	}
	
	public static boolean getStdOut() {
		return stdout;
	}
}

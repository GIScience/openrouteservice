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
package heigit.ors.logging;

import heigit.ors.config.AppConfig;

public class LoggingSettings {
	private static boolean enabled;
	private static String levelFile = "DEFAULT_LOGGING.properties";
	private static String location = "logs/output/ors.log";
	private static boolean stdout = false;
	
	static 
	{
		String value = AppConfig.Global().getParameter("logging", "level_file");
		if (value != null)
			levelFile = value;
		value = AppConfig.Global().getParameter("logging", "enabled");
		if (value != null)
			enabled = Boolean.parseBoolean(value);	
		value = AppConfig.Global().getParameter("logging", "location");
		if (value != null)
			location = value;
		value = AppConfig.Global().getParameter("logging", "stdout");
		if (value != null)
			stdout = Boolean.parseBoolean(value);
	}
	
	public static Boolean getEnabled() {
		return enabled;
	}
	
	public static String getLevelFile() {
		return levelFile;
	}

	public static String getLocation() {
		return location;
	}
	
	public static Boolean getStdOut() {
		return stdout;
	}
}

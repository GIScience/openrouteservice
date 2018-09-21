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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

public class LoggingUtility {

	public static void init(String logsPath) throws Exception
	{
		if (LoggingSettings.getEnabled())
		{
			String location = LoggingSettings.getLevelFile();

			if (location != null)
			{
				File configFile = Paths.get(logsPath, location).toFile();
				if (configFile.exists())
				{
					File outputPath  = Paths.get(LoggingSettings.getLocation()).toFile();
					
					if (!outputPath.isAbsolute())
					   outputPath = Paths.get(logsPath, LoggingSettings.getLocation()).toFile();
					
					initInternal(configFile, outputPath.toString(), LoggingSettings.getStdOut());
				}
				else
					throw new Exception("Logging config file does not exist.");
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private static void initInternal(File configFile, String outputPath, boolean stdOut) throws IOException
	{
		List<Appender> appenders = new ArrayList<Appender>();

		// Retrieve all existing appenders
		Enumeration apps = LogManager.getRootLogger().getAllAppenders();
		while(apps.hasMoreElements()) {
			Appender appender = (Appender)apps.nextElement();
			if (!(appender instanceof ConsoleAppender || appender instanceof FileAppender)) 
				appenders.add( appender );
		}

		Properties lprops = new Properties();
		lprops.load(new FileInputStream(configFile));
		LogManager.resetConfiguration();
		
		if (lprops.getProperty("log4j.appender.orslogfile.File") == null)
			lprops.put("log4j.appender.orslogfile.File", outputPath);
		
		PropertyConfigurator.configure(lprops);

		if (!stdOut) {
            apps = org.apache.log4j.Logger.getRootLogger().getAllAppenders();
            while (apps.hasMoreElements()) {
            	Appender appender = (Appender)apps.nextElement();
                if (appender instanceof org.apache.log4j.ConsoleAppender) {
                    org.apache.log4j.Logger.getRootLogger().removeAppender(appender);
                }
            }
		} 

		for (Appender appender : appenders ) 
			LogManager.getRootLogger().addAppender( appender );
	}
}

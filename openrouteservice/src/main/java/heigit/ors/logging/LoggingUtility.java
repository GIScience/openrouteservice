/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
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
		FileInputStream in = new FileInputStream(configFile);
		lprops.load(in);
		in.close();
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

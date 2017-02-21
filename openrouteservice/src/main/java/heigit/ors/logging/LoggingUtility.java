/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
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

	private static void initInternal(File configFile, String outputPath, boolean stdOut) throws IOException
	{
		List<Appender> appenders = new ArrayList<Appender>();

		// Retrieve all existing appenders
		Enumeration<Appender>  apps = LogManager.getRootLogger().getAllAppenders();
		while(apps.hasMoreElements()) {
			Appender appender = apps.nextElement();
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
            apps = (Enumeration<Appender>)org.apache.log4j.Logger.getRootLogger().getAllAppenders();
            while (apps.hasMoreElements()) {
            	Appender appender = apps.nextElement();
                if (appender instanceof org.apache.log4j.ConsoleAppender) {
                    org.apache.log4j.Logger.getRootLogger().removeAppender(appender);
                }
            }
		} 

		for (Appender appender : appenders ) 
			LogManager.getRootLogger().addAppender( appender );
	}
}

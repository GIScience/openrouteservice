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
package heigit.ors.servlet.listeners;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import heigit.ors.logging.LoggingUtility;

public class LoggingStartupContextListener implements ServletContextListener
{
	private static Logger LOGGER;    

	public void contextInitialized(ServletContextEvent contextEvent)
	{
		 final ServletContext context = contextEvent.getServletContext();
		 
		 try
		 {
			 LoggingUtility.init(context.getRealPath("/WEB-INF/logs"));
		 }
		 catch(Exception ex)
		 {
			 getLogger().error("Unable to initialize logging system.");
		 }
	}

	public void contextDestroyed(ServletContextEvent contextEvent) {
		
	}
	
	private Logger getLogger() 
	{
        if(LOGGER == null) 
            LOGGER = LoggerFactory.getLogger("heigit.ors.logging");
        
        return LOGGER;
    }
} 
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

import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import heigit.ors.routing.RoutingProfileManager;
import heigit.ors.locations.providers.LocationsDataProviderFactory;

public class ORSInitContextListener implements ServletContextListener
{
	private static final Logger LOGGER = LoggerFactory.getLogger("heigit.ors.logging");    

	public void contextInitialized(ServletContextEvent contextEvent) 
	{
		try {
			RoutingProfileManager.getInstance().toString();
		} catch (IOException e) {
			LOGGER.warn("Unable to initialize ORS.");
			e.printStackTrace();
		}
	}

	public void contextDestroyed(ServletContextEvent contextEvent) {
		try {
			LOGGER.info("Start shutting down ORS and releasing resources.");

			RoutingProfileManager.getInstance().destroy();

			LocationsDataProviderFactory.releaseProviders();
			
			LogFactory.release(Thread.currentThread().getContextClassLoader());

			try {
				System.gc();
				System.runFinalization();
				System.gc();
				System.runFinalization();
			} catch(Throwable t) {
				LOGGER.error("Failed to perform finalization.");
				t.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
} 
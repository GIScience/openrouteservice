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
package heigit.ors.servlet.listeners;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import heigit.ors.logging.LoggingUtility;

//@WebListener
public class LoggingStartupContextListener implements ServletContextListener
{
	private static Logger LOGGER;    

	public void contextInitialized(ServletContextEvent contextEvent)
	{
		 final ServletContext context = contextEvent.getServletContext();
		 
		 try
		 {
			 LoggingUtility.init();
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
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
package org.heigit.ors.servlet.listeners;

import org.heigit.ors.logging.LoggingUtility;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class LoggingStartupContextListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		 contextEvent.getServletContext();
		 try {
			 LoggingUtility.init();
		 } catch(Exception ex) {
			 LoggerFactory.getLogger("org.heigit.ors.logging").error("Unable to initialize logging system.");
		 }
	}

	@Override
	public void contextDestroyed(ServletContextEvent contextEvent) {
		// DO NOTHING
	}
}
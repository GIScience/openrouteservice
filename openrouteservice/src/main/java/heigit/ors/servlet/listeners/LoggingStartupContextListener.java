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
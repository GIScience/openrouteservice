/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://giscience.uni-hd.de
 *   http://heigit.org
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

import heigit.ors.isochrones.statistics.StatisticsProviderFactory;
import heigit.ors.routing.RoutingProfileManager;
import heigit.ors.routing.RoutingProfileManagerStatus;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ORSInitContextListener implements ServletContextListener
{
	private static final Logger LOGGER = LoggerFactory.getLogger("heigit.ors.logging");    

	public void contextInitialized(ServletContextEvent contextEvent) 
	{
		Runnable runnable = () -> {
		    try {
		    	RoutingProfileManager.getInstance().toString();
		    }
		    catch (Exception e) {
		    	LOGGER.warn("Unable to initialize ORS.");
				e.printStackTrace();
		    } 
		};

		Thread thread = new Thread(runnable);
		thread.setName("ORS-Init");
		thread.start();
	}

	public void contextDestroyed(ServletContextEvent contextEvent) {
		try {
			LOGGER.info("Start shutting down ORS and releasing resources.");

			if (RoutingProfileManagerStatus.isReady())
				RoutingProfileManager.getInstance().destroy();

			StatisticsProviderFactory.releaseProviders();
			
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

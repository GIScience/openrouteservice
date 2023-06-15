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
package org.heigit.ors.logging;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.springframework.core.io.ClassPathResource;


public class LoggingUtility {
	private LoggingUtility() {}

	public static void init() throws Exception {
		if (LoggingSettings.getEnabled()) {
			String settingsFileName = LoggingSettings.getLevelFile();

			if (settingsFileName != null) {
				ClassPathResource rs = new ClassPathResource("logs/" + settingsFileName);
				ConfigurationSource source = new ConfigurationSource(rs.getInputStream());

				ConfigurationBuilder<BuiltConfiguration> conf = ConfigurationBuilderFactory.newConfigurationBuilder();
				conf.setConfigurationSource(source);
				conf.addProperty("filename", LoggingSettings.getLocation() + "/ors-logs.log");

				Configurator.initialize(conf.build());
				Logger.getLogger(LoggingUtility.class.getName()).info(String.format("Logging configuration loaded from %s, logging to file %s", settingsFileName, LoggingSettings.getLocation() + "/ors-logs.log"));
			}
		}
	}
}

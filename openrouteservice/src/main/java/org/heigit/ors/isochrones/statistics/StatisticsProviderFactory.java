/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   	http://www.giscience.uni-hd.de
 *   	http://www.heigit.org
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
package org.heigit.ors.isochrones.statistics;

import org.apache.log4j.Logger;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.isochrones.IsochronesErrorCodes;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class StatisticsProviderFactory {
	
	private static final Logger LOGGER = Logger.getLogger(StatisticsProviderFactory.class.getName());
	
	private static final Map<String, StatisticsProviderItem> providers;
    private static final Object lockObj;

	static {
    	lockObj = new Object();
		providers = new HashMap<>();
		synchronized(lockObj) {
			ServiceLoader<StatisticsProvider> loader = ServiceLoader.load(StatisticsProvider.class);
			for (StatisticsProvider entry : loader) {
				String name = entry.getName().toLowerCase();
				if (!providers.containsKey(name)) {
					try {
						StatisticsProvider provider = entry.getClass().getConstructor().newInstance();
						StatisticsProviderItem item = new StatisticsProviderItem(provider);
						providers.put(name, item);
					} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
						LOGGER.error(e);
					}
				}
			}
		}
	}

	private StatisticsProviderFactory() {}

	public static StatisticsProvider getProvider(String name, Map<String, Object> parameters) throws Exception {
		if (name == null)
			throw new Exception("Data provider is not defined.");
		StatisticsProvider provider ;
		synchronized(lockObj) {
			String pname = name.toLowerCase();
			StatisticsProviderItem item = providers.get(pname);
			if (item == null) {
				Exception ex = new Exception("Unable to find a data provider with name '" + name + "'.");
				LOGGER.error(ex);
				throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, ex.getMessage());
			}
			provider = item.getProvider();
			if (!item.getIsInitialized()) {
				try {
					provider.init(parameters);
				} catch(Exception ex) {
					LOGGER.error(ex);
					throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, "Unable to initialize a data provider with name '" + name + "'.");
				}
				item.setIsInitialized(true);
			}
		}
		return provider;
	}
	

	public static void releaseProviders() throws Exception {
		synchronized(lockObj) {
			for(Map.Entry<String, StatisticsProviderItem> item: providers.entrySet()) {
				if (item.getValue().getIsInitialized())
					item.getValue().getProvider().close();
			}
		}
	}
}

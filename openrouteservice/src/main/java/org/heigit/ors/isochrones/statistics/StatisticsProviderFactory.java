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
package heigit.ors.isochrones.statistics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.log4j.Logger;

import heigit.ors.exceptions.InternalServerException;
import heigit.ors.isochrones.IsochronesErrorCodes;

public class StatisticsProviderFactory {
	
	private static final Logger LOGGER = Logger.getLogger(StatisticsProviderFactory.class.getName());
	
	private static Map<String, StatisticsProviderItem> _providers;
    private static Object _lockObj;

	static
	{
    	_lockObj = new Object();
		_providers = new HashMap<String, StatisticsProviderItem>();
		
		synchronized(_lockObj)
		{
			ServiceLoader<StatisticsProvider> loader = ServiceLoader.load(StatisticsProvider.class);

			Iterator<StatisticsProvider> entries = loader.iterator();
			while (entries.hasNext()) {
				StatisticsProvider entry = entries.next();
				String name = entry.getName().toLowerCase();
				
				if (!_providers.containsKey(name))
				{
					try {
						StatisticsProvider provider = entry.getClass().newInstance();
						StatisticsProviderItem item = new StatisticsProviderItem(provider);
						_providers.put(name, item);
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	public static String getProviderName(String statName)
	{
		return null;
	}
	
	public static StatisticsProvider getProvider(String name, Map<String, Object> parameters) throws Exception
	{
		if (name == null)
			throw new Exception("Data provider is not defined.");
		
		StatisticsProvider provider = null;

		synchronized(_lockObj)
		{
			String pname = name.toLowerCase();

			StatisticsProviderItem item = _providers.get(pname);

			if (item == null)
			{
				Exception ex = new Exception("Unable to find a data provider with name '" + name + "'.");
				LOGGER.error(ex);
				
				throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, ex.getMessage());
			}

			provider = item.getProvider();
			
			if (!item.getIsInitialized())
			{
				try
				{
					provider.init(parameters);
				}
				catch(Exception ex)
				{
					LOGGER.error(ex);
					
					throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, "Unable to initialize a data provider with name '" + name + "'.");
				}
				
				item.setIsInitialized(true);
			}
		}

		return provider;
	}
	

	public static void releaseProviders() throws Exception
	{
		synchronized(_lockObj)
		{
			for(Map.Entry<String, StatisticsProviderItem> item: _providers.entrySet())
			{
				if (item.getValue().getIsInitialized())
					item.getValue().getProvider().close();
			}
		}
	}
}

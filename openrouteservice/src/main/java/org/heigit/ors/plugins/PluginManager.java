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
package heigit.ors.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.apache.log4j.Logger;

public class PluginManager<T extends Plugin> {
	private static final Logger LOGGER = Logger.getLogger(PluginManager.class.getName());

	private ServiceLoader<T> _loader;
	private Object _lockObj;
	private static Map<String, Object> _pluginMgrCache = new HashMap<String, Object>();

	@SuppressWarnings("unchecked")
	public synchronized static <T extends Plugin> PluginManager<T> getPluginManager(Class<?> cls) throws Exception
	{
		PluginManager<T> pmgr = null;
		pmgr = (PluginManager<T>)_pluginMgrCache.get(cls.getName());
		if (pmgr == null)
		{
			pmgr = new PluginManager<T>(cls);
			_pluginMgrCache.put(cls.getName(), pmgr);
		}
		return pmgr;
	}

	@SuppressWarnings("unchecked")
	public PluginManager(Class<?> cls) throws Exception {
		if (cls.equals(getClass()))
			throw new Exception("Wrong class parameter");
		_loader = (ServiceLoader<T>)ServiceLoader.load(cls);
		_lockObj = new Object();
	}

	public List<T> createInstances(Map<String, Map<String, String>> parameters)
	{
		List<T> result = new ArrayList<T>(parameters.size());

		if (parameters != null && parameters.size() > 0)
		{
			for(Map.Entry<String, Map<String, String>> storageEntry : parameters.entrySet())
			{
				T instance = createInstance(storageEntry.getKey(), storageEntry.getValue());

				if (instance != null)
				{
					
					result.add(instance);
				}
				else
					LOGGER.warn(String.format(storageEntry.getKey() + " '%s' was not found.", storageEntry.getKey()));
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public T createInstance(String name, Map<String, String> params)
	{
		T instance = null;

		try
		{
			// ServiceLoader is not threadsafe
			synchronized(_lockObj)
			{
				Iterator<T> entries = _loader.iterator();
				while (instance == null && entries.hasNext()) {
					T entry = entries.next();
					if (entry.getName().equalsIgnoreCase(name))
					{
						instance = ((Class<T>)entry.getClass()).newInstance();
						instance.setParameters(params);
						break;
					}
				}
			}
		}
		catch (ServiceConfigurationError | InstantiationException | IllegalAccessException se) 
		{
			instance = null;
			se.printStackTrace();
		}

		return instance;
	}
}

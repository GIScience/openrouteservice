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
package org.heigit.ors.plugins;

import java.lang.reflect.InvocationTargetException;
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

	private final ServiceLoader<T> loader;
	private final Object lockObj;
	private static final Map<String, Object> pluginMgrCache = new HashMap<>();

	@SuppressWarnings("unchecked")
	public static synchronized <T extends Plugin> PluginManager<T> getPluginManager(Class<?> cls) throws Exception {
		PluginManager<T> pmgr = null;
		pmgr = (PluginManager<T>) pluginMgrCache.get(cls.getName());
		if (pmgr == null)
		{
			pmgr = new PluginManager<>(cls);
			pluginMgrCache.put(cls.getName(), pmgr);
		}
		return pmgr;
	}

	@SuppressWarnings("unchecked")
	public PluginManager(Class<?> cls) throws Exception {
		if (cls.equals(getClass()))
			throw new Exception("Wrong class parameter");
		loader = (ServiceLoader<T>)ServiceLoader.load(cls);
		lockObj = new Object();
	}

	public List<T> createInstances(Map<String, Map<String, String>> parameters) {
		List<T> result = new ArrayList<>(parameters.size());
		if (!parameters.isEmpty()) {
			for(Map.Entry<String, Map<String, String>> storageEntry : parameters.entrySet()) {
				T instance = createInstance(storageEntry.getKey(), storageEntry.getValue());

				if (instance != null) {
					result.add(instance);
				}
				else
					LOGGER.warn(String.format("'%s' was not found.", storageEntry.getKey()));
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public T createInstance(String name, Map<String, String> params) {
		T instance = null;
		try {
			// ServiceLoader is not threadsafe
			synchronized(lockObj) {
				Iterator<T> entries = loader.iterator();
				while (entries.hasNext()) {
					T entry = entries.next();
					if (entry.getName().equalsIgnoreCase(name)) {
						instance = ((Class<T>)entry.getClass()).getDeclaredConstructor().newInstance();
						instance.setParameters(params);
						break;
					}
				}
			}
		} catch (ServiceConfigurationError | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException se)  {
			instance = null;
			LOGGER.error(se);
		}
		return instance;
	}
}

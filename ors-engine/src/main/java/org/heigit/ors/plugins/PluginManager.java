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

import org.apache.log4j.Logger;
import org.heigit.ors.config.profile.ExtendedStorageName;
import org.heigit.ors.config.profile.ExtendedStorageProperties;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.heigit.ors.config.profile.ExtendedStorageName.*;

public class PluginManager<T extends Plugin> {
    private static final Logger LOGGER = Logger.getLogger(PluginManager.class.getName());
    private static final Set<ExtendedStorageName> STORAGES_MIGRATED_TO_ENCODED_VALUES = Set.of(
            HEAVY_VEHICLE,
            OSM_ID,
            TOLLWAYS,
            WAY_CATEGORY,
            WAY_SURFACE_TYPE,
            ROAD_ACCESS_RESTRICTIONS,
            HILL_INDEX,
            TRAIL_DIFFICULTY
    );

    private final ServiceLoader<T> loader;
    private final Object lockObj;
    private static final Map<String, Object> pluginMgrCache = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static synchronized <T extends Plugin> PluginManager<T> getPluginManager(Class<?> cls) throws Exception {
        PluginManager<T> pmgr = null;
        pmgr = (PluginManager<T>) pluginMgrCache.get(cls.getName());
        if (pmgr == null) {
            pmgr = new PluginManager<>(cls);
            pluginMgrCache.put(cls.getName(), pmgr);
        }
        return pmgr;
    }

    @SuppressWarnings("unchecked")
    public PluginManager(Class<?> cls) throws Exception {
        if (cls.equals(getClass()))
            throw new Exception("Wrong class parameter");
        loader = (ServiceLoader<T>) ServiceLoader.load(cls);
        lockObj = new Object();
    }

    public List<T> createInstances(Map<String, ExtendedStorageProperties> parameters) {
        List<T> result = new ArrayList<>(parameters.size());
        if (!parameters.isEmpty()) {
            for (Map.Entry<String, ExtendedStorageProperties> storageEntry : parameters.entrySet()) {
                String storageName = storageEntry.getKey();
                ExtendedStorageName storage;
                try {
                    storage = ExtendedStorageName.getEnum(storageName);
                } catch (IllegalArgumentException ex) {
                    LOGGER.warn("Unknown extended storage '%s'; skipping.".formatted(storageName));
                    continue;
                }
                if (!STORAGES_MIGRATED_TO_ENCODED_VALUES.contains(storage)) {
                    T instance = createInstance(storageName, storageEntry.getValue());

                    if (instance != null) {
                        result.add(instance);
                    } else {
                        LOGGER.warn("'%s' was not found.".formatted(storageName));
                    }
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public T createInstance(String name, ExtendedStorageProperties params) {
        T instance = null;
        try {
            // ServiceLoader is not threadsafe
            synchronized (lockObj) {
                Iterator<T> entries = loader.iterator();
                while (entries.hasNext()) {
                    T entry = entries.next();
                    if (entry.getName().equalsIgnoreCase(name)) {
                        instance = ((Class<T>) entry.getClass()).getDeclaredConstructor().newInstance();
                        instance.setParameters(params);
                        break;
                    }
                }
            }
        } catch (ServiceConfigurationError | InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException se) {
            instance = null;
            LOGGER.error(se);
        }
        return instance;
    }
}

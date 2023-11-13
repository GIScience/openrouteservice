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
package org.heigit.ors.config;

import com.graphhopper.util.Helper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import org.apache.log4j.Logger;
import org.heigit.ors.util.FileUtility;
import org.heigit.ors.util.StringUtility;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppConfig {

    public static final String PREFIX_ORS_SERVICES = "ors.services.";
    private Config config;
    private static AppConfig global;
    private static String osmMd5Hash = null;
    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final String SERVICE_NAME_ROUTING = "routing";
    private static final String SERVICE_NAME_ISOCHRONES = "isochrones";

    public AppConfig(String path) {
        File file = new File(path);
        config = ConfigFactory.parseFile(file);
    }

    public AppConfig() {
        try {
            File configFile;
            String appConfigResource = "app.config";
            if (System.getProperty("ors_config") != null) {
                configFile = new FileSystemResource(System.getProperty("ors_config")).getFile();
                LOGGER.info("System property 'ors_config' used as configuration path");
            } else if (System.getProperty("ors_app_config") != null) {
                configFile = new FileSystemResource(System.getProperty("ors_app_config")).getFile();
                LOGGER.info("System property 'ors_app_config' used as configuration path");
                LOGGER.warn("""
                        DEPRECATION NOTICE: The system property 'ors_app_config' will be not be supported in the\
                         future\
                        """);
                LOGGER.warn("Use 'ors_config' instead");
            } else if (System.getenv("ORS_CONFIG") != null) {
                configFile = new FileSystemResource(System.getenv("ORS_CONFIG")).getFile();
                LOGGER.info("Environment variable 'ORS_CONFIG' used as configuration path");
            } else if (System.getenv("ORS_APP_CONFIG") != null) {
                configFile = new ClassPathResource(System.getenv("ORS_APP_CONFIG")).getFile();
                LOGGER.info("Environment variable 'ORS_APP_CONFIG' used as configuration path");
                LOGGER.warn("""
                        DEPRECATION NOTICE: The Environment variable 'ORS_APP_CONFIG' will be not be supported\
                         in the future\
                        """);
                LOGGER.warn("Use 'ORS_CONFIG' instead");
            } else if (new ClassPathResource("ors-config.json").isFile()) {
                configFile = new ClassPathResource("ors-config.json").getFile();
                LOGGER.info("Default path of 'ors-config.json' used for configuration");
                if (new ClassPathResource(appConfigResource).isFile()) {
                    LOGGER.warn("""
                            DEPRECATION NOTICE: You seem to have an unused 'app.config' file, which won't be \
                            supported in the future\
                            """);
                }
            } else if (new ClassPathResource(appConfigResource).isFile()) {
                configFile = new ClassPathResource(appConfigResource).getFile();
                LOGGER.info("Deprecated path of 'app.config' used");
                LOGGER.warn("""
                        DEPRECATION NOTICE: The used 'app.config' configuration path will not be supported in the \
                        future.\
                        """);
                LOGGER.warn("Use 'ors-config.json' instead.");
            } else {
                throw new IOException("""
                        No valid configuration file found in 'ors-api/src/main/resources'. \
                        Did you copy ors-config-sample.json to ors-config.json?\
                        """);
            }
            LOGGER.info("Loading configuration from " + configFile);
            config = ConfigFactory.parseFile(configFile);
            config = overrideFromEnvVariables(config);

            LOGGER.warn("Deprecation notice: Old configuration method with JSON files is deprecated. Switch to ors-config.yml files!");
        } catch (IOException ioe) {
            // no deprecated JSON config found
        }

        //Modification by H Leuschner: Save md5 hash of map file in static String for access with every request
        String graphPath = getServiceParameter("routing.profiles.default_params", "graphs_root_path");
        if (graphPath != null) {
            File graphsDir = new File(graphPath);
            File[] md5Files = graphsDir.listFiles(pathname -> pathname.getName().endsWith(".md5"));
            if (md5Files != null && md5Files.length == 1) {
                try {
                    osmMd5Hash = FileUtility.readFile(md5Files[0].toString()).trim();
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
        }
    }

    public static AppConfig getGlobal() {
        if (global == null)
            global = new AppConfig();
        return global;
    }

    public String getParameter(String section, String paramName) {
        try {
            return config.getString("ors." + section + "." + paramName);
        } catch (Exception e) {
            // IGNORE
        }
        return null;
    }

    public List<String> getStringList(String path) {
        try {
            return config.getStringList(path);
        } catch (Exception e) {
            // IGNORE
        }
        return new ArrayList<>();
    }

    public String getServiceParameter(String serviceName, String paramName) {
        try {
            return config.getString(PREFIX_ORS_SERVICES + serviceName + "." + paramName);
        } catch (Exception e) {
            // IGNORE
        }
        return null;
    }

    private Config overrideFromEnvVariables(Config baseConfig) {
        if (System.getenv("GRAPHS_FOLDER") != null) {
            LOGGER.info("Environment variable 'GRAPHS_FOLDER' used as graphs folder path");
            String graphsFolderPath = System.getenv("GRAPHS_FOLDER");
            Config newConfig = ConfigFactory.parseString("ors.services.routing.profiles.default_params.graphs_root_path=".concat(graphsFolderPath));
            baseConfig = newConfig.withFallback(baseConfig);
        }
        if (System.getenv("ELEVATION_CACHE_FOLDER") != null) {
            LOGGER.info("Environment variable 'ELEVATION_CACHE_FOLDER' used as elevation cache folder path");
            String elevationCacheFolder = System.getenv("ELEVATION_CACHE_FOLDER");
            Config newConfig = ConfigFactory.parseString("ors.services.routing.profiles.default_params.elevation_cache_path=".concat(elevationCacheFolder));
            baseConfig = newConfig.withFallback(baseConfig);
        }
        if (System.getenv("PBF_FILE_PATH") != null) {
            LOGGER.info("Environment variable 'PBF_FILE_PATH' used as pbf file path");
            String pbfPath = System.getenv("PBF_FILE_PATH");
            Config newConfig = ConfigFactory.parseString("ors.services.routing.sources=[".concat(pbfPath).concat("]"));
            baseConfig = newConfig.withFallback(baseConfig);
        }
        if (System.getenv("LOGS_FOLDER") != null) {
            LOGGER.info("Environment variable 'LOGS_FOLDER' used as logs folder path");
            String logsFolder = System.getenv("LOGS_FOLDER");
            Config newConfig = ConfigFactory.parseString("ors.logging.location=".concat(logsFolder));
            baseConfig = newConfig.withFallback(baseConfig);
        }
        return baseConfig;
    }

    public String getRoutingProfileParameter(String profile, String paramName) {
        try {
            String rootPath = PREFIX_ORS_SERVICES + "routing.profiles";
            ConfigObject configObj = config.getObject(rootPath);
            for (String key : configObj.keySet()) {
                if (key.startsWith("profile-")) {
                    String profileName = getServiceParameter(SERVICE_NAME_ROUTING, "profiles." + key + ".profiles");
                    if (profile.equals(profileName)) {
                        String profileValue = getServiceParameter(SERVICE_NAME_ROUTING, "profiles." + key + ".parameters." + paramName);
                        if (profileValue != null) {
                            return profileValue;
                        }
                    }
                }
            }
            return config.getString(rootPath + ".default_params." + paramName);
        } catch (Exception e) {
            // IGNORE
        }
        return null;
    }

    public List<? extends ConfigObject> getObjectList(String serviceName, String paramName) {
        try {
            return config.getObjectList(PREFIX_ORS_SERVICES + serviceName + "." + paramName);
        } catch (Exception e) {
            // IGNORE
        }
        return new ArrayList<>();
    }

    public boolean getBoolean(String path) {
        try {
            ConfigObject configObj = config.getObject("ors");
            return configObj.toConfig().getBoolean(path);
        } catch (Exception e) {
            // IGNORE
        }
        return false;
    }

    public double getDouble(String path) {
        try {
            ConfigObject configObj = config.getObject("ors");
            return configObj.toConfig().getDouble(path);
        } catch (Exception e) {
            // IGNORE
        }
        return Double.NaN;
    }

    public List<? extends ConfigObject> getObjectList(String paramName) {
        try {
            return config.getObjectList("ors." + paramName);
        } catch (Exception e) {
            // IGNORE
        }
        return new ArrayList<>();
    }

    public List<Double> getDoubleList(String serviceName, String paramName) {
        try {
            return config.getDoubleList(PREFIX_ORS_SERVICES + serviceName + "." + paramName);
        } catch (Exception e) {
            // IGNORE
        }
        return new ArrayList<>();
    }

    public List<String> getServiceParametersList(String serviceName, String paramName) {
        try {
            return config.getStringList(PREFIX_ORS_SERVICES + serviceName + "." + paramName);
        } catch (Exception e) {
            // IGNORE
        }
        return new ArrayList<>();
    }

    public static boolean hasValidMD5Hash() {
        return osmMd5Hash != null;
    }

    public static String getMD5Hash() {
        return osmMd5Hash;
    }

    public Map<String, Object> getServiceParametersMap(String serviceName, String paramName, boolean quotedStrings) {
        Map<String, Object> result = null;
        try {
            String rootPath = PREFIX_ORS_SERVICES + serviceName + "." + paramName;
            ConfigObject configObj = config.getObject(rootPath);
            result = new HashMap<>();
            for (String key : configObj.keySet()) {
                Object value = null;
                ConfigValue paramValue = config.getValue(rootPath + "." + key);
                switch (paramValue.valueType()) {
                    case NUMBER:
                    case LIST:
                    case BOOLEAN:
                        value = paramValue.unwrapped();
                        break;
                    case OBJECT:
                        value = getServiceParametersMap(serviceName, paramName + "." + key, quotedStrings);
                        break;
                    case STRING:
                        if (quotedStrings)
                            value = paramValue.render();
                        else
                            value = StringUtility.trim(paramValue.render(), '"');
                        break;
                    default:
                        break;
                }
                result.put(key, value);
            }
        } catch (Exception ex) {
            // IGNORE
        }

        return result;
    }

    public static String getRoutingParameter(String paramName) {
        return getGlobal().getServiceParameter(SERVICE_NAME_ROUTING, paramName);
    }

    public static String getRoutingParameter(String paramName, boolean notNull) {
        String value = getGlobal().getServiceParameter(SERVICE_NAME_ROUTING, paramName);
        if (notNull && Helper.isEmpty(value))
            throw new IllegalArgumentException("Parameter '" + paramName + "' must not be null or empty.");

        return value;
    }

    public static List<String> getRoutingParametersList(String paramName) {
        return getGlobal().getServiceParametersList(SERVICE_NAME_ROUTING, paramName);
    }

    public static Map<String, Object> getRoutingParametersMap(String paramName, boolean quotedStrings) {
        return getGlobal().getServiceParametersMap(SERVICE_NAME_ROUTING, paramName, quotedStrings);
    }

    public static List<String> getIsochronesParametersList(String paramName) {
        return getGlobal().getServiceParametersList(SERVICE_NAME_ISOCHRONES, paramName);
    }

    public static Map<String, Object> getIsochronesParametersMap(String paramName, boolean quotedStrings) {
        return getGlobal().getServiceParametersMap(SERVICE_NAME_ISOCHRONES, paramName, quotedStrings);
    }
}

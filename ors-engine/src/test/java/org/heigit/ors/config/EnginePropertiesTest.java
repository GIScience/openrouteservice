package org.heigit.ors.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.heigit.ors.config.profile.ProfileProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnginePropertiesTest {

    //language=JSON
    private final String testJson = """
            {
              "graph_management": {
                "graph_extent": null,
                "repository_uri": null,
                "repository_name": null,
                "repository_profile_group": null,
                "download_schedule": "0 0 0 31 2 *",
                "activation_schedule": "0 0 0 31 2 *",
                "max_backups": 0
              },           
              "profile_default": {
                "enabled": false,
                "source_file": "/path/to/source/file",
                "graph_path": "/path/to/graphs",
                "preparation": {
                  "min_network_size": 300,
                  "methods": {
                    "lm": {
                      "enabled": false,
                      "weightings": "shortest",
                      "landmarks": 2
                    }
                  }
                },
                "execution": {
                  "methods": {
                    "lm": {
                      "active_landmarks": 2
                    }
                  }
                },
                "ext_storages": {
                  "WayCategory": {
                    "enabled": true
                  },
                  "GreenIndex": {
                    "enabled": true,
                    "filepath": "/path/to/file.csv"
                  }
                }
              },
              "profiles": {
                "car": {
                  "encoder_name": "driving-car",
                  "encoder_options": {},
                  "preparation": {
                    "methods": {
                      "lm": {
                        "enabled": true,
                        "threads": 5
                      }
                    }
                  },
                  "execution": {
                    "methods": {
                      "lm": {
                        "active_landmarks": 2
                      }
                    }
                  },
                  "ext_storages": {}
                },
                "hgv": {
                  "encoder_name": "driving-hgv",
                  "preparation": {
                    "min_network_size": 900,
                    "methods": {
                      "lm": {
                        "enabled": true
                      }
                    }
                  },
                  "ext_storages": {
                    "HeavyVehicle": {
                      "restrictions": true
                    }
                  }
                },
                "car-custom": {
                  "enabled": true,
                  "encoder_name": "driving-car",
                  "preparation": {
                    "min_network_size": 900
                  }
                },
                "car-custom2": {
                  "enabled": false,
                  "encoder_name": "driving-car"
                }
              }
            }""";
    EngineProperties enginePropertiesTest;
    EngineProperties defaultEngineProperties;
    HashSet<String> defaultProfilePropertiesIgnoreList = new HashSet<>(List.of("initialized", "graphsDataAccess", "elevation.dataAccess", "elevation.cacheClear", "graphManagement", "profileDefault.enabled", "profileDefault.extStorages", "profileDefault.preparation.minNetworkSize", "profileDefault.preparation.methods.lm.enabled", "profileDefault.preparation.methods.lm.weightings", "profileDefault.preparation.methods.lm.landmarks", "profileDefault.execution.methods.lm.activeLandmarks", "profiles"));

    @BeforeEach
    void setUp() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        enginePropertiesTest = mapper.readValue(testJson, EngineProperties.class);
        // Defaults to check against
        defaultEngineProperties = new EngineProperties();
    }

    @Test
    void getActiveProfilesReturnsEmptyMapWhenProfileDefaultIsNotEnabled() {
        EngineProperties engineProperties = new EngineProperties();
        Map<String, ProfileProperties> activeProfiles = engineProperties.getInitializedActiveProfiles();
        assertNotNull(activeProfiles);
        assertTrue(activeProfiles.isEmpty());
    }

    @Test
    void getActiveProfilesReturnsNonEmptyMapWhenProfileDefaultIsEnabled() {
        enginePropertiesTest.getProfileDefault().setEnabled(true);
        Map<String, ProfileProperties> activeProfiles = enginePropertiesTest.getInitializedActiveProfiles();
        assertNotNull(activeProfiles);
        assertFalse(activeProfiles.isEmpty());
    }

    @Test
    void getActiveProfilesReturnsCorrectProfiles() {
        enginePropertiesTest.getProfileDefault().setEnabled(true);
        Map<String, ProfileProperties> activeProfiles = enginePropertiesTest.getInitializedActiveProfiles();
        assertTrue(activeProfiles.containsKey("car"));
        assertTrue(activeProfiles.containsKey("hgv"));
        assertTrue(activeProfiles.containsKey("car-custom"));
    }

    @Test
    void getActiveProfilesReturnsCorrectProfileNames() {
        enginePropertiesTest.getProfileDefault().setEnabled(true);
        Map<String, ProfileProperties> activeProfiles = enginePropertiesTest.getInitializedActiveProfiles();
        assertEquals("car", activeProfiles.get("car").getProfileName());
        assertEquals("hgv", activeProfiles.get("hgv").getProfileName());
        assertEquals("car-custom", activeProfiles.get("car-custom").getProfileName());
    }

    @Test
    void getActiveProfilesReturnsCorrectGraphPath() {
        enginePropertiesTest.getProfileDefault().setEnabled(true);
        Map<String, ProfileProperties> activeProfiles = enginePropertiesTest.getInitializedActiveProfiles();
        assertEquals(enginePropertiesTest.getProfileDefault().getGraphPath().resolve("car"), activeProfiles.get("car").getGraphPath());
        assertEquals(enginePropertiesTest.getProfileDefault().getGraphPath().resolve("hgv"), activeProfiles.get("hgv").getGraphPath());
        assertEquals(enginePropertiesTest.getProfileDefault().getGraphPath().resolve("car-custom"), activeProfiles.get("car-custom").getGraphPath());
    }
}
# Running openrouteservice as JAR

Since version 8, openrouteservice can be built as a fat JAR file that contains all its dependencies and can be run as stand-alone application.

## Prerequisites

* [java](https://www.java.com/en/) 17 (or higher) should be available, preferably as default Java environment.

To run openrouteservice, you also need an OSM data file, e.g. from [Geofabrik](http://download.geofabrik.de). For more details, see chapter [Data](data.md).

## Download

Starting with version 8 you can download the ready to use JAR file from the "Assets" section of the desired release from our GitHub [releases](https://github.com/GIScience/openrouteservice/releases) page.

## Build

How this is done is independent of the artifact type you want to use and is documented in [Building from Source](building-from-source.md).

## Run

To run the openrouteservice application, use the following command:

```shell 
java -jar ors.jar
```

## Configure

The recommended way to configure an openrouteservice instance run plain using the JAR file is to use a YAML configuration file. You can download an example file by using the following command: 

```shell 
wget https://raw.githubusercontent.com/GIScience/openrouteservice/main/ors-config.yml
```

For details on how to make openrouteservice apply the settings in the configuration file (there are multiple options) see chapter [Configuration](configuration/index.md).

## Troubleshooting

The place where the log files are written is defined by the configuration property `logging.file.name`.
In the [logging documentation](configuration/spring/logging.md) you find more logging options like setting log level etc.

The openrouteservice startup log looks similar to this:
```shell
ors-app  | ▢ Startup command: java -Djava.awt.headless=true -server -XX:TargetSurvivorRatio=75 -XX:SurvivorRatio=64 -XX:MaxTenuringThreshold=3 -XX:+UseG1GC -XX:+ScavengeBeforeFullGC -XX:ParallelGCThreads=4 -Xms1g -Xmx2g  -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9001 -Dcom.sun.management.jmxremote.rmi.port=9001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=localhost  -jar /ors.jar
ors-app  | 
ors-app  |   .   ____          _            __ _ _
ors-app  |  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
ors-app  | ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
ors-app  |  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
ors-app  |   '  |____| .__|_| |_|_| |_\__, | / / / /
ors-app  |  =========|_|==============|___/=/_/_/_/
ors-app  |  :: Spring Boot ::                (v3.1.6)
ors-app  | 
ors-app  | 2024-03-12 10:54:45 INFO                                                  main [ o.h.o.a.Application                      ]   Starting Application v8.0-SNAPSHOT using Java 21.0.2 with PID 1 (/ors.jar started by root in /home/ors)
ors-app  | 2024-03-12 10:54:45 INFO                                                  main [ o.h.o.a.Application                      ]   The following 1 profile is active: "default"
ors-app  | 2024-03-12 10:54:45 INFO                                                  main [ o.h.o.a.ORSEnvironmentPostProcessor      ]   
ors-app  | 2024-03-12 10:54:45 INFO                                                  main [ o.h.o.a.ORSEnvironmentPostProcessor      ]   Configuration lookup started.
ors-app  | 2024-03-12 10:54:45 INFO                                                  main [ o.h.o.a.ORSEnvironmentPostProcessor      ]   Configuration file set by environment variable.
ors-app  | 2024-03-12 10:54:45 INFO                                                  main [ o.h.o.a.ORSEnvironmentPostProcessor      ]   Loaded file '/home/ors/config/ors-config.yml'
ors-app  | 2024-03-12 10:54:45 INFO                                                  main [ o.h.o.a.ORSEnvironmentPostProcessor      ]   Configuration lookup finished.
ors-app  | 2024-03-12 10:54:45 INFO                                                  main [ o.h.o.a.ORSEnvironmentPostProcessor      ]   
ors-app  | 2024-03-12 10:54:46 INFO                                              ORS-Init [ o.h.o.a.s.l.ORSInitContextListener       ]   Initializing ORS...
ors-app  | 2024-03-12 10:54:46 INFO                                              ORS-Init [ o.h.o.r.RoutingProfileManager            ]   Total - 1024 MB, Free - 965.93 MB, Max: 2 GB, Used - 58.07 MB
ors-app  | 2024-03-12 10:54:46 INFO                                              ORS-Init [ o.h.o.r.RoutingProfileManager            ]   ====> Initializing profiles from '/home/ors/files/example-heidelberg.osm.gz' (1 threads) ...
ors-app  | 2024-03-12 10:54:46 INFO                                              ORS-Init [ o.h.o.r.RoutingProfileManager            ]   2 profile configurations submitted as tasks.
ors-app  | 2024-03-12 10:54:46 INFO                                     ORS-pl-wheelchair [ o.h.o.r.RoutingProfile                   ]   [1] Profiles: 'wheelchair', location: './graphs/wheelchair'.
ors-app  | 2024-03-12 10:54:47 INFO                                     ORS-pl-wheelchair [ o.h.o.r.g.e.ORSGraphHopper               ]   version v4.9.1|2024-01-17T09:08:46Z (7,20,5,4,5,7)
ors-app  | 2024-03-12 10:54:47 INFO                                     ORS-pl-wheelchair [ o.h.o.r.g.e.ORSGraphHopper               ]   graph wheelchair|RAM_STORE|3D|turn_cost|,,,,, details:edges:0(0MB), nodes:0(0MB), name:(0MB), geo:0(0MB), bounds:1.7976931348623157E308,-1.7976931348623157E308,1.7976931348623157E308,-1.7976931348623157E308,1.7976931348623157E308,-1.7976931348623157E308
ors-app  | 2024-03-12 10:54:47 INFO                                     ORS-pl-wheelchair [ o.h.o.r.g.e.ORSGraphHopper               ]   No custom areas are used, custom_areas.directory not given
ors-app  | 2024-03-12 10:54:47 INFO                                     ORS-pl-wheelchair [ o.h.o.r.g.e.ORSGraphHopper               ]   start creating graph from /home/ors/files/example-heidelberg.osm.gz
ors-app  | 2024-03-12 10:54:47 INFO                                     ORS-pl-wheelchair [ o.h.o.r.g.e.ORSGraphHopper               ]   using wheelchair|RAM_STORE|3D|turn_cost|,,,,, memory:totalMB:1024, usedMB:260
ors-app  | 2024-03-12 10:54:47 INFO                                                  main [ o.h.o.a.Application                      ]   Started Application in 2.442 seconds (process running for 3.066)
ors-app  | 2024-03-12 10:54:47 INFO                                                  main [ o.h.o.a.Application                      ]   openrouteservice {"build_date":"2024-03-08T15:01:47Z","version":"8.0"}
```

Most important information here:

* The version of openrouteservice and java: `Starting Application v8.0-SNAPSHOT using Java 21.0.2`
* The evaluated configuration file: `Loaded file '/home/ors/config/ors-config.yml'`
* Memory usage: `Total - 1024 MB, Free - 965.93 MB, Max: 2 GB, Used - 58.07 MB`
* The evaluated OSM file: `====> Initializing profiles from '/home/ors/files/example-heidelberg.osm.gz'`
* Potential errors with your setup

After the startup section, you can find information about errors at run time.

::: tip Hint
The output `The following 1 profile is active: "default"` is from spring,
it refers to the active **spring profile** and has nothing to do with routing profiles.
:::

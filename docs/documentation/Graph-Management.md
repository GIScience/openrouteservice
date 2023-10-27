# Graph Management 

Depending on the value in the configuration property `source_file`, Openrouteservice can be started in two different modes:

* **Graph generation mode**, when a OSM file is configured, e.g.: `source_file=/opt/openrouteservice/files/planet.pbf`. This is the classic mode, where Openrouteservice generates a graph locally for each configured and activated profile with the configured OSM data as input. 
* **Graph management mode**, when the URL of a Openrouteservice Graph Repository is configured, e.g.: `source_file=https://nexus.openrouteservice.org/service/rest/ors-graphs/andorra`. Graph management mode is a new feature introduced in version TODO-Version. In this mode, openrouteservice no longer generates graphs locally, but downloads precalculated graphs from a special Openrouteservice graph repository and checks for updates on a regular basis.

## Graph folder structure

Before version TODO-Version, all graph files were located in a directory named like its routing profile, e.g. "car":

```commandline
car
├── edges
├── geometry
├── ...
└── turn_costs
```

Since version TODO-Version, the structure was extended by one directory level. 
For each profile, a hash value is generated and the graph files for this graph configuration is located in a subdirectory named with this hash value.

```commandline
car
└── 8250b2498273af0908682e2d660f279a                    # Active graph of the active profile. The graph will be backed up when a new one is activated.
    ├── geometry
    ├── ...
    └── turn_costs
```

The hash value is based on all configuration properties that are relevant for a graph.
When openrouteservice is started with a changed routing profile configuration (even if only one relevant parameter was changed), 
openrouteservice will generate a new graph in a new hash-directory:

```commandline
car
├── 8250b2498273af0908682e2d660f279a                    # Graph of the initial profile configuration
│   ├── geometry
│   ├── ...
│   └── turn_costs
└── d4d43791efac62e0bf75322ef17ed92c                    # Graph of the changed profile configuration
    ├── geometry
    ├── ...
    └── turn_costs
```


## Files in the profile folders

```commandline
car
├── 8250b2498273af0908682e2d660f279a                    # Active graph of the active profile. The graph will be backed up when a new one is activated.
│   ├── 8250b2498273af0908682e2d660f279a.json           # Metadata about the graph and the profile configuration that was used for generating it 
│   ├── ...                                             # Graphhopper graph files and extensions
├── 8250b2498273af0908682e2d660f279a_new_incomplete     # Extraction folder for a downloaded graph. When extraction is done, it "_incomplete" will be removed from the folder name.  
│   ├── ...                                             
├── 8250b2498273af0908682e2d660f279a_new                # Extracted downloaded graph from the graph repository. This graph will be activated on the next system start.  
│   ├── 8250b2498273af0908682e2d660f279a.json           # Metadata
│   ├── ...                                             # Graphhopper graph files and extensions
├── 8250b2498273af0908682e2d660f279a_2023-10-18_152345  # Backup of a previous graph. The timestamp is the date when the graph was replaced by a new one. 
│   ├── 8250b2498273af0908682e2d660f279a.json           # Metadata
│   ├── ...                                             # Graphhopper graph files and extensions
├── 8250b2498273af0908682e2d660f279a.json               # Downloaded metadata of the newest graph for this profile in the repository 
├── 8250b2498273af0908682e2d660f279a.json.incomplete    #      first the file is downloaded with extension `incomplete`, which is removed when the download (of this file) is finished
├── 8250b2498273af0908682e2d660f279a.ghz                # Downloaded graph of this profile 
└── 8250b2498273af0908682e2d660f279a.ghz.incomplete     #      first the file is downloaded with extension `incomplete`, which is removed when the download (of this file) is finished
```


## Scheduled updates of graphs

The update of graphs is split into two independent steps:

* Download of new graphs from the repository
* Activation of downloaded graphs

The schedule for both steps can be configured separately, see the following subchapters.

### Graph Download

There is a configuration property where the schedule of lookups for new graphs in the repository can be defined:

```yaml
ors:
  engine:
    graphservice:
        schedule:
            download:
                cron: 0 0 * * * *
```

The value is a cron pattern with 6 positions. The fields read from left to right are interpreted as follows:

* second
* minute
* hour
* day of month
* month
* day of week

For more information see [org.springframework.scheduling.annotation](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html#cron())

If the property is not set, the default is "never".

When openrouteservice detects new compressed graphs in the repository, 
these are downloaded and extracted, but not yet activated (see next section).
In theory, it is possible, that a downloaded graph is outdated before it was activated. 
In this case, the newer graph is downloaded from the repository and will be the next graph to be activated. 


### Graph Activation

An independent process checks on a regular basis, if there are downloaded graphs that can be activated.
If this is the case, the service loads the new graphs which causes a short downtime of the service.

The schedule of the activation process can be configured with a separate configuration property, 
e.g. it is independent from the download process. Activation can be scheduled to a time outside the "rush hour".

```yaml
ors:
  engine:
    graphservice:
        schedule:
            activate:
                cron: 0 30 2 * * *
```

If openrouteservice is still busy with downloading or extracting a graph at activation time,
new activation attempts are done every minute and activation happens as soon as possible.

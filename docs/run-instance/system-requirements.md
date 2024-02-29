# System Requirements

When running openrouteservice, by far the biggest consideration that needs to be made is the amount of RAM that is available. Smaller areas such as Baden-Württemberg in Germany can run happily on a mid-range system, but for larger areas you need more RAM. For example, in the openrouteservice public API infrastructure, each profile for the planet is running on a 128GB machine. Anything less than that and you will run out of memory during the build process.

The main things that affect the amount of RAM needed are:
* The size of the data being loaded (OSM data)
* The profile(s) being built (bike and pedestrian need more space than driving profiles)
* The routing optimisations (having the optimised routing algorithms needs more RAM)
* Elevation (using elevation needs the elevation data to also be loaded into RAM, so the larger the geographic area, the more RAM is needed for elevation)
* Extra info (Each profile can have extra info calculated, each of these requires RAM for storage).

As a guide, you can look at the size of OSM data extracts as a rough guide as to how much RAM you would need. [Geofabrik](https://download.geofabrik.de) provides a number of these extracts along with the file sizes of the pbf files. The planet file is around 50GB and for bike and pedestrian profiles, this needs around 100-105GB RAM. Germany is around 3GB and can normally be built on a reasonable system with around 8GB RAM, and Baden-Württemberg is 450MB which can be built on a machine with around 2-4GB RAM. In general though, having more RAM also speeds up the build process as less garbage collection actions need to be taken, and you should also be aware of any other services running in parallel to openrouteservice that consume RAM on the machine.

[//]: # (TODO: @koebi was working on a set of example memory footprint values or something similar? Add here)

## JVM configuration

Independent of the configuration of the openrouteservice itself, you might need to adjust settings of the Java Virtual Machine (JVM) running the code. Since the memory requirements of openrouteservice are proportional to the size of the OSM data (and therefore the resulting graph), the setting that needs to be adjusted most often is the heap memory size.

[//]: # (TODO: elaborate)

```
JAVA_OPTS="-Xms105g -Xmx105g "
```

## Memory mapping in large builds with a containerized openrouteservice instance
If you are running a large build (e.g. a planet file) then you may need to increase the number of memory mappings. You only need to do this on the host machine as this value is used by the Docker containers running on it as well. To do this, go into the system configuration file with `sudo nano /etc/sysctl.conf` and add the following line to the bottom of the file:

```shell
vm.max_map_count=81920
```

The usual sign that you need to do this change is if you see something similar to the following in your logs:

```shell
# There is insufficient memory for the Java Runtime Environment to continue.
# Native memory allocation (mmap) failed to map 16384 bytes for committing reserved memory.
# An error report file with more information is saved as:
# /ors-core/hs_err_pid128.log
```


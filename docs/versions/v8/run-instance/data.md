# Data: Input and output folders and files

## Input Files

The most important input file is the config file.
In the [Configuration](/run-instance/configuration/index.md) section you find everything about configuration options and files.

Openrouteservice makes use of public open-source data. To generate the best routes, a number of different datasets are used.

### OSM Data
The base data used for the road network and related information (road type, access restrictions etc.) is [OpenStreetMap](https://openstreetmap.org) (OSM). This dataset is a free and open dataset that can be edited by anyone.  
You can download the latest OSM dataset from https://planet.openstreetmap.org/ or regional extracts from http://download.geofabrik.de/.

The OSM file to be used needs be configured with the property [`ors.engine.source_file`](/run-instance/configuration/ors/engine/index.md).

### Elevation
The data used for elevation are [SRTM](http://srtm.csi.cgiar.org/) and [GMTED](https://www.usgs.gov/coastal-changes-and-impacts/gmted2010).  

Configuration: [`ors.engine.elevation`](/run-instance/configuration/ors/engine/elevation.md)

### Population
When requesting isochrones, you can also request to get population data for the isochrone areas. The data used for this is the [Global Human Settlement Layer (GHSL)](https://ghsl.jrc.ec.europa.eu/ghs_pop2023.php) from the European Commission.
Note, that while the dataset was published in 2023, the most recent data contained is from 2020. This is used by the openrouteservice at a resolution of 100m.

### Borders
Data relating to the avoid borders features is derived from administrative boundaries features in OpenStreetMap. Information about open borders is obtained from [Wikipedia](https://en.wikipedia.org/wiki/Open_border).

Configuration parameters: [`ors.engine.profiles.*.ext_storages.Borders`](/run-instance/configuration/ors/engine/profiles.md#borders)

### GTFS
The public transport profile integrates [GTFS](https://developers.google.com/transit/gtfs) data for the public transit part. GTFS feeds can be obtained e.g. from sites like https://gtfs.de/ (for Germany), or from local public transport operators.

Configuration parameters: [`ors.engine.profiles.*.gtfs_file`](/run-instance/configuration/ors/engine/profiles.md)

### Green & Quiet
The data used to identify green and quiet routes were derived from research projects in the GIScience research group at Heidelberg University. 
More information about these can be found on the GIScience news blog [here](https://giscienceblog.uni-heidelberg.de/2017/07/03/healthy-routing-prefering-green-areas-added-to-openrouteserviceorg/) and [here](http://giscienceblog.uni-heidelberg.de/2017/07/10/reducing-stress-by-avoiding-noise-with-quiet-routing-in-openrouteservice/)

Configuration parameters: [`ors.engine.profiles.*.ext_storages`](/run-instance/configuration/ors/engine/profiles.md#ext-storages)


## Output Files

Openrouteservice produces output files of three types, for which the paths can be configured. The directories these paths point to need to be *writable*. 

### Graphs

openrouteservice reads the input data and computes a graph for each enabled routing profile. 

The root directory for the graphs can be configured with the configuration property [`ors.engine.graphs_root_path`](/run-instance/configuration/ors/engine/index.md). 

### Elevation Cache

If elevation is activated in the configuration, openrouteservice will download and cache the elevation data tiles in a directory
which can be configured with the property [`ors.engine.elevation.cache_path`](/run-instance/configuration/ors/engine/index.md).

### Logs

Log output is written to auto-rotated log files.
See chapter [logging](/run-instance/configuration/spring/logging.md) for details on configuring the location of log files.




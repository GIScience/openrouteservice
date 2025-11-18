# Data: Input and output folders and files

## Input Files

The most important input file is the config file.
In the [Configuration](configuration/index.md) section you find everything about configuration options and files.

Openrouteservice makes use of public open-source data. To generate the best routes, a number of different datasets are used.

### OSM Data
The base data used for the road network and related information (road type, access restrictions etc.) is [OpenStreetMap](https://openstreetmap.org) (OSM). This dataset is a free and open dataset that can be edited by anyone.  
You can download the latest OSM dataset from https://planet.openstreetmap.org/ or regional extracts from http://download.geofabrik.de/.

The OSM file to be used needs be configured with the property [
`ors.engine.profile_default.build.source_file`](configuration/engine/index.md).

### Elevation
The data used for elevation are [SRTM](http://srtm.csi.cgiar.org/) and [GMTED](https://www.usgs.gov/coastal-changes-and-impacts/gmted2010).  

Configuration: [`ors.engine.elevation`](configuration/engine/elevation.md)

### Population
When requesting isochrones, you can also request to get population data for the isochrone areas. The data used for this is the [Global Human Settlement Layer (GHSL)](https://ghsl.jrc.ec.europa.eu/ghs_pop2023.php) from the European Commission.
Note, that while the dataset was published in 2023, the most recent data contained is from 2020. This is used by openrouteservice at a resolution of 100m.

### Borders
Data relating to the avoid borders feature is derived from administrative boundaries in OpenStreetMap. Information about open borders is obtained from [Wikipedia](https://en.wikipedia.org/wiki/Open_border).

Configuration parameters: [
`ors.engine.profiles.<PROFILE-NAME>.ext_storages.Borders`](configuration/engine/profiles/build.md#borders)

To ensure `avoid_borders` functions correctly, the following are required.

##### CSV headers & column order

- The loader skips the first row (header).
- Columns are positional, i.e. header labels are not used to map the fields
  - `ids` csv file consists of three columns: `id`, `name`, and `name:en`. The `name` field must match the `name` property in the `boundaries` geojson polygons file.
  - `openborders` csv file consists of rows containing pairs of country names matching those from the `name:en` field in the `ids` csv file.

#### Formatting & encoding
- Comma-separated, double-quote fields when needed (e.g. names with commas).
- Use UTF-8 and trim whitespace.
- Names are compared by exact string equality (case-sensitive).

Please note that any change to border files requires a full graph rebuild.

::: warning Performance considerations
Large or highly detailed polygons can make border processing slow or stall. For extensive regions, border geometries should be simplified or subdivided into smaller polygons. See [this tutorial](example-setups/countries_grid.md) for guidance.
:::

### GTFS
The public transport profile integrates [GTFS](https://developers.google.com/transit/gtfs) data for the public transit part. GTFS feeds can be obtained e.g. from sites like https://gtfs.de/ (for Germany), or from local public transport operators.

Configuration parameters: [`ors.engine.profiles.<PROFILE-NAME>.gtfs_file`](configuration/engine/profiles/build.md)

### Green & Quiet
The data used to identify green and quiet routes were derived from research projects in the GIScience research group at Heidelberg University. 
More information about these can be found on the GIScience news blog [here](https://giscienceblog.uni-heidelberg.de/2017/07/03/healthy-routing-prefering-green-areas-added-to-openrouteserviceorg/) and [here](http://giscienceblog.uni-heidelberg.de/2017/07/10/reducing-stress-by-avoiding-noise-with-quiet-routing-in-openrouteservice/)

Configuration parameters: [
`ors.engine.profiles.<PROFILE-NAME>.ext_storages`](configuration/engine/profiles/build.md#ext_storages)


## Output Files

Openrouteservice produces output files of three types, for which the paths can be configured. The directories these paths point to need to be *writable*. 

### Graphs

openrouteservice reads the input data and computes a graph for each enabled routing profile. 

The root directory for the graphs can be configured with the configuration property [
`ors.engine.profile_default.graph_path`](configuration/engine/index.md).

### Elevation Cache

If elevation is activated in the configuration, openrouteservice will download and cache the elevation data tiles in a directory
which can be configured with the property [`ors.engine.elevation.cache_path`](configuration/engine/index.md).

### Logs

Log output is written to auto-rotated log files.
See chapter [logging](configuration/logging.md) for details on configuring the location of log files.

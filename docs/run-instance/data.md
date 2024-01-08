# Data
Openrouteservice makes use of public open-source data. To generate the best routes, a number of different datasets are used.

## Base Data
The base data used for the road network and related information (road type, access restrictions etc.) is [OpenStreetMap](https://openstreetmap.org) (OSM). This dataset is a free and open dataset that can be edited by anyone.  
You can download the latest OSM dataset from https://planet.openstreetmap.org/ or regional extracts from http://download.geofabrik.de/.

## Elevation
The data used for elevation are [SRTM](http://srtm.csi.cgiar.org/) and [GMTED](https://www.usgs.gov/coastal-changes-and-impacts/gmted2010).  

## Population
When requesting isochrones, you can also request to get population data for the isochrone areas. The data used for this is the [Global Human Settlement Layer (GHSL)](https://ghsl.jrc.ec.europa.eu/ghs_pop2023.php) from the European Commission. openrouteservice uses the 2020 data at a resolution of 100m.

## Borders
Data relating to the avoid borders features is derived from administrative boundaries features in OpenStreetMap. Information about open borders is obtained from [Wikipedia](https://en.wikipedia.org/wiki/Open_border).

## Green & Quiet
The data used to identify green and quiet routes were derived from research projects in the GIScience research group at Heidelberg University. More information about these can be found on the GIScience news blog [here](http://k1z.blog.uni-heidelberg.de/2017/07/03/healthy-routing-prefering-green-areas-added-to-openrouteserviceorg/) and [here](http://k1z.blog.uni-heidelberg.de/2017/07/10/reducing-stress-by-avoiding-noise-with-quiet-routing-in-openrouteservice/)

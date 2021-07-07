---
title: Data
nav_order: 3
has_toc: false
---

# Data
openrouteservice makes use of public opensource data. To generate the best routes, a number of different datasets are used.

## Base Data
The base data used for the road network and related information (road type, access restrictions etc.) is [OpenStreetMap](https://openstreetmap.org). This dataset is a free and open dataset that can be edited by anyone.

## Elevation
The data used for elevation are [SRTM](http://srtm.csi.cgiar.org/) and GMTED.  

## Population
When requesting isochrones, you can also request to get population data for the isochrone areas. The data used for this is the [Global Human Settlement Layer (GHSL)](https://data.jrc.ec.europa.eu/dataset/jrc-ghsl-ghs_pop_gpw4_globe_r2015a) from the European Commission. openrouteservice uses the 2015 data at a resolution of 250m.

## Borders
Data relating to the avoid borders features is derived from administrative boundaries features in OpenStreetMap. Information about open borders is obtained from [Wikipedia](https://en.wikipedia.org/wiki/Open_border).

## Green & Quiet
The data used to identify green and quite routes were derived from research
projects in the GIScience research group at Heidelberg University. More
information about these can be found on the GIScience news blog
[here](http://k1z.blog.uni-heidelberg.de/2017/07/03/healthy-routing-prefering-green-areas-added-to-openrouteserviceorg/)
and
[here](http://k1z.blog.uni-heidelberg.de/2017/07/10/reducing-stress-by-avoiding-noise-with-quiet-routing-in-openrouteservice/)

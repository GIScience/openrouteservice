# Matching Endpoint

The matching endpoint can be used to match point, linear and polygonal spatial features to the edges of the routing graph representing the street network for a specific means of transportation.

The matching of point geometries is performed by snapping to the nearest edge in the graph, similar to the [snapping endpoint](../snapping/index.md). Linear geometries are matched using a Hidden Markov Model (HMM) based map matching algorithm. Polygon geometries are matched by intersecting them with the edges of the routing graph.

The endpoint returns a JSON containing a list of matched edge ids.

The routing profile has to be specified as path parameter. 
The list of geometric features for matching is provided in a GeoJSON `FeatureCollection` object.

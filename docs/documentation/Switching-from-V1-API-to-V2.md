---
nav_exclude: true
parent: Documentation
title: Switching from V1 API to V2
---

# Moving from V1 API to V2

Soon (30th October, 2020), the V1 openrouteservice API will be getting switched off, so before then it is important that you update your queries to reflect this change. The V2 API offers some great benefits over the V1 API including:
* **All request information is sent in the request body rather than needing to be encoded in the URL. This makes writing and debugging of queries far easier!**
* **The URL is structured in a way that makes it easier to request different response data types and see what profile you are querying**

So how do you switch from V1 to V2 API? The biggest difference is that the main request information needs to go in the body of the request, and the request made as a POST method. The body itself is a nice JSON format, and makes use of true arrays rather than the piped (|) list in the V1 GET method. Also, the URL itself has been modified to be the format of:
`https://api.openrouteservice.org/v2/{endpoint}/{profile}`
where `{endpoint}` is one of `directions`, `isochrones`, or `matrix`, and `{profile}` is the profile you want to get the route for. In addition, the `directions` endpoint can also have the response data format added after the profile, for example `https://api.openrouteservice.org/v2/directions/driving-car/{response-format}` where `{response-format}` is one of `json`, `geojson`, or `gpx`.

**Please note that it is only the `directions`, `isochrones`, and `matrix` endpoints that use the V2 API. All other endpoints (e.g. geocoding) remain the same as before!**

So that's the basic changes, so now lets give some examples of how to change the requests.

## Directions

Let's start with a basic directions request for the car profile, where you have a start, end, and via point, you want to avoid highways, and you want to avoid France. In the V1 API, your request would be:

`https://api.openrouteservice.org/directions?api_key=xxx&coordinates=8.684692,49.40025|7.849731,48.046874|7.36084,49.231947&profile=driving-car&options={"avoid_features":"highways","avoid_countries":"70|193"}&geometry_format=geojson`

or in it's encoded form

`https://api.openrouteservice.org/directions?api_key=xxx&coordinates=8.684692,49.40025%7C7.849731,48.046874%7C7.36084,49.231947&profile=driving-car&options=%7B%22avoid_features%22:%22highways%22,%22%22avoid_countries%22%3A%2270%7C193%22%7D&geometry_format=geojson`

Using the V2 API, the same request would be as follows:

`https://api.openrouteservice.org/v2/directions/driving-car/geojson?api_key=xxx`

with the POST body being:

```json
{
	"coordinates": [
		[8.684692,49.40025],
		[7.849731,48.046874],
		[7.36084,49.231947]
	],
	"options": {
		"avoid_countries": ["FRA","CHE"],
		"avoid_features": ["highways"]
	}
}
```

As you can see, that is much easier to read!

## Isochrones

For the Isochrones endpoint, to request pedestrian isochrones for 2 locations up to 60 minutes duration with equal intervals at 10 minutes, in V1 API you would send the following request:

`https://api.openrouteservice.org/isochrones?api_key=5b3ce3597851110001cf62480fd03f0ff4c14a33a72cc528290f2823&profile=foot-walking&locations=40.38939,%2056.148562&range=3600&range_type=time&interval=600`

In V2 API, it would be done as follows (again as a POST):

`https://api.openrouteservice.org/v2/isochrones/foot-walking?api_key=xxx`

with the following body:

```json
{
	"locations": [
		[8.681495,49.41461],
		[8.686507,49.41943]
	],
	"range": [3600],
	"interval": 360,
	"range_type": "time"
}
```

## Matrix

Finally, for a HGV Matrix request using 4 locations, where the last 2 in the list are the destinations, returning distance and duration, and the distances being in kilometres, in V1 you would request:

`https://api.openrouteservice.org/matrix?api_key=xxx&profile=driving-hgv&locations=9.970093,48.477473|9.207916,49.153868|37.573242,55.801281|115.663757,38.106467&destinations=2,3&metrics=distance|duration&units=km`

or in its encoded form:

`https://api.openrouteservice.org/matrix?api_key=xxx&profile=driving-hgv&locations=9.970093,48.477473%7C9.207916,49.153868%7C37.573242,55.801281%7C115.663757,38.106467&destinations=2,3&metrics=distance%7Cduration&units=km`

For the same request in V2, you would use the following POST request:

`https://api.openrouteservice.org/v2/matrix/driving-hgv?api_key=xxx`

```json
{
	"locations": [
		[9.70093,48.477473],
		[9.207916,49.153868],
		[37.573242,55.801281],
		[115.663757,38.106467]
	],
	"destinations": [2,3],
	"metrics": ["distance","duration"],
	"units": "km"
}
```

So that's basically how to convert your V1 API requests into V2 API requests. As you can see, things are a lot easier to read in the newer API, and there are some additional functionalities which are not available in the V1 API.

You can get a better understanding of the different requests and how to build them in our [interactive documentation](https://openrouteservice.org/dev/#/api-docs/).

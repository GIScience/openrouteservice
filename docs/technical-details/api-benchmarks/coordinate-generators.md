# Coordinate Generation Tools

The openrouteservice provides tools for generating test coordinates that can be used for benchmarking, testing, and development purposes. These tools allow you to create realistic coordinates that are compatible with the openrouteservice API and suited for different routing profiles.

## Prerequisites

To use the coordinate generation tools, you need:

- Java 17+
- Maven
- An active openrouteservice instance (local or remote)

## Overview

The coordinate generation tools are part of the `ors-benchmark` module and include:

1. **Route Generator** - Creates origin-destination coordinate pairs suitable for routing requests
2. **Snapping Generator** - Creates coordinates that are snapped to the nearest point on the road network

These tools are particularly useful for:

- Creating test datasets for benchmarking performance
- Generating realistic coordinate sets for load testing
- Preparing sample data for demonstrations or tutorials

## Route Generator

The Route Generator creates pairs of coordinates (origin and destination) that are suitable for routing requests based on specified criteria such as distance constraints and routing profiles.

### Route Generator Working Principles

The tool generates random coordinates within a specified bounding box and then uses the openrouteservice API to verify that a valid route exists between each pair of coordinates. This ensures that the generated coordinates are usable for route testing and provide working data for the specified routing profiles.

### Route Generator Options

| Option                | Description                                                                                                      | Default                     |
|-----------------------|------------------------------------------------------------------------------------------------------------------|-----------------------------|
| `-n, --num-routes`    | Number of routes to generate.                                                                                    | (required)                  |
| `-e, --extent`        | Bounding box (minLon,minLat,maxLon,maxLat).  Use <https://boundingbox.klokantech.com/> to generate your extents. | (required).                 |
| `-p, --profiles`      | Comma-separated routing profiles.                                                                                | (required)                  |
| `-u, --url`           | ORS API base URL.                                                                                                | <http://localhost:8080/ors> |
| `-o, --output`        | Output CSV file path.                                                                                            | route_coordinates.csv       |
| `-d, --min-distance`  | Minimum distance between start and endpoint in an a-to-b routing pair in meters. This is valid for all profiles. | 1                           |
| `-m, --max-distances` | Maximum distances in meters **per profile** between the start and endpoint.                                      | (none)                      |
| `-t, --threads`       | Number of threads to use.                                                                                        | Available processors        |
| `-sr, --snap-radius`  | Search radius in meters for coordinate snapping.                                                                 | 1000                        |

### Route Generator Examples

#### Basic Route Example

Generate 100 routes for the driving-car profile with a minimum distance of 1000m:

```bash
./mvnw clean compile exec:java -Dexec.cleanupDaemonThreads=false -pl 'ors-benchmark' \
  -Dexec.mainClass="org.heigit.ors.coordinates_generator.RouteGeneratorApp" \
  -Dexec.args="\
  -n 100 \
  -e 8.6,49.3,8.7,49.4 \
  -p driving-car \
  -d 1000 \
  -u http://localhost:8080/ors \
  -o routes.csv"
```

#### Multiple Profiles with Different Distance Constraints

Generate 50 routes for both driving-car and cycling-regular profiles with different maximum distances:

```bash
./mvnw clean compile exec:java -Dexec.cleanupDaemonThreads=false -pl 'ors-benchmark' \
  -Dexec.mainClass="org.heigit.ors.coordinates_generator.RouteGeneratorApp" \
  -Dexec.args="\
  --num-routes 50 \
  --extent 8.681495,49.411721,8.695485,49.419365 \
  --profiles driving-car,cycling-regular \
  --min-distance 2000 \
  --max-distances 5000,3000 \
  --threads 4 \
  --snap-radius 1500 \
  --url http://localhost:8080/ors \
  --output routes.csv"
```

In this example, driving-car routes will have a maximum distance of 5000m, while cycling-regular routes will have a maximum distance of 3000m.

## Snapping Generator

The Snapping Generator creates coordinates that are snapped to the nearest point on the road network. This is useful when you need coordinates that are guaranteed to be on the road network.

### Snapping Generator Working Principles

The tool generates random coordinates within a specified bounding box and then uses the openrouteservice API to snap these points to the nearest road segment. Each point is snapped using the specified routing profile(s), ensuring the coordinates are usable for that profile's routing network.

### Snapping Generator Options

| Option | Description | Default |
|--------|-------------|---------|
| `-n, --num-points` | Number of points to generate per profile. | (required) |
| `-e, --extent` | Bounding box (minLon,minLat,maxLon,maxLat). | (required) |
| `-p, --profiles` | Comma-separated list of routing profiles. | (required) |
| `-r, --radius` | Search radius in meters. | 350 |
| `-u, --url` | ORS API base URL. | <http://localhost:8080/ors> |
| `-o, --output` | Output CSV file path. | snapped_coordinates.csv |

### Snapping Generator Examples

#### Basic Snapping Example

Generate 100 snapped points for the driving-car profile with a search radius of 500m:

```bash
./mvnw clean compile exec:java -Dexec.cleanupDaemonThreads=false -pl 'ors-benchmark' \
  -Dexec.mainClass="org.heigit.ors.coordinates_generator.SnappingGeneratorApp" \
  -Dexec.args="\
  -n 100 \
  -e 8.6,49.3,8.7,49.4 \
  -p driving-car \
  -r 500 \
  -u http://localhost:8080/ors \
  -o snapped.csv"
```

#### Snapping with Multiple Profiles

Generate 50 snapped points for both driving-car and cycling-regular profiles:

```bash
./mvnw clean compile exec:java -Dexec.cleanupDaemonThreads=false -pl 'ors-benchmark' \
  -Dexec.mainClass="org.heigit.ors.coordinates_generator.SnappingGeneratorApp" \
  -Dexec.args="\
  --num-points 50 \
  --extent 8.681495,49.411721,8.695485,49.419365 \
  --profiles driving-car,cycling-regular \
  --radius 250 \
  --url http://localhost:8080/ors \
  --output snapped.csv"
```

## Output Format

### Route Generator Output

The Route Generator produces a CSV file with columns:

```csv
start_longitude,start_latitude,end_longitude,end_latitude,profile
```

### Snapping Generator Output

The Snapping Generator produces a CSV file with columns:

```csv
longitude,latitude,profile
```

## Performance Tips

1. **Adjust the number of threads**: Increase for faster generation, but be mindful of system resources. Double the number of available cores is still yielding good performance.
2. **Set realistic distance constraints**: Very large min/max distances may result in fewer valid routes. This highly depends on the chosen profiles. E.g. long distances for car take considerably less calculation time than for pedestrian.
3. **Use a local ORS instance**: For generating large datasets, a local instance reduces network latency and saves your API quota.

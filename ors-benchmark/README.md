# ORS Benchmark Tools

Collection of tools for generating test coordinates using OpenRouteService APIs.

## Coordinate Generator

A tool to generate coordinate pairs within specified distance constraints using the ORS Matrix API.

### Building

```bash
mvn clean compile
```

### Point Generator

### Route Generator

A command-line tool for generating pairs of coordinates suitable for route testing based on specified criteria.

#### Route Generator Usage

Options:

- `-n, --num-routes <value>`: Number of routes to generate (required)
- `-e, --extent <minLon> <minLat> <maxLon> <maxLat>`: Bounding box for coordinate generation (required)
- `-p, --profile <value>`: Routing profile to use (e.g., driving-car) (required)
- `-u, --url <value>`: ORS API base URL (default: http://localhost:8080/ors)
- `-o, --output <file>`: Output CSV file path (default: route_coordinates.csv)
- `-d, --min-distance <value>`: Minimum distance between coordinates in meters (default: 0)
- `-h, --help`: Show help message

Example:

```bash
mvn clean compile exec:java -pl 'ors-benchmark' \
  -Dexec.mainClass="org.heigit.ors.generators.CoordinateGeneratorRoute" \
  -Dexec.args="\
  -n 100 \
  -e 8.6 49.3 8.7 49.4 \
  -p driving-car \
  -d 1000 \
  -o routes.csv"
```

Example with long parameters:

```bash
mvn clean compile exec:java -pl 'ors-benchmark' \
  -Dexec.mainClass="org.heigit.ors.generators.CoordinateGeneratorRoute" \
  -Dexec.args="\
  --num-routes 50 \
  --extent 8.681495 49.411721 8.695485 49.419365 \
  --profile driving-car \
  --url http://localhost:8080/ors \
  --min-distance 2000 \
  --output heidelberg_routes.csv"
```

### Snapping Generator

A command-line tool for generating coordinates and snapping them to the nearest road network points.

#### Point Generator Usage

Options:

- `-n, --num-points <value>`: Number of points to generate (required)
- `-e, --extent <minLon> <minLat> <maxLon> <maxLat>`: Bounding box for coordinate generation (required)
- `-p, --profile <value>`: Routing profile to use (e.g., driving-car) (required)
- `-r, --radius <value>`: Search radius in meters (default: 350)
- `-u, --url <value>`: ORS API base URL (default: http://localhost:8080/ors)
- `-o, --output <file>`: Output CSV file path (default: snapped_coordinates.csv)
- `-h, --help`: Show help message

Example:

```bash
mvn clean compile exec:java -pl 'ors-benchmark' \
  -Dexec.mainClass="org.heigit.ors.generators.CoordinateGeneratorSnapping" \
  -Dexec.args="\
  -n 100 \
  -e 8.6 49.3 8.7 49.4 \
  -p driving-car \
  -r 500 \
  -o snapped.csv"
```

Example with long parameters:

```bash
mvn clean compile exec:java -pl 'ors-benchmark' \
  -Dexec.mainClass="org.heigit.ors.generators.CoordinateGeneratorSnapping" \
  -Dexec.args="\
  --num-points 50 \
  --extent 8.681495 49.411721 8.695485 49.419365 \
  --profile driving-car \
  --radius 250 \
  --url http://localhost:8080/ors \
  --output heidelberg_snapped.csv"
```

### Isochrones Load Test

A Gatling-based load test for the ORS Isochrones API.

#### Isochrones Load Test Usage

Options:

- `source_file`: CSV file containing coordinates (required)
- `base_url`: ORS API base URL (default: http://localhost:8082/ors)
- `api_key`: API key for authentication
- `profile`: Routing profile (default: driving-car)
- `range`: Isochrone range in meters (default: 300)
- `field_lon`: CSV field name for longitude (default: longitude)
- `field_lat`: CSV field name for latitude (default: latitude)
- `calls`: Number of API calls to make (default: 100)
- `query_size`: Number of points per request (default: 5)
- `ramp_time`: Duration for ramping up users in minutes (default: 1)

Example:

```bash
mvn -pl 'ors-benchmark' gatling:test \
  -Dsource_file='points_berlin_cycling-regular_10000.csv' \
  -Dbase_url='http://localhost:8080/ors' \
  -Dcalls=2000 \
  -Drange=1000
```

Example with additional parameters:

```bash
mvn -pl 'ors-benchmark' gatling:test \
  -Dsource_file='heidelberg_points.csv' \
  -Dbase_url='http://localhost:8080/ors' \
  -Dapi_key='your-api-key' \
  -Dprofile='cycling-regular' \
  -Drange=2000 \
  -Dcalls=1000 \
  -Dquery_size=10 \
  -Dramp_time=5 \
  -Dfield_lon='lon' \
  -Dfield_lat='lat'
```

The test will generate a Gatling report with detailed performance metrics after completion.

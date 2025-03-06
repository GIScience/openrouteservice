# ORS Benchmark Tools

Collection of tools for generating test coordinates and performing load tests using OpenRouteService APIs.

## Building

```bash
mvn clean compile
```

### Route Generator

A command-line tool for generating pairs of coordinates suitable for route testing based on specified criteria.

#### Route Generator Usage

Options:

- `-n, --num-routes <value>`: Number of routes to generate (required)
- `-e, --extent <value>`: Bounding box for coordinate generation (minLon,minLat,maxLon,maxLat) (required)
- `-p, --profiles <value>`: Comma-separated routing profiles (e.g., driving-car,cycling-regular) (required)
- `-u, --url <value>`: ORS API base URL (default: <http://localhost:8080/ors>)
- `-o, --output <file>`: Output CSV file path (default: route_coordinates.csv)
- `-d, --min-distance <value>`: Minimum distance between coordinates in meters (default: 1)
- `-m, --max-distances <values>`: Maximum distances in meters, comma-separated per profile (e.g., 5000,3000)
- `-t, --threads <value>`: Number of threads to use (default: number of available processors)
- `-h, --help`: Show help message

Example with space-separated extent:

```bash
mvn clean compile exec:java -Dexec.cleanupDaemonThreads=false -pl 'ors-benchmark' \
  -Dexec.mainClass="org.heigit.ors.generators.CoordinateGeneratorRoute" \
  -Dexec.args="\
  -n 100 \
  -e 8.6 49.3 8.7 49.4 \
  -p driving-car \
  -d 1000 \
  -o routes.csv"
```

Example with comma-separated extent:

```bash
mvn clean compile exec:java -Dexec.cleanupDaemonThreads=false -pl 'ors-benchmark' \
  -Dexec.mainClass="org.heigit.ors.generators.CoordinateGeneratorRoute" \
  -Dexec.args="\
  -n 100 \
  -e 8.6,49.3,8.7,49.4 \
  -p driving-car \
  -d 1000 \
  -t 4 \
  -o routes.csv"
```

Example with multiple profiles and max distances:

```bash
mvn clean compile exec:java -Dexec.cleanupDaemonThreads=false -pl 'ors-benchmark' \
  -Dexec.mainClass="org.heigit.ors.generators.CoordinateGeneratorRoute" \
  -Dexec.args="\
  --num-routes 50 \
  --extent 8.681495,49.411721,8.695485,49.419365 \
  --profiles driving-car,cycling-regular \
  --url http://localhost:8080/ors \
  --min-distance 2000 \
  --max-distances 5000,3000 \
  --threads 4 \
  --output routes.csv"
```

### Snapping Generator

A command-line tool for generating coordinates and snapping them to the nearest road network points. Supports generating points for multiple routing profiles simultaneously.

#### Snapping Generator Usage

Options:

- `-n, --num-points <value>`: Number of points to generate per profile (required)
- `-e, --extent <value>`: Bounding box for coordinate generation (minLon,minLat,maxLon,maxLat) (required)
- `-p, --profiles <values>`: Comma-separated list of routing profiles (e.g., driving-car,cycling-regular) (required)
- `-r, --radius <value>`: Search radius in meters (default: 350)
- `-u, --url <value>`: ORS API base URL (default: <http://localhost:8080/ors>)
- `-o, --output <file>`: Output CSV file path (default: snapped_coordinates.csv)
- `-h, --help`: Show help message

Example with space-separated extent:

```bash
mvn clean compile exec:java -Dexec.cleanupDaemonThreads=false -pl 'ors-benchmark' \
  -Dexec.mainClass="org.heigit.ors.generators.CoordinateGeneratorSnapping" \
  -Dexec.args="\
  -n 100 \
  -e 8.6 49.3 8.7 49.4 \
  -p driving-car \
  -r 500 \
  -o snapped.csv"
```

Example with comma-separated extent:

```bash
mvn clean compile exec:java -Dexec.cleanupDaemonThreads=false -pl 'ors-benchmark' \
  -Dexec.mainClass="org.heigit.ors.generators.CoordinateGeneratorSnapping" \
  -Dexec.args="\
  -n 100 \
  -e 8.6,49.3,8.7,49.4 \
  -p driving-car \
  -r 500 \
  -o snapped.csv"
```

Example with multiple profiles:

```bash
mvn clean compile exec:java -Dexec.cleanupDaemonThreads=false -pl 'ors-benchmark' \
  -Dexec.mainClass="org.heigit.ors.generators.CoordinateGeneratorSnapping" \
  -Dexec.args="\
  --num-points 50 \
  --extent 8.681495,49.411721,8.695485,49.419365 \
  --profiles driving-car,cycling-regular \
  --radius 250 \
  --url http://localhost:8080/ors \
  --output snapped.csv"
```

### Isochrones Load Test

A Gatling-based load test for the ORS Isochrones API. The test supports batch processing of locations, allowing multiple coordinate pairs to be processed in a single request.

#### Isochrones Load Test Usage

Options:

- `source_files`: Comma-separated list of CSV files containing coordinate pairs
- `base_url`: ORS API base URL (default: <http://localhost:8082/ors>)
- `api_key`: API key for authentication
- `profile`: Routing profile (default: driving-car)
- `range`: Comma-separated list of isochrone ranges in meters (e.g., "300,600,900")
- `field_lon`: CSV field name for longitude column (default: longitude)
- `field_lat`: CSV field name for latitude column (default: latitude)
- `concurrent_users`: Number of concurrent users (default: 1)
- `query_sizes`: Comma-separated list of batch sizes for location processing (e.g., "1,2,4,8")
- `test_unit`: Type of range to test - 'distance' or 'time' (default: distance)
- `parallel_execution`: Run scenarios in parallel or sequential (default: false)

CSV File Format:

```csv
longitude,latitude
8.681495,49.41461
8.686507,49.41943
...
```

Example with batch processing:

```bash
mvn -pl 'ors-benchmark' gatling:test \
  -Dgatling.simulationClass=org.heigit.ors.benchmark.IsochronesLoadTest \
  -Dsource_files='heidelberg_points.csv' \
  -Dbase_url='http://localhost:8080/ors' \
  -Dquery_sizes='1,2,4,8' \
  -Drange='300,600,900' \
  -Dtest_unit='time'
```

This will test the Isochrones API with varying batch sizes, processing 1, 2, 4, and 8 locations per request, using time-based isochrones at 300, 600, and 900 second intervals.

Example with all parameters:

```bash
mvn -pl 'ors-benchmark' gatling:test \
  -Dgatling.simulationClass=org.heigit.ors.benchmark.IsochronesLoadTest \
  -Dsource_files='points.csv' \
  -Dbase_url='http://localhost:8080/ors' \
  -Dprofile='cycling-regular' \
  -Drange=500 \
  -Dconcurrent_users=1 \
  -Dquery_sizes='1,2,4,6,8,10,12,15,20' \
  -Drun_time=60 \
  -Dtest_unit='distance'
```

Example with sequential execution:

```bash
mvn -pl 'ors-benchmark' gatling:test \
  -Dgatling.simulationClass=org.heigit.ors.benchmark.IsochronesLoadTest \
  -Dsource_files='points.csv' \
  -Dquery_sizes='1,2,3,4,5' \
  -Dparallel_execution=false \
  -Drun_time=300
```

Example with multiple source files:

```bash
mvn -pl 'ors-benchmark' gatling:test \
  -Dgatling.simulationClass=org.heigit.ors.benchmark.IsochronesLoadTest \
  -Dsource_files='points1.csv,points2.csv' \
  -Dbase_url='http://localhost:8080/ors' \
  -Dquery_sizes='1,2,3,4,5' \
  -Drun_time=300
```

The test will generate a Gatling report with detailed performance metrics after completion.

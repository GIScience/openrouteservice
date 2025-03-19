# API Benchmarking Tools

The openrouteservice provides tools for benchmarking API performance, allowing you to test the responsiveness, throughput, and scalability of your openrouteservice instance or an external API under various load conditions.

## Prerequisites

To use the API benchmarking tools, you need:

- Java 17+
- Maven
- An active openrouteservice instance (local or remote)
- CSV files with coordinates for testing (can be generated using the [Coordinate Generation Tools](/technical-details/coordinate-generators))

## Overview

The benchmarking tools are part of the `ors-benchmark` module and include:

1. **Isochrones Range Load Test** - Tests the performance of the Isochrones API under various load conditions with range options.
2. **Routing Algorithm Load Test** - Tests the performance of the Directions API with different routing algorithms and profiles.

These tools use the Gatling framework to simulate user load and provide detailed performance metrics that help you:

- Evaluate the performance of your openrouteservice instance
- Identify bottlenecks in your configuration
- Compare different routing algorithms and profiles
- Optimize your setup for specific use cases

## Isochrones Range Load Test

The Isochrones Range Load Test tool allows you to test the performance of the Isochrones API by sending multiple requests with configurable parameters, including batch processing of locations and Range types (time or distance).

### Isochrones Working Principles

The tool reads coordinates from CSV files and creates isochrone requests with varying batch sizes (number of locations per request). It uses the Gatling framework to simulate concurrent users and measure response times, throughput, and other performance metrics.

### Isochrones Options

| Option | Description | Default |
|--------|-------------|---------|
| `source_files` | Comma-separated list of CSV files containing coordinate pairs (required) | (Required) |
| `base_url` | ORS API base URL | <http://localhost:8082/ors> |
| `api_key` | API key for authentication | (none) |
| `profile` | Routing profile | driving-car |
| `range` | Comma-separated list of isochrone ranges in meters or seconds | 300 |
| `field_lon` | CSV field name for longitude column | longitude |
| `field_lat` | CSV field name for latitude column | latitude |
| `concurrent_users` | Number of concurrent users | 1 |
| `query_sizes` | Comma-separated list of batch sizes for location processing | 1 |
| `test_unit` | Type of range to test - 'distance' or 'time' | distance |
| `parallel_execution` | Run scenarios in parallel or sequential | false |

### Isochrones CSV File Format

The Isochrones Range Load Test expects CSV files with the following format:

```csv
longitude,latitude,profile
8.681495,49.41461,driving-car
8.686507,49.41943,driving-car
8.681495,49.41461,cycling-regular
8.686507,49.41943,cycling-regular
...
```

### Isochrones Examples

#### Generating Points for Isochrones Testing

Generate 10,000 points across Germany for various profiles:

```bash
mvn clean compile exec:java -Dexec.cleanupDaemonThreads=false -pl 'ors-benchmark' \
  -Dexec.mainClass="org.heigit.ors.coordinates_generator.SnappingGeneratorApp" \
  -Dexec.args="\
  -n 2500 \
  -e 7.5,47.3,13.5,55.0 \
  -p driving-car,cycling-regular,foot-walking,driving-hgv \
  -r 350 \
  -u http://localhost:8080/ors \
  -o germany_car-foot-bike-hgv_10000_points.csv"
```

#### Isochrones Basic Usage

Test the Isochrones API with the default parameters:

```bash
mvn -pl 'ors-benchmark' gatling:test \
  -Dgatling.simulationClass=org.heigit.ors.benchmark.IsochronesRangeLoadTest \
  -Dsource_files='germany_car-foot-bike-hgv_10000_points.csv' \
  -Dbase_url='http://localhost:8080/ors'
```

#### Testing with Batch Processing

Test the Isochrones API with varying batch sizes, processing 1, 2, 4, and 8 locations per request:

```bash
mvn -pl 'ors-benchmark' gatling:test \
  -Dgatling.simulationClass=org.heigit.ors.benchmark.IsochronesRangeLoadTest \
  -Dsource_files='germany_car-foot-bike-hgv_10000_points.csv' \
  -Dbase_url='http://localhost:8080/ors' \
  -Dquery_sizes='1,2,4,8' \
  -Drange='300,600,900' \
  -Dtest_unit='time'
```

#### Comprehensive Test with All Parameters

Generate points specific to cycling-regular profile:

```bash
mvn -pl 'ors-benchmark' gatling:test \
  -Dgatling.simulationClass=org.heigit.ors.benchmark.IsochronesRangeLoadTest \
  -Dsource_files='germany_car-foot-bike-hgv_10000_points.csv' \
  -Dbase_url='http://localhost:8080/ors' \
  -Dprofile='cycling-regular' \
  -Drange=500 \
  -Dconcurrent_users=1 \
  -Dquery_sizes='1,2,4,6,8,10,12,15,20' \
  -Dtest_unit='distance'
```

## Routing Algorithm Load Test

The Routing Algorithm Load Test tool allows you to test the performance of the Directions API with different routing algorithms and profiles.

### Routing Working Principles

The tool reads origin-destination coordinate pairs from CSV files and sends routing requests to the Directions API. It can test different routing algorithms and profiles, simulating concurrent users and measuring response times, throughput, and other performance metrics.

### Routing Options

| Option | Description | Default |
|--------|-------------|---------|
| `source_files` | Comma-separated list of CSV files containing coordinate pairs (required) | (none) |
| `base_url` | ORS API base URL | <http://localhost:8082/ors> |
| `api_key` | API key for authentication | (none) |
| `modes` | Comma-separated routing modes: algoch, algocore, algolmastar | all |
| `field_start_lon` | CSV field name for start longitude column | start_longitude |
| `field_start_lat` | CSV field name for start latitude column | start_latitude |
| `field_end_lon` | CSV field name for end longitude column | end_longitude |
| `field_end_lat` | CSV field name for end latitude column | end_latitude |
| `concurrent_users` | Number of concurrent users | 1 |
| `parallel_execution` | Run scenarios in parallel or sequential | false |

### Routing CSV File Format

The Routing Algorithm Load Test expects CSV files with the following format:

```csv
start_longitude,start_latitude,end_longitude,end_latitude,profile
8.681495,49.41461,8.686507,49.41943,driving-car
8.686507,49.41943,8.691528,49.41567,driving-car
...
```

### Routing Examples

#### Generate Routes for Routing Tests

Generate 10,000 routes with different distance constraints per profile:

```bash
mvn clean compile exec:java -Dexec.cleanupDaemonThreads=false -pl 'ors-benchmark' \
  -Dexec.mainClass="org.heigit.ors.coordinates_generator.RouteGeneratorApp" \
  -Dexec.args="\
  -n 2500 \
  -e 7.5,47.3,13.5,55.0 \
  -p driving-car,cycling-regular,foot-walking,driving-hgv \
  -d 200 \
  -m 600,10,5,600 \
  -t 8 \
  -u http://localhost:8080/ors \
  -o germany_car600-bike10-foot5-hgv600_10000_routes.csv"
```

#### Routing Basic Usage

Test the Directions API with the default parameters:

```bash
mvn -pl 'ors-benchmark' gatling:test \
  -Dgatling.simulationClass=org.heigit.ors.benchmark.RoutingAlgorithmLoadTest \
  -Dsource_files='germany_car600-bike10-foot5-hgv600_10000_routes.csv' \
  -Dbase_url='http://localhost:9082/ors'
```

#### Testing with Custom Field Names

Generate routes with custom column names:

```bash
mvn clean compile exec:java -Dexec.cleanupDaemonThreads=false -pl 'ors-benchmark' \
  -Dexec.mainClass="org.heigit.ors.coordinates_generator.RouteGeneratorApp" \
  -Dexec.args="\
  -n 10000 \
  -e 7.5,47.3,13.5,55.0 \
  -p driving-car,cycling-regular,foot-walking,driving-hgv \
  -d 200 \
  -m 600,10,5,600 \
  -t 8 \
  -u http://localhost:8080/ors \
  -o germany_car600-bike10-foot5-hgv600_10000_routes.csv"
```

Test the Directions API with custom field names:

```bash
mvn -pl 'ors-benchmark' gatling:test \
  -Dgatling.simulationClass=org.heigit.ors.benchmark.RoutingAlgorithmLoadTest \
  -Dsource_files='germany_car600-bike10-foot5-hgv600_10000_routes.csv' \
  -Dfield_start_lon='from_lon' \
  -Dfield_start_lat='from_lat' \
  -Dfield_end_lon='to_lon' \
  -Dfield_end_lat='to_lat' \
  -Dmodes='algolmastar'
```

#### Testing Multiple Routing Algorithms

Test the Directions API with multiple routing algorithms:

```bash
mvn -pl 'ors-benchmark' gatling:test \
  -Dgatling.simulationClass=org.heigit.ors.benchmark.RoutingAlgorithmLoadTest \
  -Dsource_files='germany_car600-bike10-foot5-hgv600_10000_routes.csv' \
  -Dparallel_execution=false \
  -Dmodes='algoch,algocore'
```

## Performance Testing Best Practices

1. **Start with a baseline**: Run tests on a known, stable configuration to establish a baseline for comparison.
2. **Incremental load**: Start with low concurrency and gradually increase to find the breaking point.
3. **Realistic scenarios**: Use realistic data and request patterns based on your expected usage.
4. **Steady state**: Allow the system to reach a steady state before drawing conclusions.
5. **Isolate variables**: Change only one parameter at a time to understand its impact.
6. **Test repeatedly**: Multiple test runs will give more reliable results.
7. **Monitor resources**: Watch CPU, memory, disk I/O, and network usage during tests.

## Troubleshooting

- **Out of Memory Errors**: Increase the JVM heap size with `-Xmx` parameter.
- **Connection Timeouts**: Check network connectivity and firewall settings.
- **Rate Limiting**: If using a remote API, be aware of rate limiting policies.
- **CSV File Issues**: Ensure CSV files are properly formatted with no BOM markers.

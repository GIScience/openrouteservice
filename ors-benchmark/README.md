# ORS Benchmark Tools

## Coordinate Generator

A tool to generate coordinate pairs within specified distance constraints using the ORS Matrix API.

### Building

```bash
mvn clean compile
```

### Usage

Run the tool using Maven from the parent project directory:

```bash
mvn compile exec:java -pl 'ors-benchmark' \
  -Dexec.mainClass="org.heigit.ors.benchmark.CoordinateGenerator" \
  -Dexec.args="[options]"
```

### Options

- `-n, --num-points <value>` : Number of coordinate pairs to generate (required)
- `-e, --extent <minLon> <minLat> <maxLon> <maxLat>` : Bounding box for coordinates (required)
- `-d, --distance <min> <max>` : Distance range in meters (required)
- `-m, --max-attempts <value>` : Maximum number of generation attempts (default: 1000)
- `-p, --profile <value>` : Routing profile (e.g., driving-car) (required)
- `-u, --url <value>` : ORS API base URL (default: http://localhost:8080/ors)
- `-o, --output <file>` : Output CSV file path (default: coordinates.csv)
- `-h, --help` : Show help message

### Example

Generate 100 coordinate pairs in Heidelberg area with distances between 100 and 5000 meters:

```bash
mvn compile exec:java -pl 'ors-benchmark' \
  -Dexec.mainClass="org.heigit.ors.benchmark.CoordinateGenerator" \
  -Dexec.args="--num-points 100 \
  --extent 8.573179 49.352003 8.794049 49.459693 \
  --distance 100 5000 \
  --max-attempts 100 \
  --profile driving-car \
  --url http://localhost:8082/ors \
  --output output.csv"
```

### Output Format

The tool generates a CSV file with columns:
- `from_lon`: Starting point longitude
- `from_lat`: Starting point latitude
- `to_lon`: Destination point longitude
- `to_lat`: Destination point latitude

### Note for ORS Cloud API Users

When using the openrouteservice.org API, set your API key as an environment variable:

```bash
export ORS_API_KEY=your_api_key_here
```

Or pass it as a system property:

```bash
mvn compile exec:java -pl 'ors-benchmark' \
  -DORS_API_KEY=your_api_key_here \
  -Dexec.mainClass="org.heigit.ors.benchmark.CoordinateGenerator" \
  -Dexec.args="--num-points 100 \
  --extent 8.573179 49.352003 8.794049 49.459693 \
  --distance 100 5000 \
  --max-attempts 100 \
  --profile driving-car \
  --url https://api.openrouteservice.org \
  --output cloud_output.csv"
```

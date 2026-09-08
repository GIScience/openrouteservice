# CSV extra info

Generic extra information loaded from a CSV file at graph-build time.

This feature is experimental and is **not** available on the public openrouteservice hosted by HeiGIT. You need a self-hosted instance with the `Csv` extended storage enabled.

## Enable at graph build

Add a `Csv` entry under `ors.engine.profiles.<PROFILE-NAME>.build.ext_storages` and point `filepath` at your CSV file:

```yaml
ors:
  engine:
    profiles:
      foot-walking:
        build:
          ext_storages:
            Csv:
              filepath: /path/to/extra.csv
```

Graphs must be rebuilt after changing the file.

See also [`ext_storages.Csv`](/run-instance/configuration/engine/profiles/build.md#csv).

## CSV file format

The first row is a header. The first column is the OSM way id. Every later column is a named value that can be requested independently.

```csv
osm_id,heat,greenness
4084881,0.33,0.84
4280150,0.43,0.93
```

- Values are floating-point numbers in `[-1.0, 1.0]`. They are stored as integers `value * 100` (so `-100` to `100`).
- Ways that are not in the file get the default stored value `50` (that is `0.50`).
- Column names come from the header (everything after the id column). They are case-sensitive.

## Request extra info

Ask for `csv` in `extra_info` and name the column with `options.profile_params.weightings.csv_column`:

```json
{
  "coordinates": [[8.681495, 49.41461], [8.686507, 49.41943]],
  "extra_info": ["csv"],
  "options": {
    "profile_params": {
      "weightings": {
        "csv_column": "heat",
        "csv_factor": 0
      }
    }
  }
}
```

`csv_column` must match a header name. `csv_factor` is optional; set it to `0` if you only want the extra info and do not want the column to influence the route. Values between `0` and `1` use the column as a heat-stress-style weighting.

The values appear in the directions response under

```jsonpath
$.routes[*].extras.csv
```

with the same `values` / `summary` structure as other extra info. The third number of each `values` triple is the stored integer (`original_float * 100`).

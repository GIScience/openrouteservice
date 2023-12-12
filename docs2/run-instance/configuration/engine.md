# Engine properties 

The `engine` block consists of the following properties.

| key                         | type    | description                                      | default value |
|-----------------------------|---------|--------------------------------------------------|---------------|
| ors.engine.init_threads     | number  |                                                  |               |
| ors.engine.preparation_mode | boolean |                                                  |               |
| ors.engine.source_file      | string  |                                                  |               |
| ors.engine.graphs_root_path | string  |                                                  |               |
| ors.engine.elevation        | object  | See [elevation properties](#elevationproperties) |               |
| ors.engine.profile_default  | object  | Described in [profile properties](profiles)      |               |
| ors.engine.profiles         | object  | Described in [profile properties](profiles)      |               |

## Elevation properties

Elevation settings `ors.engine.elevation` comprise the following properties.

| key          | type    | description | default value |
|--------------|---------|-------------|---------------|
| preprocessed | boolean |             |               |
| cache_clear  | boolean |             |               |
| provider     | string  |             |               |
| cache_path   | string  |             |               |
| data_access  | string  |             |               |


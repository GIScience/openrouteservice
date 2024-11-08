# `ors.engine.profiles`

The `profiles` object is a map containing key-object-pairs for each profile you want to use. openrouteservice can
simultaneously run a number of different profiles, each with its own set of parameters, limited by the available memory
on the system.

Each profile has a *profile name* (the key in this map), which is used as a reference in the endpoints and also
determines the directory name where the created graph data is stored. Note that the profile name can be chosen freely
but cannot contain special characters that cannot be used for directory
names on your operating system.

Each profile corresponds to a *flag encoder* specified by the `encoder_name` property, which determines how OSM data is
translated into a routing graph during graph building. Specifically, the encoder defines which OSM ways are translated
into graph edges (depending on the OSM tags), how travel speeds are estimated for each edge, and which information from
other tags are stored. The provided flag encoders correspond to a specific vehicle type and must be set for each
profile.

| Possible values for `encoder_name` |
|------------------------------------|
| `driving-car`                      | 
| `driving-hgv`                      | 
| `cycling-regular`                  | 
| `cycling-mountain`                 | 
| `cycling-road`                     | 
| `cycling-electric`                 | 
| `foot-walking`                     | 
| `foot-hiking`                      | 
| `wheelchair`                       | 
| `public-transport` (experimental)  | 

For each flag encoder there is an example setup provided by default, each coming with a recommended sensible set of
configuration options. These profiles have the same names (map keys) as their corresponding flag encoders. Therefore,
you can activate e.g. the profile for `driving-car` with recommended settings by simply setting
`ors.engine.profiles.driving-car.enabled=true`. You can further customize such a profile by overriding the default
settings, but make sure you know what settings are provided by looking at
the [default configuration](../../index.md#configuration-defaults-output).

The `ors.engine.profile_default` object is used to define default values for all profiles. It takes the same properties
as the `profiles` object, but these settings are applied to all profiles unless they are overridden by the specific
profile settings. By setting `ors.engine.profile_default.enabled=true` you can activate all example profiles (one for
each flag encoder) with recommended settings.

::: warning
The predefined example profiles' settings override settings that you specify in the `ors.engine.profile_default`! To
avoid this, use a custom name to define your profile.
:::

| key          | type    | description                                                                                                                                                           | default value |
|--------------|---------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|
| enabled      | boolean | Enables or disables the profile across openrouteservice endpoints                                                                                                     | `false`       |
| encoder_name | string  | Encoder name used for this profile. Possible values are restricted to those in the table above!                                                                       | _NA_          |
| graph_path   | string  | The root path to a directory for storing graphs. Defaults to `graphs`. For each profile a subdirectory with the same name as the profile name is created              | `graphs`      |
| build        | object  | Parameters for the [graph building phase](build.md)                                                                                                                   |               |
| repo         | object  | Parameters regarding [graph repository reference](repo.md) used to download pre-calculated graphs with the [graph repo client](/technical-details/graph-repo-client/) |               |
| service      | object  | Parameters required when running the [service](service.md) that are specific to each profile                                                                          |               |

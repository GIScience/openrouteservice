# Configuration

The configuration of your own openrouteservice instance is done in a YAML configuration file. 

In the past openrouteservice was configured [via JSON file](./json). This configuration method has been **deprecated** and will be eventually removed, therefore we strongly discourage you from using it.

## File location

There are two (optional) ways for you to provide openrouteservice the location of a configuration file:
- Program argument
  ```shell 
  java ors.jar /path/to/ors-config.yml
  ```
- Environment variable `ORS_CONFIG_LOCATION`
  ```shell 
  export ORS_CONFIG_LOCATION=/path/to/ors-config.yml
  java ors.jar 
  ```
  
If no configuration file is provided in the ways mentioned above, openrouteservice will look for a configuration file at multiple possible locations in the following order and take the first available file. 
- Current working directory, i.e. `./ors-config.yml`
- User configuration directory, i.e. `~/.config/openrouteservice/ors-config.yml`
- Global configuration directory, i.e. `/etc/openrouteservice/ors-config.yml`

At program start openrouteservice reports which configuration file was loaded. 

## File content

You can find an example file with most available configuration options [here](https://github.com/GIScience/openrouteservice/blob/main/docker-compose.yml).

At the very least, openrouteservice needs the configuration to contain an enabled [profile](profiles) and the reference to an [OSM data file](../data) to run properly. Therefore, the minimal valid content of such a file would be e.g.:

```yaml
ors:
  engine:
    source_file: ./osm_file.pbf
    profiles: 
      car: 
        enabled: true
```

## Available properties

The properties are organized in a hierarchical structure, with the following ones at top level.

- [spring-specific](./spring) settings, such as `server` and `spring`
- settings relating to [logging](./logging)
- openrouteservice settings organized under the following four blocks: 

| key                                     | description                                                                 |
|-----------------------------------------|-----------------------------------------------------------------------------|
| [ors.endpoints](./endpoints-and-limits) | Settings required at runtime to process API requests.                       |
| [ors.engine](./engine)                  | Settings required at graph-build time during startup.                       |
| [ors.cors](./cors)                      | CORS settings for the **openrouteservice** API.                             |
| [ors.messages](./messages)              | System messages that can be sent with API responses following simple rules. |

## Alternative configuration with environment variables

All configuration parameters can be overridden by setting environment variables named in a specific way. At program start openrouteservice reports on every environment variable that *might* have an effect on its behavior. You can run openrouteservice entirely without a configuration file by setting all required properties via environment variables.

Every property corresponds to an environment variable name in *uppercase letters* and with *underscores* replacing *dots*, so e.g. 
- `ORS_ENGINE_SOURCE_FILE` replaces `ors.engine.source_file`
- `ORS_ENGINE_PROFILES_CAR_ENABLED` replaces `ors.engine.profiles.car.enabled`

Therefore, you could run openrouteservice using the following commands to achieve the same as with the example minimal configuration mentioned above: 

```shell
  export ORS_ENGINE_SOURCE_FILE=./osm_file.pbf
  export ORS_ENGINE_PROFILES_CAR_ENABLED=true
  java ors.jar 
```

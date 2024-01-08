# Configuration

The configuration of your own **openrouteservice** instance is done by pointing an environment variable called `ORS_CONFIG_LOCATION` to a YAML configuration file. 

To download an example configuration and set the environment variable use the following:

```shell
wget https://raw.githubusercontent.com/GIScience/openrouteservice/master/ors-api/ors-config.yml
export ORS_CONFIG_LOCATION=${pwd}/ors-config.yml
```

Default settings for all configurable parameters are defined in
[`application.yml`](https://github.com/GIScience/openrouteservice/blob/master/ors-api/src/main/resources/application.yml).

If you have cloned the repository, you will find these files at `ors-api/ors-config.yml` and `ors-api/src/main/resources/application.yml` respectively. 

In the past **openrouteservice** was configured [via JSON file](./json). This configuration method has been deprecated and will be eventually removed, therefore we strongly discourage you from using it.

## Available properties

The properties are organized in a hierarchical structure, with the following ones at top level.

- [spring-specific](./spring) settings, such as `server` and `spring`
- settings relating to [logging](./logging)
- **openrouteservice**  settings organized under the following four blocks: 

| key                                     | description                                                                 |
|-----------------------------------------|-----------------------------------------------------------------------------|
| [ors.endpoints](./endpoints-and-limits) | Settings required at runtime to process API requests.                       |
| [ors.engine](./engine)                  | Settings required at graph-build time during startup.                       |
| [ors.cors](./cors)                      | CORS settings for the **openrouteservice** API.                             |
| [ors.messages](./messages)              | System messages that can be sent with API responses following simple rules. |

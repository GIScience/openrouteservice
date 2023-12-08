# Configuration

The configuration of your own **openrouteservice** instance is done in a YAML file named `ors-config.yml`.
**openrouteservice** looks for the file in several different locations, in the following order. 

- `/etc/openrouteservice/ors-config.yml`
- `~/.openrouteservice/ors-config.yml`
- `./ors-config.yml` (in the runtime working directory)

If a property is set in multiple files, later occurrences will override previous ones.
Default settings for all configurable parameters are defined in
[`application.yml`](https://github.com/GIScience/openrouteservice/blob/master/ors-api/src/main/resources/application.yml).

In the past **openrouteservice** was configured [via JSON file](./ors-config-json). This configuration method has been deprecated and will
be eventually removed, therefore we strongly discourage you from using it.

## Available properties

The properties are organized in a hierarchical structure, with the following ones at top level.

- [spring-specific](./spring) settings, such as `server` and `spring`
- settings ralating to [logging](./logging)
- **openrouteservice**  settings organized under the following four blocks: 

| key                                     | description                                                                 |
|-----------------------------------------|-----------------------------------------------------------------------------|
| [ors.endpoints](./endpoints-and-limits) | Settings required at runtime to process API requests.                       |
| [ors.engine](./engine)                  | Settings required at graph-build time during startup.                       |
| [ors.cors](./cors)                      | CORS settings for the **openrouteservice** API.                             |
| [ors.messages](./messages)              | System messages that can be sent with API responses following simple rules. |




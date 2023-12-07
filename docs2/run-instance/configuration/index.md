# Configuration

The configuration of your own **openrouteservice** instance is done in a YAML file named `ors-config.yml`.
**openrouteservice** looks for the file in several different locations, in the following order. 

- `/etc/openrouteservice/ors-config.yml`
- `~/.openrouteservice/ors-config.yml`
- `./ors-config.yml` (in the runtime working directory)

If a property is set in multiple files, later occurances will override previous ones.
Default settings for all configurable parameters are defined in
[`application.yml`](https://github.com/GIScience/openrouteservice/blob/master/ors-api/src/main/resources/application.yml).

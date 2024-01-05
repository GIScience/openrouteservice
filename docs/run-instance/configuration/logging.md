# Logging

openrouteservice logs status and error messages both to the console and a log file. No access logs are kept for requests.

## Configure loglevel

You can configure the loglevel for your local instance in your custom YAML config file. For example, to enable debug logging, use the following configuration.
```yaml
logging:
  level:
    org.heigit: DEBUG
```
To reduce output for production deployment, use
```yaml
logging:
  level:
    org.heigit: ERROR
```

## Configure logfile

The ors log file is kept by default as `logs/ors.log` relative to the runtime working directory, and is rotated daily with archived files kept as `ors.yyyy-MM-dd.log.gz`.  
You can change the log file location by setting an environment variable `ORS_LOG_LOCATION`, e.g.:
```shell
export ORS_LOG_LOCATION=/path/to/logs
```

The log rotation can be configured also using an environment variable `ORS_LOG_TIMESTAMP`. This influences the file name pattern and the rotation trigger at the same time. For example, to set log rotation to occur every minute, set
```shell
export ORS_LOG_TIMESTAMP=yyyy-MM-dd_hh:mm
```


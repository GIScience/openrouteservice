# `logging`

By default, openrouteservice logs status and error messages both to the console and a log file. No access logs are kept for requests.

Logging can be configured using spring properties in the `logging.` space, for a full list see the [spring docs](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#appendix.application-properties.core). 

The most important ones are:

| key                      | type   | description                                         | default value                                                                                              |
|--------------------------|--------|-----------------------------------------------------|------------------------------------------------------------------------------------------------------------|
| logging.file.name        | string | Path/filename of the log file                       | `./logs/ors.log`                                                                                           |
| logging.pattern.console  | string | Pattern for log messages to console                 | `%d{YYYY-MM-dd HH:mm:ss} %highlight{%-7p} %style{%50t}{Cyan} %style{[ %-40.40c{1.} ]}{Bright Cyan}   %m%n` |
| logging.pattern.file     | string | Pattern for log messages to log file                | `%d{YYYY-MM-dd HH:mm:ss} %-7p [ %-40.40c{1.} ]   %m%n`                                                     |
| logging.level.root       | string | Default log level                                   | `WARN`                                                                                                     |
| logging.level.org.heigit | string | Log level for message from the org.heigit namespace | `INFO`                                                                                                     |


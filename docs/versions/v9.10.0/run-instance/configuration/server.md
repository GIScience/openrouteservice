# `server` 

General server properties used by spring

| key                         | type   | description                     | default value |
|-----------------------------|--------|---------------------------------|---------------|
| server.port                 | int    | Server HTTP port                | `8082`        |
| server.servlet.context-path | string | Context path of the application | `/ors`        |

::: info
Other [spring server properties](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#appendix.application-properties.server)
might work out of the box, but are untested.
:::
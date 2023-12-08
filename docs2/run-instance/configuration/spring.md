# Spring

Since **openrouteservice** is based on [spring](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html), all common
spring properties can be set in the `ors-config.yml` file. The most relevant for regular use are the ones below.

| key                  | type   | description                     | default value |
|----------------------|--------|---------------------------------|---------------|
| server.port          | int    | Server HTTP port                | 8082          |                        
| servlet.context-path | string | Context path of the application | /ors          |                        

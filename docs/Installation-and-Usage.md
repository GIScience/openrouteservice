---
title: Installation and Usage
nav_order: 2
has_children: true
has_toc: false
---

# Installation and Usage
## Installation
* [Running with Docker](installation/Running-with-Docker)
* [Building from Source](installation/Building-from-Source)
* [System requirements](installation/System-Requirements)
* [Configuration](installation/Configuration-(app.config))
* [Advanced Docker Setup](installation/Advanced-Docker-Setup)

## Usage
Openrouteservice offers a set of endpoints for different spatial purposes. They are served with the help of [Tomcat in a java servlet container](https://github.com/GIScience/openrouteservice/blob/master/openrouteservice/WebContent/WEB-INF/web.xml). By default you will be able to query the services with these addresses:

- `http://localhost:8080/name_of_war_archive/routes`
- `http://localhost:8080/name_of_war_archive/isochrones`
- `http://localhost:8080/name_of_war_archive/matrix`


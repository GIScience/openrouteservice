---
title: Installation and Usage
nav_order: 1
has_children: true
---

[ :arrow_backward:  Home](https://github.com/GIScience/openrouteservice/wiki)
# Installation and Usage
## Installation
* [Running with Docker](https://github.com/GIScience/openrouteservice/wiki/Running-with-Docker)
* [Building from Source](https://github.com/GIScience/openrouteservice/wiki/Building-from-Source)
* [System requirements](https://github.com/GIScience/openrouteservice/wiki/System-Requirements)
* [Advanced Docker Setup](Advanced-Docker-Setup)

## Usage
Openrouteservice offers a set of endpoints for different spatial purposes. They are served with the help of [Tomcat in a java servlet container](https://github.com/GIScience/openrouteservice/blob/master/openrouteservice/WebContent/WEB-INF/web.xml). By default you will be able to query the services with these addresses:

- `http://localhost:8080/name_of_war_archive/routes`
- `http://localhost:8080/name_of_war_archive/isochrones`
- `http://localhost:8080/name_of_war_archive/matrix`


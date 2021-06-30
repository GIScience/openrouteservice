---
parent: Contributing
nav_order: 1
title: Contributing Code
---

[ :arrow_backward: Contributing](Contributing)
# Contributing Code

openrouteservice is an open source project, which means that you are free to download and modify the code as you wish (while respecting the [license](https://github.com/GIScience/openrouteservice/blob/master/LICENSE)). We highly appreciate contributions of cool new features or fixes of annoying bugs, so that the whole community can benefit from it.

## Guidelines

We put together a [few guidelines](https://github.com/GIScience/openrouteservice/blob/master/CONTRIBUTE.md) to help you in the process and keep the repository clean and tidy.

## Quick Setup

For development it's best to use our testing settings:

1. Fork the repository and checkout `development`

```bash
git clone https://github.com/user/openrouteservice.git
cd openrouteservice
git checkout development
```

2. Copy the test app configuration `app.config.test` to the appropriate directory:

```bash
cp openrouteservice-api-tests/conf/app.config.test openrouteservice/WebContent/WEB-INF
```

3. Replace all instances of `openrouteservice-api-tests/data` with the full path, e.g. `/home/nilsnolde/.../openrouteservice-api-tests/data`.

4. Set up your IDE project and import `openrouteservice` and `openrouteservice-api-tests` modules as Maven model.

5. Configure your IDE to run `tomcat7` and set the environment variable `ORS_APP_CONIFG=app.config.test`.

6. You can run API tests via JUnit.

## Integrate GraphHopper

If you need to make adjustments to our forked and edited [GraphHopper repository](https://github.com/GIScience/graphhopper), follow these steps:

1. Clone and checkout `ors_0.13.2`:

```bash
git clone https://github.com/GIScience/graphhopper.git
cd graphhopper
git checkout ors_0.13.2
```

2. Build the project to create the local snapshot.

3. Change the `openrouteservice/pom.xml`:

```xml
<!--
<dependency>
<groupId>com.github.GIScience.graphhopper</groupId>
<artifactId>graphhopper-core</artifactId>
    <version>v0.9.12</version>
</dependency>

<dependency>
<groupId>com.github.GIScience.graphhopper</groupId>
<artifactId>graphhopper-reader-osm</artifactId>
<version>v0.9.12</version>
</dependency>
-->

<dependency>
    <groupId>com.graphhopper</groupId>
    <artifactId>graphhopper-core</artifactId>
    <version>0.13-SNAPSHOT</version>
</dependency>

<dependency>
    <groupId>com.graphhopper</groupId>
    <artifactId>graphhopper-reader-osm</artifactId>
    <version>0.13-SNAPSHOT</version>
</dependency>
```

4. Test your new functionality and run `openrouteservice-api-tests` after rebasing your feature branch with the latest `development` branch. Adjust tests if necessary

5. If successful, create a PR for both [openrouteservice](https://github.com/GIScience/openrouteservice/pulls) and [GraphHopper](https://github.com/GIScience/graphhopper/pulls) against `development` and `ors_0.13.2` branches, respectively.

**Note that in these examples, the 0.13_2 version of GH is used - you should update which you use accordingly. To know which to use, check the openrouteservice pom file and see what version is being used for the `com.github.GIScience.graphhopper` dependencies**

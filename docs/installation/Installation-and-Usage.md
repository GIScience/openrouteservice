---
title: Installation and Usage
nav_order: 2
has_children: true
has_toc: false
---

# Installation and Usage

## Installation via Docker

We suggest using docker to install and launch openrouteservice backend. In short, run the following commands will get
everything done.

```bash
wget https://raw.githubusercontent.com/GIScience/openrouteservice/master/docker-compose.yml
docker compose up
```

This will pull the latest release version of openrouteservice from dockerhub and start it up using an example setup and
the provided test OSM file.
You can also modify the configuration and source file settings to match your needs. For more details, check
the [Running with Docker](Running-with-Docker)-Section.
More explanation about customization can be found in the [Advanced Docker Setup](Advanced-Docker-Setup)

## Installation of `openrouteservice-jws5` via RPM Package

The following explanation will guide you through the installation of the `openrouteservice-jws5` package
on `RHEL 8.x` with `OpenJDK 17 headless` and `JBoss Web Server 5.x` for `ors version 7.2.x`.

### Prerequisites

#### Installation via RedHat jws5 subscription

Install the following packages via dnf with a valid RedHat subscription for jws5:

```bash
dnf groupinstall jws5
dnf install -y java-17-openjdk-headless
```

#### Set environment variables

For the installation, the variable `ORS_HOME` showing the persistence directory of the OpenRouteService needs to be set, e.g.

    export ORS_HOME=/opt/openrouteservice

Only if JBoss Web Service was installed _in a different way_ than described above, some paths need to be specified additionally:

    export JWS_CONF_FOLDER=<your custom path>
    export JWS_WEBAPPS_FOLDER=<your custom path>

#### Yum .repo Configuration

To access and install `openrouteservice` via RPM packages from our repository, set up the following .repo file
within `/etc/yum.repos.d/`:

> /etc/yum.repos.d/ors.repo

```bash
[openrouteservice-rpm-snapshots]
name=openrouteservice RPM repository
baseurl=https://test-repo.openrouteservice.org/repository/openrouteservice-rpm/snapshots/openrouteservice-jws
enabled=1
gpgcheck=1
gpgkey=https://keys.openpgp.org/vks/v1/by-fingerprint/825F57B756C0B5851C398478585E8FA82AFB5B55

# The releases repository is not yet available
#[openrouteservice-rpm-releases]
#name=openrouteservice RPM repository
#baseurl=https://test-repo.openrouteservice.org/repository/openrouteservice-rpm/realeases/openrouteservice-jws
#enabled=1
#gpgcheck=1
#gpgkey=https://keys.openpgp.org/vks/v1/by-fingerprint/825F57B756C0B5851C398478585E8FA82AFB5B55
```

This repository includes two channels: `snapshots` and `releases`.
The `snapshots channel` contains the latest snapshot builds of the `openrouteservice-jws5` package,
while the `releases channel` holds the latest release builds (though it is not yet operational).

After adding the repository, update dnf:

    dnf update


### Installation

Now you can install the openrouteservice itself using the following command:

    dnf install openrouteservice-jws5

This will install the openrouteservice WAR file into the jws5 webapps folder and generates the `ORS_HOME` working directory, which houses the
subsequent subfolders:

```bash
/opt/openrouteservice/
├── .elevation-cache # Contains the elevation data cache
├── config # Should contain the configuration file
├── files # Should contain the osm.pbf source files
├── .graphs # Contains the graph files when build
└── logs # Contains the log files
```

In the `config` folder a file `example-config.json` is extracted, which can be used as template for the file `ors-config.json` in the same directory. `ors-config.json` is your custom config file which is used by openrouteservice.

---
**Example Usage**

The following usage example showcases the execution of OpenRouteService with the recently
installed `openrouteservice-jws5` package:

```bash
# Obtain a OSM file using curl
curl https://download.geofabrik.de/europe/andorra-latest.osm.pbf -o /opt/openrouteservice/files/osm-file.osm.pbf
# Utilize the default configuration file
cp /opt/openrouteservice/config/config-example.yml /opt/openrouteservice/config/ors-config.yml
# Restart the tomcat server and await graph construction
# Check the endpoint ors/v2/status, which should display "ready" once graph construction is complete.
curl http://127.0.0.1:8080/ors/v2/status
```

---

## Other Resources

* [Building from Source](Building-from-Source)
* [System requirements](System-Requirements)
* [Configuration](Configuration)

## Usage

Openrouteservice offers a set of endpoints for different spatial purposes. They are served with the help
of [Tomcat in a java servlet container](https://github.com/GIScience/openrouteservice/blob/master/ors-api/WebContent/WEB-INF/web.xml).
By default you will be able to query the services with these addresses:

- `http://localhost:8080/ors/v2/directions`
- `http://localhost:8080/ors/v2/isochrones`
- `http://localhost:8080/ors/v2/matrix`

Note, that Tomcat running via maven will use port `8082` by default.


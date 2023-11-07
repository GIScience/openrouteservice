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

## Installation of `openrouteservice-jws5` via RPM Package for Enterprise Linux Environments

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

```bash
echo "export ORS_HOME=/opt/openrouteservice" >> /etc/environment
```    

**Only if** JBoss Web Service was installed _in a different way_ than described above, some paths need to be specified additionally:

```bash
echo "JWS_CONF_FOLDER=<your custom path>" >> /etc/environment
echo "JWS_WEBAPPS_FOLDER=<your custom path>" >> /etc/environment
```

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

```bash
dnf update
```

### Installation

Now you can install the openrouteservice itself using the following command:

```bash
sudo dnf clean all && sudo dnf check-update && sudo dnf install -y openrouteservice-jws5
```

---
**Default openrouteservice User and Group**

The installation of `openrouteservice-jws5` establishes a `new openrouteservice user and group`.
This `non-root`, `nologin` user is employed to securely manage RPM package files and directories during installation,
upgrading, and removal of openrouteservice-jws5 packages.

The `tomcat` user `is assigned membership` within the openrouteservice group,
granting them access to manipulate files and folders within `ORS_HOME` and its subdirectories.

Upon each execution of the openrouteservice-jws5 package's update routine, file and folder permissions
within `ORS_HOME` are realigned to the openrouteservice user and group.

This collaborative group setup prevents interference between the tomcat user permissions during installation,
ensuring a smooth process.


---
**Default folder structure**

Upon installation, `openrouteservice-jws5` generates the `ORS_HOME` working directory, which houses the
subsequent subfolders (initially empty unless manually created):

```bash
/opt/openrouteservice/ # Ownership: 770 openrouteservice:openrouteservice | Desc: Base for $ORS_HOME
├── .elevation-cache   # Ownership: 770 openrouteservice:openrouteservice | Desc: Contains the elevation cache
├── logs               # Ownership: 770 openrouteservice:openrouteservice | Desc: Contains the log files
├── .graphs            # Ownership: 770 openrouteservice:openrouteservice | Desc: Contains the graph files when built
├── files              # Ownership: 770 openrouteservice:openrouteservice | Desc: Should contain the OSM file(s)
├── config             # Ownership: 770 openrouteservice:openrouteservice | Desc: Contains the example-config.json configuration file
├── config/example-config.json # Ownership: 440 openrouteservice:openrouteservice | Desc: Contains the example-config.json configuration file
└──.openrouteservice-jws5-permanent-state # Ownership: 440 openrouteservice:openrouteservice | Desc: Contains the permanent state of the openrouteservice installation
```

The directory structure at `$ORS_HOME` is configured with specific permissions to maintain security and access control.
In general, it follows the `770` permission scheme, allowing both the Owner and Group to read and write to the necessary
folders and have the search bit enabled to create files.

In instances where write access is unnecessary, permissions are set to `640`, restricting write privileges for
user `openrouteservice` and group members to read only.
This arrangement ensures that the 'tomcat' user can access and read the files without the ability to make changes.

Notably, the `.openrouteservice-jws5-permanent-state` file and the `config/example-config.json` are assigned `440`
permissions, ensuring that only the
installation processes can write to it, while access for others is restricted.

---
**Configuration**

For proper operation, the `openrouteservice-jws5` installation `necessitates` the presence of the `ors-config.json`
configuration file within the `$ORS_HOME/config` directory.
This configuration file effectively configures the openrouteservice backend.

Upon installation, a sample configuration file (`config-example.json`) can be located within the `$ORS_HOME/config` directory.

Upon installation, a tomcat configuration file (`openrouteservice-jws5-permanent-state`) can be located within the `$ORS_HOME/` directory. This file contains variables set for the JWS5 Tomcat instance running ORS. You should not  change the content of this file, EXCEPT in some cases the folling variables: 
- `CATALINA_OPTS`: Memory settings for the VM running the ORS instance. Max heap memory is set to max amount of ram available on the system as per cat /proc/meminfo, minus  4 GB if it is more than 4 GB; initial heap size is set to half the max value. Change these settings only if necessary and on your own risk.
- `ORS_LOG_ROTATION`: Cron-like pattern passed to Log4J to determine log rotation timing and frequency. Defaults to 00:00:00 h every day. See [Log4J documentation](https://logging.apache.org/log4j/2.x/manual/appenders.html#cron-triggering-policy) for details. Note that ORS does not log any individual requests, and the log file(s) will normally only contain startup information and any occurring error messages.

---
**Example Usage**

The following usage example showcases the execution of OpenRouteService with the recently
installed `openrouteservice-jws5` package:

```bash
# Obtain a OSM file using curl
curl https://download.geofabrik.de/europe/andorra-latest.osm.pbf -o /opt/openrouteservice/files/osm-file.osm.pbf
# Utilize the default configuration file
cp /opt/openrouteservice/config/config-example.json /opt/openrouteservice/config/ors-config.json
# Restart the tomcat server and await graph construction
# Check the endpoint ors/v2/status, which should display "ready" once graph construction is complete.
curl http://127.0.0.1:8080/ors/v2/status
```

---

## Custom settings for JWS 5.x

In order to set custom settings for JAVA_OPTS, CATALINA_OPTS, e.g. you can use following commands:

```bash
sudo mkdir -p /etc/opt/rh/scls/jws5/tomcat/conf.d
```

```bash
sudoedit /etc/opt/rh/scls/jws5/tomcat/conf.d/openrouteservice.conf
```

```bash
export CATALINA_OPTS="$CATALINA_OPTS -Xms4g -Xmx4g"
export ORS_HOME=/data/openrouteservice
```

After setting the values it is required to restart JWS 5.x

## Starting OpenRouteService

```bash
sudo systemctl enable --now jws5-tomcat.service
```

## Usage

Openrouteservice offers a set of endpoints for different spatial purposes. They are served with the help
of [Tomcat in a java servlet container](https://github.com/GIScience/openrouteservice/blob/master/ors-api/WebContent/WEB-INF/web.xml).
By default you will be able to query the services with these addresses:

- `http://127.0.0.1:8080/ors/v2/directions`
- `http://127.0.0.1:8080/ors/v2/isochrones`
- `http://127.0.0.1:8080/ors/v2/matrix`

## Logging and debugging

JWS 5.x uses RHEL's journald functionality.

```bash
sudo journalctl -f -u jws5-tomcat
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

Note, that Tomcat running via maven will use port `8080` by default.


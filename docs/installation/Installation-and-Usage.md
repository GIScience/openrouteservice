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

The following explanation will guide you through the installation of the `openrouteservice-jws5` package on `RHEL8/CentOS 8` with `Java 17` and `JBoss Web Server 5` for `ors version 7.2.x`.

---
**Necessary Packages**

To successfully install OpenRouteService via the RPM package,
ensure that your RedHat Enterprise Linux 8 or CentOS 8 system has the following packages enabled through the subscription manager::
```bash
- jws5-runtime  
- jws5-tomcat  
- jws5-tomcat-native  
- jws5-tomcat-selinux  
- java-17-openjdk
```

In case these packages are absent, the installation process will make an automatic attempt to install them. 
A failure to do so will result in the inability to install the `openrouteservice-jws5` package.

---
**Required environment variables**

During the installation of the `openrouteservice-jws` package, specific environment variables are essential:

- `JWS_HOME` - The `tomcat` installation directory of the JBoss Web Server 5, e.g. `/opt/jws5/tomcat`

Upon the successful installation of the `jws5-*` packages, 
the `JWS_HOME` environment variable will be automatically set, 
directing to the tomcat installation directory of JBoss Web Server 5.

To verify whether the `JWS_HOME` environment variable is correctly configured, run the following command:

```bash
echo $JWS_HOME
```

The expected output should be the `tomcat` installation directory of JBoss Web Server 5 (e.g., `/opt/jws5/tomcat`).

---
**Required JWS5 User**

The installation of `openrouteservice-jws5` anticipates one of the following users to be present and employed **as the execution user** by JBoss Web Server 5:

- `jboss` or
- `tomcat`

The existence of either `jboss` **or** `tomcat` users is mandatory for the successful installation of `openrouteservice-jws5`.

---
**Yum .repo Configuration**

To access and install `openrouteservice` via RPM packages from our repository, set up the following .repo file within `/etc/yum.repos.d/`:

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

---
**Yum installation**

To install the latest snapshot build of the `openrouteservice-jws5` package, run the following command:

```bash
yum update && yum install openrouteservice-jws5
``` 
---
**Default openrouteservice User and Group**

The installation of `openrouteservice-jws5` establishes a `new openrouteservice user and group`. 
This `non-root`, `nologin` user is employed to securely manage RPM package files and directories during installation, upgrading, and removal of openrouteservice-jws5 packages.

Both `jboss` and `tomcat` users `are assigned membership` within the openrouteservice group, 
granting them access to manipulate files and folders `within /opt/openrouteservice/` and its subdirectories. 

Upon each execution of the openrouteservice-jws5 package's update routine, file and folder permissions within `/opt/openrouteservice` are realigned to the openrouteservice user and group.

This collaborative group setup prevents interference between the jboss and tomcat user permissions during installation, ensuring a smooth process.

---
**Default folder structure**

Upon installation, `openrouteservice-jws5` generates the `/opt/openrouteservice/ working directory`, which houses the subsequent subfolders (initially empty unless manually created):

```bash
/opt/openrouteservice/
├── .elevation-cache # Contains the elevation data cache
├── .war-files # Contains the versioned openrouteservice.war files
├── config # Should contain the configuration file
├── files # Should contain the osm.pbf source files
├── graphs # Contains the graphhopper graph files when build
└── logs # Contains the log files
```

Of these folders, only `.war-files` houses the versioned `.war files`. 
The remaining folders are empty following the first installation. 
Always, `the .war-files folder retains all versioned .war files` from prior installations. 
The current `.war file` from the latest package installation is `linked` to the tomcat server `through a symbolic link`, as follows:

> (symlink) ${JWS_HOME}/webapps/ors.war -> /opt/openrouteservice/.war-files/openrouteservice-{LATEST_VERSION}.war
---
**Configuration**

For proper operation, the `openrouteservice-jws5` installation `necessitates` the presence of the `ors-config.yml` configuration file within the `/opt/openrouteservice/config/` directory. 
This configuration file effectively configures the openrouteservice backend.

Upon installation, a sample configuration file (`config-example.yml`) can be located `within` the `/opt/openrouteservice/config/` directory.

---
**Example Usage**

The following usage example showcases the execution of OpenRouteService with the recently installed `openrouteservice-jws5` package:

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


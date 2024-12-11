# Setup ORS v9 with Tomcat 10 on Ubuntu 22.04

This guide will guide you how to set up openrouteservice with Java 17, Tomcat 10 and ORS v9.

::: info
To the [german version](de_tomcat-10-ubuntu_22_04) of this tutorial.
:::

## Prerequisites

- Ubuntu 22.04 with Systemd enabled (Systemd should be enabled by default)
- Tomcat 10
- Java 17+

## Assumptions

- We assume that your system is running only one Tomcat 10 instance.
- If you plan to run multiple Tomcat services, you are an advanced user and should adapt the instructions accordingly.

## Prepare the Tomcat 10 environment

The following steps will guide you through the process of preparing the Tomcat 10 environment.

### Install Java 17

Openrouteservice v9 requires Java 17 or higher to run.
The reason is the introduction of Tomcat 10.
You can also use a higher version of Java if available.

```shell
# Update the package index
> sudo apt-get update
# Install Java 17 or higher, curl and nano
> sudo apt-get install openjdk-17-jre-headless curl nano
```

- `openjdk-17-jre-headless` is the Java 17 runtime environment as a headless package. We don't need a graphical user
  interface for openrouteservice.
- `curl` is a command-line utility for downloading files from the web and will be used to download certain resources.
- `nano` is a simple text editor that will be used to edit certain files.

List your available Java versions.

```shell
# List available options and copy the path to the Java 17 installation
sudo update-alternatives --list java
```

The output should look like similar to this:

```shell
[...]
/usr/lib/jvm/java-17-openjdk-amd64/
[...]
```

Update the default Java version to use Java 17 or the version you installed.

```shell
# Set the default Java version to Java 17
> sudo update-alternatives --set java /usr/lib/jvm/java-17-openjdk-amd64/bin/java
```

::: info
**Note:** The path to the Java 17 installation may vary depending on your system. \
**Note:** From now on we will refer to the path to the Java 17 installation as `JAVA_HOME`.
:::

### Create a new user for Tomcat 10

The Tomcat 10 runtime should be run as a separate user for security reasons.
This user should not have any login permissions and should not be able to execute commands.
We will call this user `tomcat`.

```shell
# Create a new user for Tomcat 10
> useradd -r -m -U -d /opt/tomcat -s /bin/false tomcat
```

- `-r` creates a system user
- `-m` creates a home directory (`/opt/tomcat`) for the user and applies the necessary permissions
- `-U` creates a group with the same name as the user

### Download and setup Tomcat 10

```shell
# Set the Tomcat version
> export TOMCAT_VERSION=10.1.33
# Download the Tomcat 10 tarball
> curl -L https://dlcdn.apache.org/tomcat/tomcat-10/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz > apache-tomcat-$TOMCAT_VERSION.tar.gz
# Extract the downloaded file
> tar -xf apache-tomcat-$TOMCAT_VERSION.tar.gz
# Copy the contents of the extracted directory to the /opt/tomcat directory
> cp -R apache-tomcat-$TOMCAT_VERSION/** /opt/tomcat
# Clean up the extracted directory
> rm -r apache-tomcat-$TOMCAT_VERSION apache-tomcat-$TOMCAT_VERSION.tar.gz
```

### Set the necessary permissions for the Tomcat 10 user

The following commands will set the necessary permissions for the `tomcat` user.
They have to be executed as the root user and should always be executed when changes were made to the Tomcat directory.

```shell
> sudo chown -R tomcat:tomcat /opt/tomcat
> sudo chmod -R 754 /opt/tomcat
```

- `chown -R tomcat:tomcat /opt/tomcat` sets the owner and group of the `/opt/tomcat` directory to the `tomcat` user.
- `chmod -R 754 /opt/tomcat` sets the permissions for the `/opt/tomcat` directory.
    - `7` grants read, write, and execute permissions to the owner.
    - `5` grants read and execute permissions to the group.
    - `4` grants read permissions to others.

### Create a systemd service for Tomcat 10

We now create a systemd service for Tomcat 10 to manage the Tomcat 10 instance.
This will allow your Systemd to start and stop Tomcat 10 as a service automatically.
Paste the content into the file and save it.

```shell
> sudo nano /etc/systemd/system/openrouteservice.service
```

```ini
[Unit]
Description=Tomcat - openrouteservice
After=syslog.target network.target

[Service]
Type=forking

User=tomcat
Group=tomcat
RestartSec=10
Restart=always

Environment="JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/"
Environment="JAVA_OPTS=-Djava.security.egd=file:///dev/urandom"
Environment="CATALINA_OPTS=-server -XX:+UseParallelGC"

Environment=CATALINA_PID=/opt/tomcat/temp/tomcat.pid
Environment=CATALINA_HOME=/opt/tomcat
Environment=CATALINA_BASE=/opt/tomcat

ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/opt/tomcat/bin/shutdown.sh

[Install]
WantedBy=multi-user.target
```

### Test the new Tomcat 10 setup

```shell
# Reload the systemd daemon
> sudo systemctl daemon-reload
# Start the Tomcat 10 service
> sudo systemctl enable --now openrouteservice.service
# Check the status of the Tomcat 10 service
> sudo systemctl status openrouteservice.service
```

Now navigate to [http://localhost:8080](http://localhost:8080) in your browser to see the Tomcat 10 welcome page.

## Prepare the openrouteservice environment

Since we have set up Tomcat 10, we can now set up openrouteservice with Java 17.

### Download the openrouteservice WAR-File

To set up openrouteservice v9 with Tomcat 10, you first need the respective war-File.
Head over to the [openrouteservice releases page](https://github.com/GIScience/openrouteservice/releases) and download
the latest release WAR-File.

```shell
# Download the latest openrouteservice WAR-File, e.g. for v9.0.0
> curl -L https://github.com/GIScience/openrouteservice/releases/download/v9.0.0/ors.war > ors.war
# Move the WAR-File to the Tomcat webapps directory
> mv ors.war /opt/tomcat/webapps/
# Restart the Tomcat 10 service
> sudo systemctl restart openrouteservice.service
# Check the status of the Tomcat 10 service
> sudo systemctl status openrouteservice.service
```

If you navigate to `http://localhost:8080/ors/v2/health` you should see the health status of the openrouteservice.
The output will be as follows:

```json
{
    "status": "not ready"
}
```

::: info
ORS is now set up correctly with Tomcat 10, but it still needs to be configured. Therefore, it is showing `not ready`. \
If you want to see an exact log output, you can check the tomcat log files in the `/opt/tomcat/logs` directory.
:::

### Create the openrouteservice folder structure

In order for openrouteservice to work correctly, you need to set up the folder structure, download a test osm file and
set up the configuration.

```shell
mkdir -p "/opt/openrouteservice/graphs"
mkdir -p "/opt/openrouteservice/logs"
mkdir -p "/opt/openrouteservice/data"
mkdir -p "/opt/openrouteservice/elevation_cache"
```

::: info
The folder structure is a suggestion and can be adapted to your needs. \
Make sure to adjust the configuration accordingly as well as the permissions.
:::

### Download a test osm file

A good source for latest osm-files is the [Geofabrik download server](https://download.geofabrik.de/).
We will download a small test osm file for Andorra.

```shell
# Download a test osm file to the /opt/openrouteservice/data directory
> curl -L https://download.geofabrik.de/europe/andorra-latest.osm.pbf > /opt/openrouteservice/data/andorra-latest.osm.pbf
```

### Configure openrouteservice

Configure Tomcat and openrouteservice is best done by setting the configuration in the `setenv.sh` file in the `bin`
directory of Tomcat.

```shell
# Create the `setenv.sh` file in the `bin` directory of Tomcat.
> sudo nano /opt/tomcat/bin/setenv.sh
```

Paste the following contents into the file and save it.
Make sure to adjust the `-Xmx` value to a value that fits your system and graph.
If you want to build another OSM-File, you can adjust the `source_file` value.

If you want to learn more about the new configuration options in version 9, check
the [configuration documentation](/run-instance/configuration/index.md).

**Example `setenv.sh` file for openrouteservice v9**

```shell
export CATALINA_OPTS="$CATALINA_OPTS -server -XX:+UseParallelGC -Xmx15g"
export JAVA_OPTS="$JAVA_OPTS \
-Dors.engine.profiles.driving-car.enabled=true \
-Dors.engine.graphs_data_access=MMAP \
-Dors.engine.profile_default.enabled=false \
-Dors.engine.profile_default.graph_path=/opt/openrouteservice/graphs \
-Dors.engine.profile_default.build.source_file=/opt/openrouteservice/data/andorra-latest.osm.pbf \
-Dlogging.file.name=/opt/openrouteservice/logs/ors.log \
-Dors.engine.elevation.cache_path=/opt/openrouteservice/elevation_cache
"
```

Set `ors.engine.graphs_data_access` to `RAM_STORE` if you want to use the RAM store instead of the MMAP store.
Make sure you set up `-Xmx` to a value that fits your system and graph and that your system provides enough memory.

## Run openrouteservice

If you followed the above steps, openrouteservice should now be correctly set up with Tomcat 10 and Java 17.
The following steps are mandatory whenever you change the configuration, folder structure or graphs:

```shell
# Reinstate ownerships for tomcat
> sudo chown -R tomcat:tomcat /opt/tomcat
> sudo chown -R tomcat:tomcat /opt/openrouteservice
> sudo chmod -R 754 /opt/tomcat/bin/setenv.sh
> sudo chmod -R 754 /opt/openrouteservice
# Restart the Tomcat 10 service
> sudo systemctl restart openrouteservice.service
# Check the status of the Tomcat 10 service
> sudo systemctl status openrouteservice.service
```

Now that openrouteservice is set up with correct configurations, check the log files in the
`/opt/openrouteservice/logs/ors.log` or the Tomcat log files in the `/opt/tomcat/logs` directory.

Navigate to [http://localhost:8080/ors/v2/health](http://localhost:8080/ors/v2/health) in your browser to see the health status of the openrouteservice or
make a request to the API.

```shell
> curl http://localhost:8080/ors/v2/health
```

The output should look like this:

```json

{
    "status": "ready"
}
```

## Example requests

Let's test if openrouteservice is working correctly by making a request to the API.
Please adapt the test according to your downloaded osm file.

```shell
# Request a route from A to B withing Andorra
> curl -X POST \
  'http://localhost:8080/ors/v2/directions/driving-car' \
  -H 'Content-Type: application/json; charset=utf-8' \
  -H 'Accept: application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8' \
  -d '{"coordinates":[[1.5036892890930176, 42.4972256361276],[1.6298711299896242, 42.57831077271361]]}'

# Request an Isochrone from a point in Andorra
> curl -X POST \
  'http://localhost:8080/ors/v2/isochrones/driving-car' \
  -H 'Content-Type: application/json; charset=utf-8' \
  -H 'Accept: application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8' \
  -d '{"locations":[[1.5036892890930176, 42.4972256361276]],"range":[300]}'
```

## Optional: Pre-Built Graph Update

In case you want to only update the existing graphs with new pre-built ones, you can follow these steps:

1. Download the new graphs from the respective source. They will most like come in a zip or tar.xz file.
2. Unpack the new graphs and make sure they are in the correct format.

```shell
# In case of a tar.xz file
> tar -xvf new_graphs.tar.xz
# In case of a zip file you can use the unzip command
> unzip new_graphs.zip
```

The unpacked graphs will have a folder structure similar to this:

```shell
new_graphs/
├── driving-car
└── cycling-regular
```

3. Empty the old graphs directory `/opt/openrouteservice/graphs/`:

```shell
> mv /opt/openrouteservice/graphs /opt/openrouteservice/graphs_old
> mkdir /opt/openrouteservice/graphs
```

4. Move the new graphs to the `/opt/openrouteservice/graphs/` directory. The new graphs folder structure should look
   similar to this:

```shell
/opt/openrouteservice/graphs/
├── driving-car
└── cycling-regular
```

5. [Apply the correct permissions](#set-the-necessary-permissions-for-the-tomcat-10-user) to the Tomcat directory
   and restart the Tomcat service.
6. Try the [example requests](#example-requests) to see if the new graphs are working correctly.

## Optional: Update openrouteservice to a new version

In case you want to update openrouteservice to a new version, you can follow these steps:

1. Stop the Tomcat service.

```shell
> sudo systemctl stop openrouteservice.service
```

2. Empty the old webapps directory `/opt/tomcat/webapps/` and move the new war-File to the directory.
3. Download the new [war-File](#download-the-openrouteservice-war-file) for the new version.
4. Adapt the [openrouteservice configuration](#configure-openrouteservice) in the `setenv.sh` file.
5. [Apply the correct permissions](#set-the-necessary-permissions-for-the-tomcat-10-user) to the Tomcat directory
   and restart the Tomcat service.
6. Try the [example requests](#example-requests) to see if the new version is working correctly.

# Run openrouteservice WAR

Running a WAR means deploying it to a Tomcat instance:

1. Install Tomcat 10 on your system. E.g. on Ubuntu 22.04, follow these [instructions](https://linuxize.com/post/how-to-install-tomcat-10-on-ubuntu-22-04/).

2. If you want to use system settings (i.e. Java heap size) other than the default, then you need to add these to the `setenv.sh` file in your tomcat bin folder, typically somewhere like `/usr/share/tomcat10/bin/`. If the file is not present, then you can create it.

   The environment variable `ORS_CONFIG_LOCATION` and other optional environment variables need to be written to that file, too. The settings generally used on our servers are similar to:

      ```shell
      JAVA_OPTS="-server -XX:TargetSurvivorRatio=75 -XX:SurvivorRatio=64 -XX:MaxTenuringThreshold=3 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:ParallelGCThreads=4 -Xms105g -Xmx105g -XX:MaxMetaspaceSize=50m"
      CATALINA_OPTS="(here we set settings for JMX monitoring)"
      ORS_CONFIG_LOCATION=/path/to/ors-config.yml
      ```

3. If you add these new settings to the `setenv.sh` file, then you need to restart Tomcat for these to take effect using a command like `sudo systemctl restart tomcat.service`.

4. To get openrouteservice up and running, copy the `ors.war` file to the Tomcat webapps folder.

5. Tomcat should now automatically detect the new WAR file and deploy the service. Depending on profiles and size of the OSM data, this can take some time until openrouteservice has built graphs and is ready for generating routes. You can check if it is ready by accessing `http://localhost:8080/ors/health` (the port and URL may be different if you have installed Tomcat differently than above). If you get a `status: ready` message, you are good to go in creating routes.

6. Your configuration file and all input / output files and directories referenced by that configuration need to be accessible (and in case of the output folders, writable) to the user your Tomcat instance is running as. You might need to adjust the location of said files and folders or `chmod` / `chown` them accordingly.

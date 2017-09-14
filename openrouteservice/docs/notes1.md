### Setup ORS with docker on ubuntu server
1. Edit `etc/hosts` and add `name of the instance` after `ip localhost`, for instance `ip localhost ors-update`
2. Add `55-cloud-init.config` to `/etc/network/interfaces.d` which contains
```
auto ens3:0
iface ens3:0 inet static
     address 129.206.7.125
     netmask 255.255.255.0
     dns-search google.com
     dns-nameservers 8.8.8.8 8.8.4.4
```
3. Install Docker-Engine 
Follow Step 1 to 3 from https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-on-ubuntu-16-04 or https://docs.docker.com/engine/installation/linux/ubuntu/
4. Install Docker-Compose
https://docs.docker.com/compose/install/
5. Create and populate directory (e.g. https://beautifulbytes.wordpress.com/2016/01/04/set-up-apache-httpd-and-tomcat-with-docker-compose/)
    a. `sudo mkdir webfrastructure`, e.g. in `/home/username`
    b. create directory structure

    ``` 
    webfrastructure (g)
    |-- dockerfiles
    |   |-- tomcat
    |   `-- ubuntu (which is apache httpd)
    |-- httpd
    |   `-- conf
    |       |-- apache2 (a)
    |       `-- jk_mod (b)
    `-- tomcat
        |-- conf (c)
        |   `-- profiles (d)
        |-- data (e)
        |   |-- ${NODENAME}1
        |   |-- ${NODENAME}2
        |   `-- ${NODENAME}n
        `-- webapps (f)
    ```

    a. edit apache config `000-default.config` accordingly
    b. edit jk_mod `workers.properties` accordingly
    c. edit `config.log.xml` and `GraphGH.properties.xml` accordingly
    d. edit profile configurations accordingly
    e. copy OSM pbf files to data, if different instances then several files
    f. copy .war ORS archives to `/webapps`
    g. edit `docker-compose.yml` accordingly


6. Create Images and run
within root `sudo docker-compose up -d`





Not needed
---
3. CREATE IMAGES FROM DOCKERFILES =>
`sudo docker build -t openrouteservice:httpd path/to/ubuntu/Dockerfile`
`sudo docker build -t openrouteservice:tomcat path/to/tomcat/Dockerfile`
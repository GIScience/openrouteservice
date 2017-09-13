### Setup ORS on Ubuntu Server
1. Edit DNS Servers, e.g,
- path `sudo nano /etc/resolv.conf`
- add `nameserver 8.8.8.8` and `nameserver 8.8.4.4` (these are Googles DNS)
- Note these may be removed on restart!
2. Edit `etc/hosts` and add `name of the instance` after `ip localhost`, for instance `ip localhost ors-update`
2. Install Apache2, e.g.,
- run `sudo apt-get install apache2`
3. Run global update of libs, with `sudo apt-get update`
3. Install Java, e.g., `sudo apt-get install openjdk-8-jre`
4. Install Tomcat, follow steps in Digital Ocean [Setup Tomcat](https://www.digitalocean.com/community/tutorials/how-to-install-apache-tomcat-8-on-ubuntu-16-04) Note: don't add the initialization config!
5. Create directory for processed graphs and config files, e.g. `opt/ors/name_of_instance`
6. Upload `ors.war`, `setenv.sh`, config files (change paths) to `opt/ors/name_of_instance`
7. Create folder `opt/osmupdater` and either manually `wget url/to/planet.pbf` or setup cronjob with the script `osmupdater.sh`
8. Copy `ors.war` to `/opt/tomcat/webapps/`, war package will automatically expand and create ors folder within webapps
9. Copy corresponding config files from `opt/ors/name_of_instance` to `webapps/ors/WEB-INF/`
10. Run `sudo ./opt/tomcat/bin/startup.sh`  

http://serverfault.com/questions/214054/how-to-install-mod-headers
http://stackoverflow.com/questions/35668702/php-7-domdocument-not-found
http://askubuntu.com/questions/58179/install-mod-proxy-to-get-proxypass-to-work
https://www.digitalocean.com/community/tutorials/how-to-set-up-mod_rewrite-for-apache-on-ubuntu-14-04
FROM tomcat:9.0.75
USER root
WORKDIR /tmp
RUN apt-get update
RUN apt-get install -y alien unzip

COPY srtm_38_03.gh /tmp/elevation-cache/srtm_38_03.gh
COPY ors-config.json /tmp/ors-config.json
COPY heidelberg.osm.gz /tmp/heidelberg.osm.gz
COPY openrouteservice-7.1.0-1.noarch.rpm /tmp/openrouteservice-7.1.0-1.noarch.rpm
RUN alien --scripts /tmp/openrouteservice-7.1.0-1.noarch.rpm
RUN dpkg -i /tmp/openrouteservice_7.1.0-2_all.deb
RUN cp /tmp/ors-config.json /usr/local/tomcat/webapps/ors/WEB-INF/classes/ors-config.json
CMD ["catalina.sh", "run"]
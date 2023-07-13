FROM tomcat:9.0.75
ARG ors_version
ENV ORS_VERSION=$ors_version
USER root
WORKDIR /tmp
RUN apt-get update
RUN apt-get install -y alien unzip

COPY srtm_38_03.gh /tmp/elevation-cache/srtm_38_03.gh
COPY ors-config.json /tmp/ors-config.json
COPY heidelberg.osm.gz /tmp/heidelberg.osm.gz
COPY openrouteservice-${ORS_VERSION}-1.noarch.rpm /tmp/openrouteservice-${ORS_VERSION}-1.noarch.rpm
RUN alien --scripts /tmp/openrouteservice-${ORS_VERSION}-1.noarch.rpm
RUN dpkg -i /tmp/openrouteservice_${ORS_VERSION}-2_all.deb
RUN cp /tmp/ors-config.json /usr/local/tomcat/webapps/ors/WEB-INF/classes/ors-config.json
CMD ["catalina.sh", "run"]
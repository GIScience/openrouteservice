FROM tomcat
USER root
WORKDIR /tmp
RUN apt-get update
RUN apt-get install -y alien unzip

COPY openrouteservice-7.1.0-1.noarch.rpm /tmp/openrouteservice-7.1.0-1.noarch.rpm
RUN alien --scripts /tmp/openrouteservice-7.1.0-1.noarch.rpm
RUN dpkg -i /tmp/openrouteservice_7.1.0-2_all.deb
#RUN dpkg -r openrouteservice

CMD ["catalina.sh", "run"]
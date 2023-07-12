Name: openrouteservice
Version: %{getenv:ORS_VERSION}
Release: 1
Summary: openrouteservice WAR deployment
License: GPL 3
BuildArch: noarch
Requires: tomcat >= 9

%description
The openrouteservice API provides global spatial services by consuming user-generated and collaboratively collected free geographic data directly from http://www.openstreetmap.org.
It is highly customizable, performant and written in Java.
This rpm package looks for an installation of JBoss/WildFly or Tomcat and tries to place the WAR file accordingly. In case neither is found, the war file is placed in /opt/ors.

%install
mkdir -p $RPM_BUILD_ROOT/opt/ors
cp ors.war $RPM_BUILD_ROOT/opt/ors/

%files
%defattr(-,root,root)
/opt/ors/ors.war

%post
if [ -n "$JBOSS_HOME" -a -d $JBOSS_HOME/standalone/deployments ]; then
    mv /opt/ors/ors.war $JBOSS_HOME/standalone/deployments/ors.war
    unzip -qq $JBOSS_HOME/standalone/deployments/ors.war -d $JBOSS_HOME/standalone/deployments/ors
    echo "deployed ors.war to JBoss deployment dir: $JBOSS_HOME/standalone/deployments"
elif [ -n "$CATALINA_HOME" -a -d $CATALINA_HOME/webapps ]; then
    mv /opt/ors/ors.war $CATALINA_HOME/webapps/ors.war
    unzip -qq $CATALINA_HOME/webapps/ors.war -d $CATALINA_HOME/webapps/ors
    echo "deployed ors.war to Tomcat deployment dir: $CATALINA_HOME/webapps"
else
    echo "deployed ors.war to default location: /opt/ors/ors.war"
fi

%preun
if [ -n "$JBOSS_HOME" -a -f $JBOSS_HOME/standalone/deployments/ors.war ]; then
    mv $JBOSS_HOME/standalone/deployments/ors.war /opt/ors/ors.war
    echo "Removed ors.war from JBoss deployment dir"
elif [ -n "$CATALINA_HOME" -a -f $CATALINA_HOME/webapps/ors.war ]; then
    mv $CATALINA_HOME/webapps/ors.war /opt/ors/ors.war
    echo "Removed ors.war from Tomcat deployment dir"
else
    echo "Removed ors.war from default location: /opt/ors/ors.war"
fi
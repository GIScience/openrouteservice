%define java_version 17
%define ors_version %{getenv:ORS_VERSION}
Name: openrouteservice
Version: %{ors_version}
Release: 1
Summary: openrouteservice WAR deployment
License: GPL 3
BuildArch: noarch
Requires:  jws5-runtime
Requires:  jws5-tomcat
Requires:  jws5-tomcat-native
Requires:  jws5-tomcat-selinux
Requires:  java-%{java_version}-openjdk
Vendor: HeiGIT gGmbH

%description
The openrouteservice API provides global spatial services by consuming user-generated and collaboratively collected free geographic data directly from http://www.openstreetmap.org.
It is highly customizable, performant and written in Java.
This rpm package looks for an installation of JBoss JWS and installs the WAR file accordingly.

%install
mkdir -p %{buildroot}/opt/openrouteservice/.war-files/
cp -f ors.war %{buildroot}/opt/openrouteservice/.war-files/%{ors_version}_ors.war

%files
%defattr(-,root,root)
"/opt/openrouteservice/.war-files/%{ors_version}_ors.war"

%pre
# Check for the JWS home ENV variable to be set and echo 'set'
if [ -n "${JWS_HOME}" ]; then
    echo "JWS_HOME found. Assuming JWS installation at ${JWS_HOME}"
else
    echo "JWS_HOME is not set. Exiting installation."
    # Exit the rpm installation with an error
    exit 1
fi

echo "Create the webapps folder in ${JWS_HOME} if it does not exist."
if [ -d ${JWS_HOME}/webapps ]; then
    echo "JWS webapps dir found at ${JWS_HOME}/webapps"
else
    echo "No webapps folder found. Create JWS webapps dir at ${JWS_HOME}/webapps"
    mkdir -p ${JWS_HOME}/webapps
fi

# Check for the existence of an old ors installation in the webapps folder and clean it.
if [ -d ${JWS_HOME}/webapps/ors ]; then
    echo "JWS webapps dir found with old ors installation at ${JWS_HOME}/webapps. Cleaning it."
    rm -rf ${JWS_HOME}/webapps/ors
fi

# Setup openrouteservice opt folder
mkdir -p /opt/openrouteservice/{graphs, logs, config, files, .elevation-cache, .war-files}

%post
# Check for the JWS home ENV variable to be set and echo 'set'
if [ -n "${JWS_HOME}" ]; then
    echo "Copy ors.war to ${JWS_HOME}/webapps"
    cp -f /opt/openrouteservice/.war-files/%{ors_version}_ors.war ${JWS_HOME}/webapps/ors.war
else
    echo "JWS_HOME is not set. Exiting installation."
    # Exit the rpm installation with an error
    exit 1
fi

# Switch to the installed java version
alternatives --set java $(readlink -f /etc/alternatives/jre_%{java_version})/bin/java

%postun
# Uninstall routine if $1 is 0 but leave the opt folder
# For explanation check https://docs.fedoraproject.org/en-US/packaging-guidelines/Scriptlets/#_syntax
if [ "$1" = "0" ]; then
    echo "Uninstalling openrouteservice"
    rm -rf ${JWS_HOME}/webapps/ors
    rm -rf ${JWS_HOME}/webapps/ors.war
fi
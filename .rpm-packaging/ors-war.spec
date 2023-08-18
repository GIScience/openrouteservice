%define java_version 17
%define ors_version %{getenv:ORS_VERSION}
%define ors_local_folder /opt/openrouteservice
%define tomcat_user tomcat
%define jboss_user jboss
%define ors_group openrouteservice
%define ors_user  openrouteservice
Name: openrouteservice-jws5
Version: %{ors_version}
Release: 1
Summary: openrouteservice WAR deployment with JWS5
License: GPL 3
BuildArch: noarch
Requires:  jws5-runtime
Requires:  jws5-tomcat
Requires:  jws5-tomcat-native
Requires:  jws5-tomcat-selinux
Requires:  java-%{java_version}-openjdk
Vendor: HeiGIT gGmbH

# For a detailed step explanation see: https://docs.fedoraproject.org/en-US/packaging-guidelines/Scriptlets/#_syntax

%description
The openrouteservice API provides global spatial services by consuming user-generated and collaboratively collected free geographic data directly from http://www.openstreetmap.org.
It is highly customizable, performant and written in Java.
This rpm package looks for an installation of JBoss JWS and installs the WAR file accordingly.

%install
mkdir -p %{buildroot}%{ors_local_folder}/.war-files/
mkdir -p %{buildroot}%{ors_local_folder}/config/
# Copy the ors.war file to the .war-files folder
cp -f ors.war %{buildroot}%{ors_local_folder}/.war-files/%{ors_version}_ors.war
# Copy the example-config.json file to the config folder
cp -f example-config.json %{buildroot}%{ors_local_folder}/config/example-config.json

%files
# Allow 770 for read and write for files for the ors group
%defattr(770,%{ors_user},%{ors_group},-)
"%{ors_local_folder}/.war-files/%{ors_version}_ors.war"
"%{ors_local_folder}/config/example-config.json"

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

# Create the correct group for the ors installation
getent group %{ors_group} >/dev/null || groupadd -r %{ors_group}

# Check if the jboss user exists and add it to the ors group
if id -u jboss >/dev/null 2>&1; then
    echo "jboss user exists. Adding it to the ors group for data access."
    usermod -a -G %{ors_group} jboss
else
    echo "jboss user does not exist. Skipping adding it to the ors group."
fi

# Check if the tomcat user exists and add it to the ors group
if id -u tomcat >/dev/null 2>&1; then
    echo "tomcat user exists. Adding it to the ors group for data access."
    usermod -a -G %{ors_group} tomcat
else
    echo "tomcat user does not exist. Skipping adding it to the ors group."
fi

# When neither jboss nor tomcat user exist, exit with an error
if ! id -u jboss >/dev/null 2>&1 && ! id -u tomcat >/dev/null 2>&1; then
    echo "Neither jboss nor tomcat user exist. Unknown tomcat setup. Exiting installation."
    # Exit the rpm installation with an error
    exit 1
fi

# Check if the ors user exists and add it to the ors group. If it doesn't exist, create it.
# This user will be used as a user placeholder for package related files
if id -u %{ors_user} >/dev/null 2>&1; then
    echo "openrouteservice user exists. Adding it to the openrouteservice group for data access."
    usermod -a -G %{ors_group} %{ors_user}
else
    echo "openrouteservice user does not exist. Creating it and adding it to the ors group."
    useradd -r -g %{ors_group} -d %{ors_local_folder} -s /sbin/nologin %{ors_user}
fi


# Setup openrouteservice opt folder
mkdir -p "%{ors_local_folder}/graphs"
mkdir -p "%{ors_local_folder}/logs"
mkdir -p "%{ors_local_folder}/config"
mkdir -p "%{ors_local_folder}/files"
mkdir -p "%{ors_local_folder}/.elevation-cache"
mkdir -p "%{ors_local_folder}/.war-files"

%post
# Check for the JWS home ENV variable to be set and echo 'set'
if [ -n "${JWS_HOME}" ]; then
    echo "Link %{ors_version}_ors.war to ${JWS_HOME}/webapps"
    # Create a symlink that links the ors.war file to the webapps folder
    ln -s %{ors_local_folder}/.war-files/%{ors_version}_ors.war ${JWS_HOME}/webapps/ors.war
else
    echo "JWS_HOME is not set. Exiting installation."
    # Exit the rpm installation with an error
    exit 1
fi

# Set environment variable in Tomcat startup script to pick up the correct config file
echo 'export ORS_CONFIG=%{ors_local_folder}/config/ors-config.json' >> ${JWS_HOME}/bin/setenv.sh

# Switch to the installed java version
alternatives --set java $(readlink -f /etc/alternatives/jre_%{java_version})/bin/java

# Set the correct permissions for the /opt/openrouteservice folder so that the ${ors_group} can read and write to it
chown -R %{ors_user}:%{ors_group} %{ors_local_folder}
# Set recursive 770 permissions for the /opt/openrouteservice folder so that the ${ors_group} can read and write to it
chmod -R 770 %{ors_local_folder}

%postun
# Uninstall routine if $1 is 0 but leave the opt folder
# For explanation check https://docs.fedoraproject.org/en-US/packaging-guidelines/Scriptlets/#_syntax
if [ "$1" = "0" ]; then
    echo "Uninstalling openrouteservice"
    # Remove environment variable from Tomcat startup script on uninstall
    sed -i '/export ORS_CONFIG=/d' ${JWS_HOME}/bin/setenv.sh
    # Remove the ors folder and war file from the webapps folder
    rm -rf ${JWS_HOME}/webapps/ors
    rm -rf ${JWS_HOME}/webapps/ors.war
    # Remove the ors user
    userdel %{ors_user}
    # Remove the ors group
    groupdel %{ors_group}
fi
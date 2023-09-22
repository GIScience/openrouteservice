%define java_version 17
%define ors_version %{getenv:ORS_VERSION}
%define ors_local_folder /tmp/openrouteservice
%define tomcat_user tomcat
%define ors_group openrouteservice
%define ors_user  openrouteservice
%define jws_config_folder /etc/opt/rh/scls/jws5/tomcat/conf.d
%define jws_webapps_folder /var/opt/rh/scls/jws5/lib/tomcat/webapps
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
Requires:  java-%{java_version}-openjdk-headless
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

# Check if the environment variables are set for JWS_CONF_FOLDER and JWS_WEBAPPS_FOLDER if not, set the default values for them
if [ -z "${JWS_CONF_FOLDER}" ]; then
    echo "JWS_CONF_FOLDER is not set. Setting default value of %{jws_config_folder}."
    jws_config_folder=%{jws_config_folder}
else
    jws_config_folder=${JWS_CONF_FOLDER}
fi

# Do the same for the JWS_WEBAPPS_FOLDER
if [ -z "${JWS_WEBAPPS_FOLDER}" ]; then
    echo "JWS_WEBAPPS_FOLDER is not set. Setting default value of %{jws_webapps_folder}."
    jws_webapps_folder=%{jws_webapps_folder}
else
    jws_webapps_folder=${JWS_WEBAPPS_FOLDER}
fi

# Set the remaining variables
jws_config_location=${jws_config_folder}/openrouteservice.conf

# Check for the JWS home ENV variable to be set and echo 'set'
if [ -n "${ORS_HOME}" ]; then
    echo "ORS_HOME found. Attempting ORS installation at ${ORS_HOME}"
else
    echo "ORS_HOME is not set. Exiting installation."
    # Exit the rpm installation with an error
    exit 1
fi

if [ -d ${jws_config_folder} ]; then
    echo "JWS webapps dir found at ${jws_config_folder}"
else
    echo "No webapps folder found. Exiting installation."
    # Exit the rpm installation with an error
    exit 1
fi
# Get the max amount of ram available on the system with cat /proc/meminfo and store in a variable and deduct 4 GB from it if it is more than 4 GB
max_ram=$(cat /proc/meminfo | awk '/^MemTotal:/{print $2}')
if [ ${max_ram} -gt 4000000 ]; then
    max_ram=$((${max_ram}-4000000))
fi
# Set min_ram with half of max_ram
min_ram=$((${max_ram}/2))


if [ -f ${jws_config_location} ]; then
    echo "Custom Tomcat config found at ${jws_config_location}. Not overriding it."
else
    echo "Creating custom Tomcat config at ${jws_config_location}."
    echo "Permanently saving the given ORS_HOME=${ORS_HOME} in ${jws_config_location}."
    echo "ORS_HOME=${ORS_HOME}" >> ${jws_config_location}
    echo "Permanently saving -Xms${min_ram}k and -Xmx${max_ram}k in ${jws_config_location}."
    echo 'CATALINA_OPTS="$CATALINA_OPTS -Xms'"${min_ram}"'k -Xmx'"${max_ram}"'k"' >> ${jws_config_location}
fi


# Check for the existence of an old ors installation in the webapps folder and clean it.
if [ -d ${jws_webapps_folder}/ors ]; then
    echo "JWS webapps dir found with old ors installation at ${jws_webapps_folder}. Cleaning it."
    rm -rf ${jws_webapps_folder}/ors
fi

# Create the correct group for the ors installation
getent group %{ors_group} >/dev/null || groupadd -r %{ors_group}

# Check if the tomcat user exists and add it to the ors group
if id -u %{tomcat_user} >/dev/null 2>&1; then
    echo "tomcat user exists. Adding it to the ors group for data access."
    usermod -a -G %{ors_group} %{tomcat_user}
else
    echo "tomcat user does not exist. Skipping adding it to the ors group."
fi

# When tomcat user does not exist, exit with an error
if ! id -u %{tomcat_user} >/dev/null 2>&1; then
    echo "tomcat user does not exist. Unknown tomcat setup. Exiting installation."
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
    useradd -r -g %{ors_group} -d ${ORS_HOME} -s /sbin/nologin %{ors_user}
fi


# Setup openrouteservice opt folder
mkdir -p "${ORS_HOME}/.graphs"
mkdir -p "${ORS_HOME}/logs"
mkdir -p "${ORS_HOME}/config"
mkdir -p "${ORS_HOME}/files"
mkdir -p "${ORS_HOME}/.elevation-cache"

%post
echo "Copy %{ors_version}_ors.war to ${jws_webapps_folder}"
cp -f %{ors_local_folder}/config/example-config.json ${ORS_HOME}/config/example-config.json
cp -f %{ors_local_folder}/.war-files/%{ors_version}_ors.war ${jws_webapps_folder}/ors.war

# Switch to the installed java version
alternatives --set java $(readlink -f /etc/alternatives/jre_%{java_version})/bin/java

chown %{tomcat_user} ${jws_webapps_folder}/ors.war
chmod 740 ${jws_webapps_folder}/ors.war
# Set the correct permissions for the /opt/openrouteservice folder so that the ${ors_group} can read and write to it
chown -R %{ors_user}:%{ors_group} ${ORS_HOME}
# Set recursive 770 permissions for the /opt/openrouteservice folder so that the ${ors_group} can read and write to it
chmod -R 770 ${ORS_HOME}

%postun
# Uninstall routine if $1 is 0 but leave the opt folder
# For explanation check https://docs.fedoraproject.org/en-US/packaging-guidelines/Scriptlets/#_syntax
if [ "$1" = "0" ]; then
    echo "Uninstalling openrouteservice"
    # Remove custom tomcat config file
    rm -rf ${jws_config_location}
    # Remove the ors folder and war file from the webapps folder
    rm -rf ${jws_webapps_folder}/ors
    rm -rf ${jws_webapps_folder}/ors.war
    rm -rf %{ors_local_folder}
    # Remove the ors user
    userdel %{ors_user}
    # Remove the ors group
    groupdel %{ors_group}
fi
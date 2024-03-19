%define java_version 17
%define ors_version %{getenv:ORS_VERSION}
%define tomcat_user tomcat
%define ors_group openrouteservice
%define ors_user  openrouteservice
%define jws_config_folder /etc/opt/rh/scls/jws6/tomcat/conf.d
%define jws_webapps_folder /var/opt/rh/scls/jws6/lib/tomcat/webapps
# for osm.pbf and config files
%define ors_etc_folder /etc/openrouteservice
# for graphs, logs, elevation_cache
%define ors_var_folder /var/openrouteservice
%define jws_permanent_state_file %{ors_etc_folder}/.openrouteservice-jws6-permanent-state
%define rpm_state_dir %{_localstatedir}/lib/rpm-state/openrouteservice
%define ors_temporary_files_location %{rpm_state_dir}/install
Name: openrouteservice-jws6
Version: %{ors_version}
Release: 1
Summary: openrouteservice WAR deployment with jws6
License: GPL 3
BuildArch: noarch
Requires:  jws6-runtime
Requires:  jws6-tomcat
Requires:  java-%{java_version}-openjdk-headless
Vendor: HeiGIT gGmbH

# For a detailed step explanation see: https://docs.fedoraproject.org/en-US/packaging-guidelines/Scriptlets/#_syntax

%description
The openrouteservice API provides global spatial services by consuming user-generated and collaboratively collected free geographic data directly from http://www.openstreetmap.org.
It is highly customizable, performant and written in Java.
This rpm package looks for an installation of JBoss JWS and installs the WAR file accordingly.

%install
mkdir -p %{buildroot}%{ors_temporary_files_location}/.war-files/
mkdir -p %{buildroot}%{ors_temporary_files_location}/config/
# Copy the ors.war file to the .war-files folder
cp -f ors.war %{buildroot}%{ors_temporary_files_location}/.war-files/%{ors_version}_ors.war
# Copy the example-ors-config.yml file to the config folder
cp -f ors-config.yml %{buildroot}%{ors_temporary_files_location}/config/example-ors-config.yml

%files
# 770 for all permissions for owner and group
%defattr(770,-,-,-)
"%{ors_temporary_files_location}/.war-files/%{ors_version}_ors.war"
"%{ors_temporary_files_location}/config/example-ors-config.yml"

%pre
###############################################################################################################
# This is the pre-installation scriptlet for the openrouteservice-jws6 rpm package.
# It checks for the existence of the JWS_HOME environment variable and checks if the webapps folder exists.
# It also checks if the JWS_CONF_FOLDER and JWS_WEBAPPS_FOLDER environment variables are set and if not, sets them to the default values.
# All variables are saved in the %{rpm_state_dir}/openrouteservice-jws6-temp-home-state file for later stages.
###############################################################################################################

# Check for the installation folders of ors to be present
echo "Creating folders %{ors_etc_folder} and %{ors_var_folder}"
mkdir -p %{ors_etc_folder}
mkdir -p %{ors_var_folder}

# Check if the environment variables are set for JWS_CONF_FOLDER and JWS_WEBAPPS_FOLDER if not, set the default values for them
if [ -z "${JWS_CONF_FOLDER}" ]; then
    echo "JWS_CONF_FOLDER is not explicitly set. Setting default value to %{jws_config_folder}."
    jws_config_folder=%{jws_config_folder}
else
    jws_config_folder=${JWS_CONF_FOLDER}
fi

# Do the same for the JWS_WEBAPPS_FOLDER
if [ -z "${JWS_WEBAPPS_FOLDER}" ]; then
    echo "JWS_WEBAPPS_FOLDER is not explicitly set. Setting default value to %{jws_webapps_folder}."
    jws_webapps_folder=%{jws_webapps_folder}
else
    jws_webapps_folder=${JWS_WEBAPPS_FOLDER}
fi

# Check if webapps folder exists
if [ -d ${jws_webapps_folder} ]; then
    echo "JWS webapps dir found at ${jws_webapps_folder}"
else
    echo "No webapps folder found. Exiting installation."
    # Exit the rpm installation with an error
    exit 1
fi

if [ -d ${jws_config_folder} ]; then
    echo "JWS conf.d dir found at ${jws_config_folder}"
else
    echo "No conf.d folder found. Exiting installation."
    # Exit the rpm installation with an error
    exit 1
fi

# Save all variables in the ORS_HOME in a file called openrouteservice-jws6-permanent-state
echo "jws_webapps_folder=${jws_webapps_folder}" > %{jws_permanent_state_file}
echo "jws_config_location=${jws_config_folder}/openrouteservice.conf" >> %{jws_permanent_state_file}

%preun
###############################################################################################################
# This is the pre-uninstallation scriptlet for the openrouteservice-jws6 rpm package.
# It does the same procedure as the pre-installation scriptlet since the %pre step isn't called when uninstalling.
###############################################################################################################
# Uninstall routine if $1 is 0 but leave the opt folder
# For explanation check https://docs.fedoraproject.org/en-US/packaging-guidelines/Scriptlets/#_syntax
if [ $1 -eq 0 ]; then
    # Check for the JWS home ENV variable to be set and echo 'set'
    echo "Uninstalling openrouteservice"
fi

%post
###############################################################################################################
# This is the post-installation scriptlet for the openrouteservice-jws6 rpm package.
# It sources the %{rpm_state_dir}/openrouteservice-jws6-temp-home-state file and uses the variables to install the ors.war file in the correct location.
# It also creates the correct user and group for the ors installation and sets the correct permissions for the ors home folder.
# It also creates a custom tomcat config file at the correct location if it does not exist yet.
# The webapps folder is filled with the ors.war file and the "new" example-ors-config.yml file is copied to the config folder.
###############################################################################################################

# Source the openrouteservice-jws6-permanent-state file from ors_etc_folder to get the permanent variables
. %{jws_permanent_state_file}

# Get the max amount of ram available on the system with cat /proc/meminfo and store in a variable and deduct 4 GB from it if it is more than 4 GB
max_ram=$(cat /proc/meminfo | awk '/^MemTotal:/{print $2}')
if [ ${max_ram} -gt 4000000 ]; then
    max_ram=$((${max_ram}-4000000))
fi

# Set min_ram with half of max_ram
min_ram=$((${max_ram}/2))

# Install routine
if [ -f ${jws_config_location} ]; then
    echo "Custom Tomcat config found at ${jws_config_location}. Not overriding it."
else
    echo "Creating custom Tomcat config at ${jws_config_location}."
    echo "Permanently saving -Xms${min_ram}k and -Xmx${max_ram}k in ${jws_config_location}."
    echo 'CATALINA_OPTS="-Xms'"${min_ram}"'k -Xmx'"${max_ram}"'k \
    -Duser.dir=%{ors_var_folder} \
    -Duser.home=%{ors_var_folder} \
    "' > ${jws_config_location}
    echo "JAVA_OPTS=\"\
    -Duser.dir=%{ors_var_folder} \
    -Duser.home=%{ors_var_folder} \
    \"" >> ${jws_config_location}
    echo "export ORS_LOG_ROTATION='0 0 0 * * ?'" >> ${jws_config_location}
fi

echo "Creating service file override at /etc/systemd/system/jws6-tomcat.service.d/override.conf"
mkdir -p /etc/systemd/system/jws6-tomcat.service.d
echo "[Service]" > /etc/systemd/system/jws6-tomcat.service.d/override.conf
echo "WorkingDirectory=%{ors_etc_folder}" >> /etc/systemd/system/jws6-tomcat.service.d/override.conf

echo "Setup the openrouteservice user and group"
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
    echo "tomcat user does not exist. Unknown tomcat setup. Exiting installation."
    # Exit the rpm installation with an error
    exit 1
fi

# Check if the ors user exists and add it to the ors group. If it doesn't exist, create it.
# This user will be used as a user placeholder for package related files
if id -u %{ors_user} >/dev/null 2>&1; then
    echo "openrouteservice user exists. Adding it to the openrouteservice group for data access."
    usermod -a -d %{ors_var_folder} -G %{ors_group} %{ors_user}
else
    echo "openrouteservice user does not exist. Creating it and adding it to the ors group."
    useradd -r -d %{ors_var_folder} -g %{ors_group} -s /sbin/nologin %{ors_user}
fi

echo "Setup openrouteservice folder structure"
# Setup openrouteservice opt folder
mkdir -p "%{ors_etc_folder}"
mkdir -p "%{ors_var_folder}/logs"
mkdir -p "%{ors_var_folder}/graphs/car"
mkdir -p "%{ors_var_folder}/elevation_cache"

echo "Copy %{ors_version}_ors.war to ${jws_webapps_folder}"
cp -f %{ors_temporary_files_location}/config/example-ors-config.yml %{ors_etc_folder}/example-ors-config.yml
cp -f %{ors_temporary_files_location}/.war-files/%{ors_version}_ors.war ${jws_webapps_folder}/ors.war

# Rewrite the example-ors-config.yml file so that line containing source_file: points to the correct location
echo "Rewriting the example-ors-config.yml file so that line containing source_file: points to the correct location"
sed -i "s|source_file:.*|source_file: %{ors_etc_folder}/osm-file.osm.pbf|" %{ors_etc_folder}/example-ors-config.yml

# Switch to the installed java version
echo "Switching the default java to version %{java_version}"
alternatives --install /usr/bin/java java $(readlink -f /etc/alternatives/jre_%{java_version})/bin/java 1
alternatives --set java $(readlink -f /etc/alternatives/jre_%{java_version})/bin/java

echo "Fixing permissions for the ors installation"
# chown to the tomcat user and ors group and give owner read and write permissions and group read and execute permissions
chown %{tomcat_user} ${jws_webapps_folder}/ors.war
chmod 440 ${jws_webapps_folder}/ors.war
# Set ownership of the ors_etc_folder folder to the ors user and ors group
chown -R %{ors_user}:%{ors_group} %{ors_etc_folder}
# Set ownership of the ors_var_folder folder to the tomcat user and ors group
chown -R %{ors_user}:%{ors_group} %{ors_var_folder}
# Make everything 770 for Owner read+write and Group read+write and the ability to create folders.
chmod -R ug+rwx %{ors_var_folder}/
chmod -R ug+rwx %{ors_etc_folder}/
# Make exceptions for example-ors-config.yml and the permanent state file with only read access on the files
chmod 440 %{ors_etc_folder}/example-ors-config.yml
chmod 440 %{jws_permanent_state_file}

%postun
###############################################################################################################
# This is the post-uninstallation scriptlet for the openrouteservice-jws6 rpm package.
# It sources the %{rpm_state_dir}/openrouteservice-jws6-temp-home-state file and uses the variables to uninstall the ors.war file in the correct location.
# It also removes the custom tomcat config file at the correct location if it exists.
# The webapps folder is cleaned from the ors.war file and the ors folder.
# The rpm_state_dir is cleaned to clean the environment for the next time..
###############################################################################################################
# Uninstall routine if $1 is 0 but leave the opt folder
# For explanation check https://docs.fedoraproject.org/en-US/packaging-guidelines/Scriptlets/#_syntax
# Source the openrouteservice-jws6-permanent-state file from ${ORS_HOME} to get the permanent variables
. %{jws_permanent_state_file}

if [ "$1" = "0" ]; then
    echo "Uninstalling openrouteservice"
    # Remove custom tomcat config file
    rm -rf ${jws_config_location}
    # Remove the ors folder and war file from the webapps folder
    rm -rf ${jws_webapps_folder}/ors
    rm -rf ${jws_webapps_folder}/ors.war
    # Remove the rpm_state_dir
    rm -rf %{rpm_state_dir}
    # Remove the ors user
    userdel %{ors_user}
    # Remove the ors group
    groupdel %{ors_group}
    # Remove any temp folders
    rm -rf %{ors_temporary_files_location}
    # Remove the permanent variables
    rm -rf %{jws_permanent_state_file}
fi

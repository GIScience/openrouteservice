%define java_version 17
%define ors_version %{getenv:ORS_VERSION}
%define tomcat_user tomcat
%define ors_group openrouteservice
%define ors_user  openrouteservice
%define jws_config_folder /etc/opt/rh/scls/jws5/tomcat/conf.d
%define jws_webapps_folder /var/opt/rh/scls/jws5/lib/tomcat/webapps
%define rpm_state_dir %{_localstatedir}/lib/rpm-state/openrouteservice_selinux
%define ors_temporary_files_location %{rpm_state_dir}/install
Name: openrouteservice-jws5-selinux
Version: %{ors_version}
Release: 1
Summary: Apply SELinux rules for openrouteservice-jws5
License: GPL 3
BuildArch: noarch
Requires:  openrouteservice-jws5 = %{ors_version}
Requires:  jws5-tomcat-selinux
Vendor: HeiGIT gGmbH

# For a detailed step explanation see: https://docs.fedoraproject.org/en-US/packaging-guidelines/Scriptlets/#_syntax

%description
This rpm package applies SELinux rules to the openrouteservice home folder

%install

%files

%pre
###############################################################################################################
# This is the pre-installation scriptlet for the openrouteservice-jws5 rpm package.
# It checks for the existence of the JWS_HOME environment variable and checks if the webapps folder exists.
# It also checks if the JWS_CONF_FOLDER and JWS_WEBAPPS_FOLDER environment variables are set and if not, sets them to the default values.
# All variables are saved in the %{rpm_state_dir}/openrouteservice-jws5-temp-home-state file for later stages.
###############################################################################################################

# Check for the JWS home ENV variable to be set and echo 'set'
if [ -n "${ORS_HOME}" ]; then
    echo "ORS_HOME variable found. Attempting ORS installation at ${ORS_HOME}."
    mkdir -p ${ORS_HOME} %{ors_temporary_files_location}
    echo "ORS_HOME=${ORS_HOME}" > %{rpm_state_dir}/openrouteservice-jws5-temp-home-state
else
    echo "ORS_HOME is not set. Exiting installation."
    # Exit the rpm installation with an error
    exit 1
fi

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

# Set the remaining variables
jws_config_location=${jws_config_folder}/openrouteservice.conf

# Save all variables in the ORS_HOME in a file called openrouteservice-jws5-permanent-state
echo "jws_webapps_folder=${jws_webapps_folder}" > ${ORS_HOME}/.openrouteservice-jws5-permanent-state
echo "jws_config_location=${jws_config_location}" >> ${ORS_HOME}/.openrouteservice-jws5-permanent-state

%preun

%post

# Source the openrouteservice-jws5-permanent-state file from %{rpm_state_dir}
. %{rpm_state_dir}/openrouteservice-jws5-temp-home-state
# Source the openrouteservice-jws5-permanent-state file from ${ORS_HOME} to get the permanent variables
. ${ORS_HOME}/.openrouteservice-jws5-permanent-state

# copy permissions of jws tomcat folder to openrouteservice home folder
semanage fcontext -a -t jws5_tomcat_var_lib_t ${ORS_HOME}
restorecon -vvRF /opt/openrouteservice

%postun

#!/bin/bash

# Function to display usage information
display_usage() {
    echo "This script is used to setup the openrouteservice environment on a debian 12 system with an external tomcat and systemd."
    echo "Minimal usage: install.sh -w <WAR_FILE>"
    echo "Full usage: install.sh -v <TOMCAT_VERSION> -u <USER> -t <TOMCAT_USER> -g <TOMCAT_GROUP> -w <WAR_FILE>"
    echo "Example to install Tomcat 10 for user ors:"
    echo ">> sudo install.sh -t 10 -u ors -w /path/to/ors.war"
}

# Function to log error messages
log_error() {
   echo -e "\e[31m✗ Error: $1\e[0m"
   return 1
}

# Function to log success messages
log_success() {
   echo -e "\e[32m✓ $1\e[0m"
   return 0
}

# Parse command-line options
while getopts ":v:u:w:t:g:h" opt; do
    case ${opt} in
        h)
            display_usage
            exit 0
            ;;
        v)
            TOMCAT_VERSION=${OPTARG:-10}
            ;;
        u)
            USER=${OPTARG:-ors}
            ;;
        t)
            TOMCAT_USER=${OPTARG:-tomcat}
            ;;
        g)
            TOMCAT_GROUP=${OPTARG:-tomcat}
            ;;
        w)
            WAR_FILE=${OPTARG}
            ;;
        \?)
            log_error "Invalid option: -$OPTARG" >&2
            display_usage
            exit 1
            ;;
        :)
            log_error "Option -$OPTARG requires an argument." >&2
            display_usage
            exit 1
            ;;
    esac
done

# Set default values
TOMCAT_VERSION=${TOMCAT_VERSION:-10}
USER=${USER:-ors}
TOMCAT_USER=${TOMCAT_USER:-tomcat}
TOMCAT_GROUP=${TOMCAT_GROUP:-tomcat}


# Ensure required options are set
if [ -z "$TOMCAT_VERSION" ] || [ -z "$USER" ] || [ -z "$WAR_FILE" ] || [ -z "$TOMCAT_USER" ] || [ -z "$TOMCAT_GROUP" ]; then
  log_error "Missing one of the following options:"
  echo "-v $TOMCAT_VERSION"
  echo "-u $USER"
  echo "-t $TOMCAT_USER"
  echo "-g $TOMCAT_GROUP"
  echo "-w $WAR_FILE"
  display_usage
  exit 1
fi

# Ensure war file exists
if [ ! -f "$WAR_FILE" ]; then
  log_error "War file does not exist: $WAR_FILE"
  display_usage
  exit 1
fi

# Ensure script is run as root
if [ "$EUID" -ne 0 ]; then
    log_error "Please run as root."
    exit 1
fi

# Check that the script is running on debian and the version is 12.x
if [ ! -f "/etc/debian_version" ] || [[ "$(cat /etc/debian_version)" != "12."* ]]; then
  log_error "This script is only supported on debian 12."
  exit 1
fi

# Print a good message with script parameters
echo "Script started with settings:"
echo "TOMCAT_VERSION: $TOMCAT_VERSION"
echo "USER: $USER"
echo "TOMCAT_USER: $TOMCAT_USER"
echo "TOMCAT_GROUP: $TOMCAT_GROUP"
echo "WAR_FILE: $WAR_FILE"

# Define variables
HOME_FOLDER="/home/$USER"
TOMCAT_FOLDER="/opt/tomcat/$TOMCAT_VERSION"

# Install dependencies
if ! apt-get update; then
    log_error "Failed to update apt."
    exit 1
fi

if ! apt-get install -y curl openjdk-17-jdk-headless systemd init maven; then
    log_error "Failed to install dependencies."
    exit 1
fi

# Create tomcat group if it does not exist
if ! getent group "$TOMCAT_GROUP" &>/dev/null; then
    if ! groupadd "$TOMCAT_GROUP"; then
        log_error "Failed to create $TOMCAT_GROUP group."
        exit 1
    fi
fi

# Create tomcat user if it does not exist
if ! id -u "$TOMCAT_USER" &>/dev/null; then
    if ! useradd -g "$TOMCAT_GROUP" -m -d "$TOMCAT_FOLDER" -s /bin/false "$TOMCAT_USER"; then
        log_error "Failed to create $TOMCAT_USER user."
        exit 1
    fi
fi

# Create user if it does not exist
if ! id -u "$USER" &>/dev/null; then
    if ! useradd -g "$TOMCAT_GROUP" -m -d "$HOME_FOLDER" -s /bin/bash "$USER"; then
        log_error "Failed to create $USER."
        exit 1
    fi
fi

# Add user to tomcat group in case the user already existed
if ! usermod -aG "$TOMCAT_GROUP" "$USER"; then
    log_error "Failed to add $USER to $TOMCAT_USER group."
    exit 1
fi

# Add tomcat to tomcat group in case the user already existed
if ! usermod -aG "$TOMCAT_GROUP" "$TOMCAT_USER"; then
    log_error "Failed to add $TOMCAT_USER to $TOMCAT_USER group."
    exit 1
fi

# Download and install tomcat
latest_tomcat_version=$(curl -s "https://dlcdn.apache.org/tomcat/tomcat-${TOMCAT_VERSION}/" | grep -Po '(?<=(<a href="v)).*(?=/">v)' | sort -V | tail -n 1)
TOMCAT_URL="https://dlcdn.apache.org/tomcat/tomcat-${TOMCAT_VERSION}/v${latest_tomcat_version}/bin/apache-tomcat-${latest_tomcat_version}.tar.gz"
OUTPUT_FILENAME="apache-tomcat-${latest_tomcat_version}.tar.gz"
if ! curl -o "/tmp/$OUTPUT_FILENAME" "$TOMCAT_URL"; then
    log_error "Failed to download Tomcat."
    exit 1
fi

# Create tomcat folder
if ! mkdir -p "$TOMCAT_FOLDER"; then
    log_error "Failed to create $TOMCAT_FOLDER."
    exit 1
fi

# Extract tomcat archive
if ! tar xzf "/tmp/$OUTPUT_FILENAME" -C "$TOMCAT_FOLDER" --strip-components=1; then
    log_error "Failed to extract Tomcat archive."
    exit 1
fi

# Remove the downloaded archive
if ! rm -rf "$TOMCAT_FOLDER/webapps/*"; then
    log_error "Failed to remove webapps."
    exit 1
fi

# Populate the tomcat folders
for folder in "$TOMCAT_FOLDER/logs" "$TOMCAT_FOLDER/temp" "$TOMCAT_FOLDER/conf" "$TOMCAT_FOLDER/webapps"; do
    mkdir -p "$folder" || { log_error "Failed to create $folder"; exit 1; }
done

# Populate the USER folders
for folder in "$HOME_FOLDER/logs" "$HOME_FOLDER/graphs"; do
    mkdir -p "$folder" || { log_error "Failed to create $folder"; exit 1; }
done

# Copy the war file to the tomcat webapps folder
if ! cp -f "$WAR_FILE" "$TOMCAT_FOLDER/webapps/ors.war"; then
    log_error "Failed to copy war file."
    exit 1
fi

# Copy setenv file to the user folder
if ! cp -f .integration-scenarios/debian-12-tomcat-war-systemd/setup/setenv.sh "$HOME_FOLDER/setenv.sh"; then
    log_error "Failed to copy setenv.sh to $HOME_FOLDER/."
    exit 1
fi

# Create the setenv. sh in the tomcat bin folder and source the user setenv.sh from there
if ! echo ". $HOME_FOLDER/setenv.sh" > "$TOMCAT_FOLDER/bin/setenv.sh"; then
    log_error "Failed to source setenv.sh."
    exit 1
fi

# Create systemd service file
if ! mkdir -p /etc/systemd/system; then
    log_error "Failed to create /etc/systemd/system."
    exit 1
fi
cat <<EOF > /etc/systemd/system/openrouteservice.service
[Unit]
Description=Apache Tomcat Web Application Container for openrouteservice
After=network.target

[Service]
Type=forking
# Decide where ors will look for the ors-config.yml file
WorkingDirectory=$HOME_FOLDER
Environment=CATALINA_PID=$TOMCAT_FOLDER/temp/tomcat.pid
Environment=CATALINA_HOME=$TOMCAT_FOLDER
Environment=CATALINA_BASE=$TOMCAT_FOLDER
Environment=JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
# CATALINA_OPTS are extended by $HOME_FOLDER/setenv.sh
Environment=CATALINA_OPTS=""
# JAVA_OPTS are extended by $HOME_FOLDER/setenv.sh
Environment=JAVA_OPTS=""
ExecStart=$TOMCAT_FOLDER/bin/startup.sh
ExecStop=$TOMCAT_FOLDER/bin/shutdown.sh
User=$TOMCAT_USER
Group=$TOMCAT_GROUP
UMask=0777
Restart=always

[Install]
WantedBy=multi-user.target
EOF

# Set correct permissions for the systemd service file
if ! chmod 664 "/etc/systemd/system/openrouteservice.service"; then
    log_error "Failed to set permissions for openrouteservice.service."
    exit 1
fi
# Copy relevant files
if ! cp -rf ors-api/src/test/files "$HOME_FOLDER/files"; then
    log_error "Failed to copy heidelberg.osm.gz."
    exit 1
fi

if ! cp -rf ors-api/src/test/files/elevation "$HOME_FOLDER/elevation_cache"; then
    log_error "Failed to copy elevation."
    exit 1
fi

if ! cp -f ors-config.yml "$HOME_FOLDER/ors-config.yml"; then
    log_error "Failed to copy ors-config.yml."
    exit 1
fi

# Fix permissions and ownerships
if ! chown -R "$TOMCAT_USER:tomcat" "$TOMCAT_FOLDER/"; then
    log_error "Failed to set permissions and ownerships for $TOMCAT_FOLDER."
    exit 1
fi

if ! chown -R "$USER:tomcat" "$HOME_FOLDER/"; then
    log_error "Failed to set permissions and ownerships for $HOME_FOLDER."
    exit 1
fi

if ! chgrp -R tomcat "$TOMCAT_FOLDER" "$HOME_FOLDER"; then
    log_error "Failed to set permissions."
    exit 1
fi

if ! chmod -R 770 "$TOMCAT_FOLDER/conf" "$TOMCAT_FOLDER/logs" "$HOME_FOLDER"; then
    log_error "Failed to set permissions."
    exit 1
fi

cat <<EOF > "$HOME_FOLDER/README.md"
Tomcat $TOMCAT_VERSION has been installed with a systemd service file to start and stop the service.
Alongside, a user '$USER' has been created and added to the tomcat group.
The user should be used to do further configurations in the $HOME_FOLDER folder.
--------------------- Manual steps ------------------------
0. Investigate the service file with: sudo systemctl cat openrouteservice
1. Adjust the default ors-config.yml and setenv.sh files in $HOME_FOLDER
Note: Everything defined in setenv.sh overwrites the default ors-config.yml file via ENVs.
Note: The default configs run an example setup with the heidelberg.osm.gz file. You should replace this with your own data.
2. Start tomcat using the command: sudo systemctl start openrouteservice
3. Check tomcat logs with: cat $TOMCAT_FOLDER/logs/catalina.out
4. Check ors logs with: cat $HOME_FOLDER/logs/ors.log
5. Make the openrouteservice tomcat process permanent: sudo systemctl enable openrouteservice
-----------------------------------------------------------
EOF

log_success "Successfully installed Tomcat $TOMCAT_VERSION with systemd service file. Check the README.md file in $HOME_FOLDER for further instructions."
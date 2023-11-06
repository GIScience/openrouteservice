#!/bin/bash
# Get container name from first argument or use default
container_name=${1:-"ors-selinux-test"}
# Get ORS version from second argument or use default
ORS_VERSION=${2:-"7.2"}
# Recreate the container env
recreate_container=${3:-"false"}
# Org to connect to register to redhat
redhat_org=${4:-""}
# Activation key to connect to register to redhat
redhat_activation_key=${5:-""}

check_container_running() {
    lxc exec "$container_name" ls 2>/dev/null  # Redirect error output to /dev/null
    return $?
}

wait_for_container_ready() {
    local max_attempts=30  # Set the maximum number of attempts
    local attempts=0
    while [ $attempts -lt $max_attempts ]; do
        if check_container_running; then
            echo "Successfully connected to the console of $container_name."
            break
        else
            echo "Waiting for $container_name to be ready (Attempt $((attempts + 1)) of $max_attempts)..."
            sleep 5  # Adjust the sleep interval as needed
        fi

        attempts=$((attempts + 1))
    done

    if [ $attempts -eq $max_attempts ]; then
        echo "Max attempts reached. $container_name is not ready."
        exit 1
    fi
}

# Write the above function with .spec as an input
build_rpm_with_spec() {
    local specfile=$1
    local ors_version=$2
    export ORS_VERSION=$ors_version
    # Get absolute path of the specfile with basepath
    specfile=$(readlink -f "$specfile")
    if [ ! -f "$specfile" ]; then
        echo "File $specfile does not exist."
        exit 1
    fi
    echo "Building RPM with specfile $specfile"
    mkdir -p ~/rpmbuild/{BUILD,RPMS,SOURCES,SPECS,SRPMS}
    cp -f ../ors-api/target/ors.war   ~/rpmbuild/BUILD/ors.war
    cp -f example-config.json ~/rpmbuild/BUILD/example-config.json
    rpmbuild -bb "$specfile"
}

# Write function that takes a filepath as an input and pushes this file to the container
push_file_to_container() {
    local filepath=$1
    local container_path=$2
    echo "Pushing file $filepath to container $container_name at $container_path"
    # Check if the file exists
    if [ ! -f "$filepath" ]; then
        echo "File $filepath does not exist."
        exit 1
    fi

    lxc file push "$filepath" "$container_name/$container_path"
}

# Write function that activates SELinux in the container
activate_selinux_in_container() {
    echo "Activating SELinux. Dont' reboot the container after this."
    lxc exec "$container_name" -- bash -c "dnf -y install selinux-policy-targeted"
    lxc exec "$container_name" -- bash -c "echo SELINUX=permissive > /etc/selinux/config"
    lxc exec "$container_name" -- bash -c "setenforce 0"
    lxc exec "$container_name" -- bash -c "sestatus"
}

# Fail if org and activation key are not set
if [ -z "$redhat_org" ] || [ -z "$redhat_activation_key" ]; then
    echo "Redhat org and activation key must be set"
    exit 1
fi

#######################################################
# Script to setup a container for testing the ORS RPM #
#######################################################

echo "Creating container for testing"
recreate=false
# Test different variances of recreate_container and depending on output assign true or false to recreate
if [ "$recreate_container" = "true" ]; then
    echo "Recreating container set to true"
    recreate=true
elif [ "$recreate_container" = "false" ] && check_container_running; then
    recreate=false
else
    echo "Container is not running or doeesn't exist. Recreating container"
    recreate=true
fi

echo "Recreate container: $recreate"
if $recreate; then
    echo "Deleting container"
    lxc delete "$container_name" -f
fi
# Check if the image already exists, and if so, use it
if $recreate && lxc image list | grep -wq "$container_name"; then
    echo "Reusing image"
    lxc launch "$container_name" "$container_name" --vm -c security.secureboot=false -c limits.cpu=4 -c limits.memory=5GiB
elif $recreate && ! lxc image list | grep -wq "$container_name"; then
    echo "Building new image"
    lxc launch images:rockylinux/8/amd64 $container_name --vm -c security.secureboot=false -c limits.cpu=4 -c limits.memory=5GiB
    if [ $? -ne 0 ]; then
        echo "Error creating the VM"
        exit 1
    fi

    wait_for_container_ready

    echo "Prepare the container for convert2rhel"
    lxc exec "$container_name" -- bash -c "curl -o /etc/pki/rpm-gpg/RPM-GPG-KEY-redhat-release https://www.redhat.com/security/data/fd431d51.txt && curl -o /etc/yum.repos.d/convert2rhel.repo https://ftp.redhat.com/redhat/convert2rhel/8/convert2rhel.repo && dnf -y update && dnf -y install convert2rhel &&  echo org = $redhat_org >> /etc/convert2rhel.ini && echo activation_key = $redhat_activation_key >> /etc/convert2rhel.ini"

    echo "Rebooting the container"
    lxc restart "$container_name"
    wait_for_container_ready

    echo "Converting the container to RHEL"
    lxc exec "$container_name" -- bash -c "convert2rhel -y"

    echo "Rebooting the container"
    lxc restart "$container_name"
    wait_for_container_ready

    echo "Subscribe"
    lxc exec "$container_name" -- bash -c "subscription-manager register --force --org $redhat_org --activationkey $redhat_activation_key"

    echo "Installing additional packages"
    lxc exec "$container_name" -- bash -c "dnf -y install https://dl.fedoraproject.org/pub/epel/epel-release-latest-8.noarch.rpm"
    lxc exec "$container_name" -- bash -c "dnf -y update && dnf -y install htop vim less audit setroubleshoot-server policycoreutils policycoreutils-python-utils setools setools-console setroubleshoot openssh-server openssh-clients"
    lxc exec "$container_name" -- bash -c "dnf group install -y jws5"

    lxc exec "$container_name" -- bash -c "sudo systemctl enable --now sshd"
    lxc exec "$container_name" -- bash -c "echo \"root:root\" | chpasswd"
    lxc exec "$container_name" -- bash -c "dnf clean all"

    # Set the ORS_HOME env variable
    lxc exec "$container_name" -- bash -c "echo \"export ORS_HOME=/opt/openrouteservice\" >> /etc/profile"

    echo "Publishing image $container_name for reuse"
    lxc stop "$container_name"
    lxc publish --reuse "$container_name" --alias "$container_name"
    lxc start "$container_name"
else
  echo "Not recreating container"
fi

# To activate SELinux in the container, run the following
wait_for_container_ready
activate_selinux_in_container

echo "Building RPM"
# Call build_rpm_with_spec and save result in variable rpm_path
mvn clean package -T14 -DskipTests -f ../pom.xml
build_rpm_with_spec ors-war.spec "$ORS_VERSION"
build_rpm_with_spec ors-selinux.spec "$ORS_VERSION"

echo "Pushing RPM to container"
push_file_to_container "$(readlink -f ~/rpmbuild/RPMS/noarch/openrouteservice-jws5-$ORS_VERSION-1.noarch.rpm)" /tmp/ors.rpm
push_file_to_container "$(readlink -f ~/rpmbuild/RPMS/noarch/openrouteservice-jws5-selinux-$ORS_VERSION-1.noarch.rpm)" /tmp/ors-selinux.rpm

echo "Install the ors RPM"
# Create /opt/openrouteservice
lxc exec "$container_name" -- bash -c "mkdir -p /opt/openrouteservice"

# Install the ors rpm with exporting ORS_HOME as an env
lxc exec "$container_name" -- bash -c "export ORS_HOME=/opt/openrouteservice; dnf install -y /tmp/ors.rpm /tmp/ors-selinux.rpm"

# Print the local ip address of the lxc container and assign it to the variable ip_address
ip_address=$(lxc exec "$container_name" -- ip addr show enp5s0 | grep -w inet | awk '{print $2}' | awk -F'/' '{print $1}')
echo "###################### STATUS ######################"
echo "# Credentials: 'root:root'"
echo "# Ways to connect to the instance:"
# Show the ssh connect command use password authentication and auto accept the host key
echo "# ssh -o \"StrictHostKeyChecking=no\" -o \"PasswordAuthentication=yes\" root@$ip_address"
# Show the pure lxc connect command
echo "# lxc exec $container_name -- bash"
echo "####################################################"

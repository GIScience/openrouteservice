#!/bin/bash
#################################
# Description: Network related functions
# Author: Julian Psotta
# Date: 2024-02-24
# #################################

# Function to check if a port is open
# Usage: check_port_open <url> <timeout_sec> <exported_http_code> <sleep> <report_every>
function wait_for_url() {
  local url="$1"
  local timeout_sec="$2"
  local expected_http_code="$3"
  local sleep=${4:-2}
  local report_every=${5:-10}

  start_time=$(date +%s)
  turn=0
  while true; do
    response=$(curl -s -o /dev/null -w "%{http_code}" "${url}")
    if [[ "$response" == "${expected_http_code}" ]]; then
      log_success "Request succeeded for ${url} with expected http code ${expected_http_code}"
      return 0
    fi
    current_time=$(date +%s)
    elapsed_time=$((current_time - start_time))
    remaining_time=$(( timeout_sec - elapsed_time))
    if ((elapsed_time >= timeout_sec)); then
      log_error "Timed out waiting for response after ${timeout_sec} seconds for ${url}"
      return 1
    fi
    if [ $(( turn % report_every )) -eq "0" ]; then
      log_warning "GET request failed with response code ${response} for ${url}, retrying for another ${remaining_time} seconds..."
    fi
    (( turn+=1 ))
    sleep "$sleep"
  done
}

# Function to return a free port
# Usage: get_free_port <lower_port> <upper_port>
get_free_port() {
  # Check for open ports in a given range. Do not echo anything besides the port!
  # If no range is given, it is chosen automatically
  local lower_port=${1}
  local upper_port=${2}
  if [ -z "${lower_port}" ]; then
    read -r lower_port _ </proc/sys/net/ipv4/ip_local_port_range
  fi
  if [ -z "${upper_port}" ]; then
    read -r _ upper_port </proc/sys/net/ipv4/ip_local_port_range
  fi
  local is_free
  is_free="$(netstat -antp 2>/dev/null | grep "$lower_port")"

  while [[ -n "$is_free" ]] && [[ $lower_port -le $upper_port ]]; do
    local lower_port=$((lower_port + 1))
    is_free=$(netstat -antp 2>/dev/null | grep "$lower_port")
  done
  if [[ "$lower_port" -gt "$upper_port" ]]; then
    log_error "Could not find a free port to start the container."
    exit 1
  fi
  echo "$lower_port"
}

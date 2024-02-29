#!/usr/bin/env bash
#################################
# Description: Logging related functions
# Author: Julian Psotta
# Date: 2024-02-24
# #################################

# Debug message in cyan
function log_debug() {
  if [[ "${container_log_level_int}" -le 10 ]]; then
    echo -e "\e[36m▢ $1\e[0m"
  fi
  return 0
}
# Success message in green. Always printed
function log_success() {
  if [[ "${container_log_level_int}" -le 20 ]]; then
    echo -e "\e[32m✓ $1\e[0m"
  fi
  return 0
}
# Info message in blue
function log_info() {
  if [[ "${container_log_level_int}" -le 30 ]]; then
    echo -e "\e[34mⓘ $1\e[0m"
  fi
  return 0
}
# Warning message in yellow
function log_warning() {
  if [[ "${container_log_level_int}" -le 40 ]]; then
    echo -e "\e[33m⚠ $1\e[0m"
  fi
  return 0
}
# Error message in red.
function log_error() {
  echo -e "\e[31m✗ $1\e[0m"
  return 1
}
function set_log_level() {
  local log_level=${1:-SUCCESS}
  case ${log_level} in
  "DEBUG")
    container_log_level_int=10
    ;;
  "SUCCESS")
    container_log_level_int=20
    ;;
  "INFO")
    container_log_level_int=30
    ;;
  "WARN")
    container_log_level_int=40
    ;;
  "ERROR")
    container_log_level_int=50
    ;;
  *)
    log_debug "No matching log level found: ${LOG_LEVEL}."
    log_debug "Defaulting to INFO."
    LOG_LEVEL="INFO"
    container_log_level_int=20
    ;;
  esac
  log_warning "LOG_LEVEL: ${LOG_LEVEL}. Set LOG_LEVEL=DEBUG for more details."
}

# Initialize the log level as soon as the script is sourced
set_log_level "${LOG_LEVEL:-INFO}"

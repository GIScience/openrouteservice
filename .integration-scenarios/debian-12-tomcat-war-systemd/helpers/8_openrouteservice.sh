# Create a function from the above snippet
# Usage: check_number_of_profiles_loaded <url> <expected_number_of_profiles>
check_number_of_profiles_loaded() {
  local url="$1"
  local expected_number_of_profiles="$2"
  if ! curl -s "${url}" | jq ".profiles | length == ${expected_number_of_profiles}" | grep -q true; then
    log_error "Expected number of profiles ${expected_number_of_profiles} not found"
    return 1
  fi
  log_success "Expected number of ${expected_number_of_profiles} profiles are loaded."
  return 0
}

# Check specific profile is loaded
# Usage: check_profile_loaded <url> <profile_name>
check_profile_loaded() {
  local url="$1"
  local profile_name="$2"
  local profile_expected=${3:-true}
  # Get the return json and check if the profile is loaded
  response=$(curl -s "${url}")
  # If profiles not found or profiles empty return error if profile_expected is true
  if ! echo "${response}" | jq ".profiles | length > 0" | grep -q true; then
    if [[ "$profile_expected" == true ]]; then
      log_error "No profiles found in the response"
      return 1
    else
      log_success "Profile ${profile_name} is not loaded as expected."
      return 0
    fi
  fi
  # Iterate over profiles and check if the profile is loaded
  if [ $profile_expected == true ] && ! echo "${response}" | jq -e ".profiles | to_entries[] | select(.value.profiles == \"${profile_name}\")" > /dev/null; then
    log_error "Profile ${profile_name} should be loaded but is not."
    return 1
  elif [ $profile_expected == false ] && echo "${response}" | jq -e ".profiles | to_entries[] | select(.value.profiles == \"${profile_name}\")" > /dev/null; then
    log_error "Profile ${profile_name} should not be loaded but is."
    return 1
  fi

  if [ $profile_expected == true ]; then
    log_success "Profile ${profile_name} is loaded, as expected."
    return 0
  else
    log_success "Profile ${profile_name} is not loaded, as expected."
    return 0
  fi
}

# Function to check for a valid avoid area request in Heidelberg
# Usage: check_avoid_area_request <url> <expected_http_code>
check_avoid_area_request() {
  local url="$1"
  local expected_http_code="$2"
  local request_body='{"coordinates":[[8.681495,49.41461],[8.686507,49.41943],[8.687872,49.420318]],"options":{"avoid_polygons":{"coordinates":[[[8.684881031827587,49.41768066444595],[8.684881031827587,49.41699648134178],[8.685816955915811,49.41699648134178],[8.685816955915811,49.41768066444595],[8.684881031827587,49.41768066444595]]],"type":"Polygon"}}}'
  response=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${url}" -H 'Content-Type: application/json; charset=utf-8' -H 'Accept: application/geo+json; charset=utf-8' -d "${request_body}")
  if [[ "$response" == "${expected_http_code}" ]]; then
    log_success "Avoid areas request succeeded for ${url} with expected http code ${expected_http_code}"
    return 0
  fi
  log_error "Avoid areas request failed with response code ${response} for ${url}"
  return 1
}

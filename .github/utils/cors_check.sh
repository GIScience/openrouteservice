#!/bin/bash
host=${1}
port=${2}
path=${3}
origin=${4}
http_code=${5}
timeout=${6}

function wait_for_preflight() {
  local url="$1"
  local origin="$2"
  local timeout_sec="$3"
  local expected_http_code="$4"
  echo "Waiting for preflight response with Origin header: ${origin} for ${url}"

  start_time=$(date +%s)
  while true; do
    response=$(curl -s -o /dev/null -w "%{http_code}" \
      -H "Origin: ${origin}" \
      -H "Access-Control-Request-Headers: authorization,content-type" \
      -H "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/111.0" \
      -H "Accept: /" \
      -H "Accept-Language: en-US,en;q=0.5" \
      -H "Accept-Encoding: gzip, deflate, br" \
      -H "Access-Control-Request-Method: POST" \
      -H "Access-Control-Request-Headers: authorization,content-type" \
      -H "Connection: keep-alive" \
      -H "Sec-Fetch-Dest: empty" \
      -H "Sec-Fetch-Mode: cors" \
      -H "Sec-Fetch-Site: cross-site" \
      -H "Pragma: no-cache" \
      -H "Cache-Control: no-cache" \
      -X OPTIONS "${url}")
    if [[ "$response" == "${expected_http_code}" ]]; then
      echo "Preflight request succeeded for ${url} with expected http code ${expected_http_code}"
      return 0
    fi
    current_time=$(date +%s)
    elapsed_time=$((current_time - start_time))
    remaining_time=$(( timeout_sec - elapsed_time))
    if ((elapsed_time >= timeout_sec)); then
      echo "Timed out waiting for preflight response after ${timeout_sec} seconds for ${url}"
      return 1
    fi
    echo "Preflight request failed with response code ${response} for ${url}, retrying for another ${remaining_time} seconds..."
    sleep 2
  done
}

wait_for_preflight "$host:$port$path" "${origin}" "${timeout}" "${http_code}"

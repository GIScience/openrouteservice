#!/usr/bin/env bash
# Tests whether ors has built graphs and is ready.
# Test succeeds if ors is ready within maximum number of tries.
# Success: exit 0, Fail: exit 1


echo "Waiting for ORS to be ready..."

status_code=000
tries=0
while [[ "$status_code" = 000 ]]
do
# Check if ORS status is healthy
status_code=$(curl --write-out %{http_code} --silent --output /dev/null http://127.0.0.1:8080/ors/v2/health)
echo "HTTP status code: $status_code"
tries=$((tries+=1))
if [[ "$tries" -gt 20 ]]; then
  echo "Maximum number of tries exceeded."
  exit 1
fi
sleep 10s
done

status="not ready"
tries=0
while [[ ! "$status" = "ready" ]]
do
# Check if ORS status is healthy
status=$(curl -s 'http://localhost:8080/ors/v2/health' | jq -r '.status')
echo "ORS status: $status"
tries=$((tries+=1))
if [[ "$tries" -gt 10 ]]; then
  echo "Maximum number of tries exceeded."
  exit 1
fi
sleep 10s
done

exit 0

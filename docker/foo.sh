ll1="-122.438811,37.773033"
ll2="-122.447608,37.770828"
url="http://localhost:8080/ors/health"
curl $url
exit

url="http://localhost:8080/ors/v2/directions/driving-car?start=$ll1&end=$ll2"

echo $url
exit
curl --include \
     --header "Content-Type: application/json; charset=utf-8" \
     --header "Accept: application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8" \
    $url

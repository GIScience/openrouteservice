const ROUTING_HOST = 'dev-1.wips.link';
const ROUTING_URL = `http://${ROUTING_HOST}:8080/ors/v2/directions`;
const routeCache = {};
const fetchRoute = ([ start, end ]) => {
  if (routeCache[[start, end]]) return Promise.resolve(routeCache[[start, end]]);

  return fetch(`${ROUTING_URL}/foot-walking?start=${start}&end=${end}`)
    .then((res) => res.json())
    .then((res) => {
      routeCache[[ start, end ]] = res;
      return res;
    });
}

export default fetchRoute;

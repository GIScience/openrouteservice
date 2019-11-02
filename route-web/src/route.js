import L from 'leaflet';
import buildRoutePath from './buildRoutePath';

const getRandomColor = () => {
  var letters = '0123456789ABCDEF';
  var color = '#';
  for (var i = 0; i < 6; i++) {
      color += letters[Math.floor(Math.random() * 16)];
    }
  return color;
}
const remove = (arr) => (arr || []).forEach((i) => i.remove());

const addLatlng = (prevRoute, { latlng, after }) => {
  const { path, ...rest } = prevRoute;
  const newPath = after ?
    path.reduce((memo, ll) => {
      return ll.lat === after.lat && ll.lng === after.lng ? [...memo, ll, latlng] : [...memo, ll];
    }, []) : [...path, latlng];
  return createRoute({ ...rest, path: newPath, prevRoute });
};

const undo = (nextRoute) => {
  const { prevRoute, polyline, markers, map } = nextRoute;
  const { leaflet } = map;
  remove(polyline);
  remove(markers);
  [...prevRoute.markers, ...prevRoute.polyline || []].forEach((l) => l.addTo(leaflet));

  const route = { ...prevRoute, nextRoute };

  return {
    ...route,
    buildRoutePromise: Promise.resolve(route),
  };
};

const generateMarkers = (path) =>
  path.map((latlng) =>
    L.circle(latlng, {
      radius: 20,
      stroke: false,
      fill: true,
      fillColor: getRandomColor(),
      fillOpacity: 1,
    }));

const generatePolylines = (buildRouteResponse, getPolyline) =>
  buildRouteResponse.map((route) => getPolyline(route));

const createRoute = (args) => {
  const {
    path,
    map,
    polyline,
    prevRoute,
    getPolyline,
    getPolylineInfo,
    markers: oldMarkers
  } = args;
  const { leaflet } = map;
  remove(oldMarkers);
  const markers = generateMarkers(path);
  markers.forEach((m) => m.addTo(leaflet));
  const route = {
    path,
    markers,
    polyline,
    map,
    getPolyline,
    getPolylineInfo,
    prevRoute,
  };
  const buildRoutePromise = buildRoutePath(path)
    .then((buildRouteResponse) => {
      remove(polyline);
      const newPolylines = generatePolylines(buildRouteResponse, getPolyline);
      newPolylines.forEach((p) => p.addTo(leaflet));
      return {
        ...route,
        polyline: newPolylines,
        buildRouteResponse,
      }
    });

  return { ...route, buildRoutePromise }
}

export {
  createRoute,
  addLatlng,
  undo,
  remove,
};

import buildPath from './buildPath';
import {
  addLatlng,
  moveLatlng,
  undo,
  remove,
  generateMarkers,
  generatePolylines,
} from './utils';

const createRoute = (args) => {
  const {
    path,
    map,
    polyline,
    prevRoute,
    getPolyline,
    getPolylineInfo,
    setTargetWithState,
    markers: oldMarkers
  } = args;
  const { leaflet } = map;

  remove(oldMarkers);

  const markers = generateMarkers(path, setTargetWithState);
  markers.forEach((m) => m.addTo(leaflet));

  const route = {
    path,
    markers,
    polyline,
    map,
    getPolyline,
    getPolylineInfo,
    setTargetWithState,
    prevRoute,
  };

  const buildRoutePromise = buildPath(path)
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

export default createRoute;

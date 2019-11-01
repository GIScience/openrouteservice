import L from 'leaflet';
import get from 'lodash/get';
import slice from 'lodash/slice';
import flatMap from 'lodash/flatMap';
import buildRoutePath from './buildRoutePath';

const remove = (arr) => (arr || []).forEach((i) => i.remove());

const addLatlng = ({ path, polylines, markers }, latlng) => {
  return createRoute([...path, latlng], { polylines, markers });
};

const removeLastLatLng = ({ path, polylines, markers, buildRouteResponse }) => {
  polylines.remove();
  return createRoute(
    slice(path, 0, -1),
    {
      polylines: generatePolylines(slice(buildRouteResponse, 0, -1)),
      markers,
    }
  );
};

const generateMarkers = (path) => path.map((latlng) => L.marker(latlng));
const generatePolylines = (buildRouteResponse) =>
  L.polyline(
    flatMap(buildRouteResponse, (route) =>
      get(route, 'features[0].geometry.coordinates')
        .map(([lat, lng]) => [lng, lat])
    )
  );

const createRoute = (path, additionalArgs = {}) => {
  const { polylines, markers: oldMarkers } = additionalArgs;
  remove(oldMarkers);
  const markers = generateMarkers(path);
  const route = {
    path,
    markers,
    polylines,
  };
  const buildRoutePromise = buildRoutePath(path)
    .then((buildRouteResponse) => {
      polylines && polylines.remove();
      const newPolylines = generatePolylines(buildRouteResponse);
      return {
        ...route,
        polylines: newPolylines,
        buildRouteResponse,
      }
    });
  // const update = (path) => {}

  return { ...route, buildRoutePromise }
}

export {
  createRoute,
  addLatlng,
  removeLastLatLng,
  remove,
};

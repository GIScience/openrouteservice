import L from 'leaflet';
import slice from 'lodash/slice';
import createRoute from './createRoute';

const getRandomColor = () => {
  var letters = '0123456789ABCDEF';
  var color = '#';
  for (var i = 0; i < 6; i++) {
      color += letters[Math.floor(Math.random() * 16)];
    }
  return color;
}

const remove = (arr) => (arr || []).forEach((i) => i.remove());

const sameLatlng = ({ lat: lat1, lng: lng1 }, { lat: lat2, lng: lng2 }) =>
  lat1 === lat2 && lng1 === lng2;

const addLatlng = (prevRoute, { latlng, after }) => {
  const { path, ...rest } = prevRoute;
  const newPath = after ?
    path.reduce((memo, ll) => {
      return sameLatlng(ll, after) ?
        [...memo, ll, latlng] : [...memo, ll];
    }, []) : [...path, latlng];
  return createRoute({ ...rest, path: newPath, prevRoute });
};

const moveLatlng = (prevRoute, { latlng, updatedLatlng }) => {
  const { path, ...rest } = prevRoute;
  const index = path.indexOf(latlng);
  const updatedPath = [
    ...slice(path, 0, index),
    updatedLatlng,
    ...slice(path, index + 1),
  ];
  return createRoute({ ...rest, path: updatedPath });
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

const generateMarkers = (path, onMouseDown) =>
  path.map((latlng) => {
    const circle = L.circle(latlng, {
      bubblingMouseEvents: false,
      radius: 20,
      stroke: false,
      fill: true,
      fillColor: getRandomColor(),
      fillOpacity: 1,
    });
    circle.on('mousedown', onMouseDown(circle, 'marker'))
    return circle;
  });

const generatePolylines = (buildRouteResponse, getPolyline) =>
  buildRouteResponse.map((route) => getPolyline(route));

export {
  addLatlng,
  moveLatlng,
  undo,
  remove,
  generateMarkers,
  generatePolylines,
};

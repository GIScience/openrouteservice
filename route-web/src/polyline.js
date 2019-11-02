import first from 'lodash/first';
import last from 'lodash/last';
import get from 'lodash/get';
import L from 'leaflet';
import uuid from './uuid';

const POLYLINE_OPTIONS = {
  opacity: 1,
}

const polylineFactory = (onAdd) => {
  const cache = {};
  const reverseCache = {};

  return {
    getPolyline: (buildRouteResponse, onMouseDown) => {
      const { coords } = buildRouteResponse;
      const points = get(buildRouteResponse, 'features[0].geometry.coordinates');
      const ll1 = first(coords);
      const ll2 = last(coords);
      const cacheKey = [ll1, ll2];

      if (!cache[cacheKey]) {
        const polyline = L.polyline(
          points.map(([lat, lng]) => [lng, lat]),
          POLYLINE_OPTIONS,
        )
        polyline.uuid = uuid();
        cache[cacheKey] = polyline;
        polyline.on('add', onAdd)
        reverseCache[polyline.uuid] = cacheKey;
      }

      return cache[cacheKey];
    },
    getPolylineInfo: (polyline) => reverseCache[polyline.uuid],
  }
}

export { polylineFactory };

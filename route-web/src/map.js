import L from 'leaflet';

const setTarget = (map, target, targetType) => {
  const { leaflet } = map;
  leaflet.dragging.disable();

  return {
    ...map,
    target,
    targetType,
  }
}

const unsetTarget = ({ target, targetType, ...map }) => {
  const { leaflet } = map;
  leaflet.dragging.enable();

  return map;
}

const initializeMap = ({ accessToken, id }) => {
  const leaflet = L.map(id, {
      center: [37.773033, -122.438811],
      zoom: 13,
  });

  L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
    attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
    maxZoom: 18,
    id: 'mapbox.streets',
    accessToken,
  }).addTo(leaflet);

  return { leaflet };
}

export { initializeMap, setTarget, unsetTarget }

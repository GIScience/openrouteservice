import React from 'react';
import L from 'leaflet';
import get from 'lodash/get';
import logo from './logo.svg';
import './App.css';

const ROUTING_HOST = '157.245.236.49';
const ROUTING_URL = `http://${ROUTING_HOST}:8080/ors/v2/directions`;
const routeCache = {};
const fetchRoute = ([ start, end ]) => {
  if (routeCache[[start, end]]) return routeCache[[start, end]];

  return fetch(`${ROUTING_URL}/driving-car?start=${start}&end=${end}`)
    .then((res) => res.json())
    .then((res) => {
      routeCache[[ start, end ]] = res;
      return res;
    });
}

const accessToken = process.env.TILE_SERVICE_KEY;

class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      path: [],
    };
    this.handleClick = this.handleClick.bind(this);
    this.fetchRoute = this.fetchRoute.bind(this);
  }

  render() {
    return (
      <div id="map" style={{ height: '100%' }}></div>
    );
  }

  fetchRoute() {
    const { path } = this.state;
    if (path.length === 1) return;

    Promise.all(
      path.reduce(([prev, promises], next) => {
        if (!prev) return [next, promises];
        const promise = fetchRoute([
          Object.values(prev).reverse(),
          Object.values(next).reverse()
        ]);
        return [next, [...promises, promise]]
      }, [undefined, []])[1]
    ).then((routes) => this.setState({ routes }))
  }

  handleClick({ latlng }) {
    const { path, map } = this.state;

    L.marker(latlng).addTo(map)
    this.setState({ path: [...path, latlng] })
    this.fetchRoute();
  }

  componentDidUpdate() {
    const { map, routes } = this.state;

    if (routes) {
      routes.forEach((route) => {
        const coordinates = get(route, 'features[0].geometry.coordinates')
        L.polyline(
          coordinates.map((latlng) => latlng.reverse())
        ).addTo(map)
      });
    }
  }

  componentDidMount() {
    const map = L.map('map', {
        center: [37.773033, -122.438811],
        zoom: 13,
    });

    L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
      attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
      maxZoom: 18,
      id: 'mapbox.streets',
      accessToken,
    }).addTo(map);

    map.on('click', this.handleClick)
    this.setState({ map });
  }
}

export default App;

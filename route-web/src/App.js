import React from 'react';
import L from 'leaflet';
import './App.css';
import {
  createRoute,
  addLatlng,
  removeLastLatLng,
  remove,
} from './route';

const UndoButton = ({ onClick }) => (
  <button
    style={{
      position: 'absolute',
      top: '10px',
      right: '10px',
      zIndex: 1000,
      border: '1px solid gray',
      padding: '10px',
    }}
    onClick={onClick}
  >
    Undo
  </button>
)

class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = { route: createRoute([]) };
    this.handleClick = this.handleClick.bind(this);
    this.undo = this.undo.bind(this);
  }

  render() {
    return (
      <div style={{ position: 'relative', height: '100%' }}>
        <div id="map" style={{ height: '100%' }}></div>
        <UndoButton onClick={this.undo} />
      </div>
    );
  }

  undo() {
    this.updateRoute(removeLastLatLng);
  }

  updateRoute(fn, ...args) {
    const { route: oldRoute } = this.state;
    const { markers } = oldRoute;
    remove(markers)
    const route =fn(oldRoute, ...args);
    this.setState({ route });
    route.buildRoutePromise.then(
      (hydratedRoute) => this.setState({ route: hydratedRoute })
    );
  }

  handleClick({ latlng }) {
    this.updateRoute(addLatlng, latlng);
  }

  componentDidUpdate(prevProps, prevState) {
    const { route, map } = this.state;
    const { markers, polylines } = route;
    markers.forEach((m) => m.addTo(map));
    if (polylines) {
      polylines.addTo(map);
    }
  }

  componentDidMount() {
    const { accessToken } = this.props;
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

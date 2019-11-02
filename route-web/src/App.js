import React from 'react';
import './App.css';
import {
  createRoute,
  addLatlng,
  moveLatlng,
  undo,
  remove,
} from './route';
import { polylineFactory } from './polyline';
import { initializeMap, setTarget, unsetTarget } from './map';

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
    this.state = { info: { dragging: false } };
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
    this.updateRoute(undo);
  }

  updateRoute(fn, ...args) {
    const { route: oldRoute } = this.state;
    const { markers } = oldRoute;
    remove(markers)
    const route = fn(oldRoute, ...args);
    this.setState({ route });
    route.buildRoutePromise.then(
      (hydratedRoute) => this.setState({ route: hydratedRoute })
    );
  }

  handleClick({ latlng, ...rest }) {
    const { map, route } = this.state;
    const { leaflet, target, targetType } = map;
    if (target) {
      if (targetType === 'polyline') {
        this.setState({ map: unsetTarget(map) });
        const [ll1] = route.getPolylineInfo(map.target)
        const [lng, lat] = ll1
        this.updateRoute(addLatlng, { latlng, after: { lat, lng }});
      } else if (targetType === 'marker') {
        const { _latlng: targetLatlng } = target;
        this.setState({ map: unsetTarget(map) });
        this.updateRoute(moveLatlng, { latlng: targetLatlng, updatedLatlng: latlng });
      }
    } else {
      this.updateRoute(addLatlng, { latlng });
    }
  }

  componentDidMount() {
    const { accessToken } = this.props;
    const map = initializeMap({ accessToken, id: 'map' })
    const setTargetWithState = (target, type) => () => {
      this.setState({ map: setTarget(map, target, type) });
    };
    const { getPolyline, getPolylineInfo } = polylineFactory(({ target }) => {
      target.on('mousedown', setTargetWithState(target, 'polyline'));
    });

    map.leaflet.on('click', this.handleClick)
    const route = createRoute({
      path: [],
      map,
      getPolyline,
      getPolylineInfo,
      setTargetWithState,
    });
    this.setState({ map, route });
  }
}

export default App;

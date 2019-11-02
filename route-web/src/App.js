import React from 'react';
import './App.css';
import {
  createRoute,
  addLatlng,
  moveLatlng,
  remove,
} from './route';
import { polylineFactory } from './polyline';
import { initializeMap, setTarget, unsetTarget } from './map';
import withRoute from './withRoute';
import withTarget from './withTarget';
import compose from 'recompose/compose';
import { Map, TileLayer, Circle, Polyline } from 'react-leaflet';

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
    this.state = {};
    this.handleClick = this.handleClick.bind(this);
    // this.undo = this.undo.bind(this);
  }

  render() {
    const {
      accessToken,
      setTarget,
      undo,
      path,
      lines,
      target,
      targetType,
    } = this.props;

    return (
      <div style={{position: 'relative', height: '100%'}}>
        <Map
          dragging={target === undefined}
          center={[37.773033, -122.438811]}
          zoom="15"
          style={{height: '100%'}}
          onClick={this.handleClick}
        >
          <TileLayer
            attribution='Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>'
            url={`https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=${accessToken}`}
            id='mapbox.streets'
          />
          {path.map((latlng) => (
            <Circle
              center={latlng}
              onMouseDown={(e) => setTarget('waypoint', e.target, { latlng })}
            />
          ))}
          {lines.map((coords, i) => (
            <Polyline
              positions={coords}
              onMouseDown={(e) => setTarget('polyline', e.target, { startLatlng: path[i] })}
            />
          ))}
        </Map>
        <UndoButton onClick={undo} />
      </div>
    );
  }

  // undo() {
  //   this.updateRoute(undo);
  // }

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
    const {
      appendPoint,
      movePoint,
      clearTarget,
      target,
      targetType,
      targetData,
    } = this.props;

    if (target) {
      if (targetType === 'polyline') {
        const { startLatlng } = targetData;
        clearTarget();
        appendPoint({ latlng, after: startLatlng });
      } else if (targetType === 'waypoint') {
        const { latlng: oldLatlng } = targetData;
        clearTarget();
        movePoint(oldLatlng, latlng);
      }
    } else {
      appendPoint({ latlng });
    }
  }
}

export default withRoute(withTarget(App));

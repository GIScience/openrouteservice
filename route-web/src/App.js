import React from 'react';
import compose from 'recompose/compose';
import { Map, TileLayer, Circle, Polyline } from 'react-leaflet';
import L from 'leaflet';

import './App.css';
import withRoute from './withRoute';
import withTarget from './withTarget';
import withLocationHash from './withLocationHash';
import { ControlsPanel, Button } from './Controls';

const determineBounds = ({ loading, path }) => {
  if (loading && path.length > 0) {
    const group = new L.featureGroup(path.map((p) => L.marker(p)));
    return group.getBounds();
  }

  return undefined;
}

const FEET_PER_MILES = 5280.0;
const MILES_PER_METER = 0.0006213712;
const INIITAL_CENTER = {
  center: [37.773033, -122.438811],
  zoom: "15"
};

class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {};
    this.handleClick = this.handleClick.bind(this);
  }

  render() {
    const {
      accessToken,
      setTarget,
      undo,
      redo,
      clear,
      path,
      lines,
      totalDistance,
      target,
      targetType,
      loading,
    } = this.props;

    const bounds = determineBounds({ loading, path })
    const additionalProps = bounds ?
      { bounds } : loading ?
      INIITAL_CENTER : {};

    const totalMiles = Math.floor(totalDistance * MILES_PER_METER * 100.0) / 100.0
    return (
      <div style={{position: 'relative', height: '100%'}}>
        <Map
          dragging={target === undefined}
          style={{height: '100%'}}
          onClick={this.handleClick}
          {...additionalProps}
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
        <ControlsPanel>
          <Button onClick={undo} text='Undo' />
          <Button onClick={redo} text='Redo' />
          <Button onClick={clear} text='Clear' />
          <div style={{flex: '1'}}>{totalMiles} miles</div>
        </ControlsPanel>
      </div>
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

export default withLocationHash(withRoute(withTarget(App)));
